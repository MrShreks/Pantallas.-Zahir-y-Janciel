package com.example.pantallas.services;

import com.example.pantallas.config.ConnectionManager;
import com.example.pantallas.utils.LoggerUtil;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class ServicioInventario {

    private ServicioInventario() { }

    private static boolean dbDisponible = false;

    private static Connection obtenerConexion() throws SQLException {
        return ConnectionManager.getConnection();
    }

    // ========================================================================
    // VERIFICACIÓN DE DISPONIBILIDAD
    // ========================================================================

    public static boolean verificarBaseDatos() {
        try (Connection con = obtenerConexion()) {
            dbDisponible = (con != null && !con.isClosed());
        } catch (Exception e) {
            dbDisponible = false;
        }
        return dbDisponible;
    }

    // ========================================================================
    // EJECUTOR INTELIGENTE: prueba múltiples combinaciones de tabla/columna
    // ========================================================================

    @FunctionalInterface
    private interface Extractor<T> {
        T extraer(ResultSet rs, String[] columnas) throws SQLException;
    }

    private static <T> T consultar(String[][] tablas, String whereTemplate, Extractor<T> extractor, T fallback) {
        if (!verificarBaseDatos()) return fallback;
        for (String[] t : tablas) {
            String sql = "SELECT " + t[1] + " FROM " + t[0] + " " + whereTemplate;
            try (Connection con = obtenerConexion();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                T resultado = extractor.extraer(rs, t);
                if (resultado != null) return resultado;
            } catch (SQLException ignored) {}
        }
        return fallback;
    }

    // ========================================================================
    // VALIDACIONES CRITICAS
    // ========================================================================

    public static boolean validarStockSuficiente(String nombreProducto, double cantidadRequerida) {
        return obtenerStockActual(nombreProducto) >= cantidadRequerida;
    }

    public static double obtenerStockActual(String nombreProducto) {
        return consultar(
            new String[][]{
                {"tbl_inventario_productos", "cantidad_stock"},
                {"Productos", "stock_actual"},
                {"tbl_productos", "cantidad_stock"}
            },
            "WHERE nombre_producto = '" + nombreProducto.replace("'", "''") + "'",
            (rs, t) -> rs.next() ? rs.getDouble(t[1]) : null,
            0.0
        );
    }

    // ========================================================================
    // MOVIMIENTOS
    // ========================================================================

    public static void registrarMovimiento(String producto, String tipo, double cantidad, String unidad, String usuario) throws SQLException {
        String sql = "INSERT INTO tbl_movimientos_inventario (producto, tipo, cantidad, unidad, fecha_movimiento, usuario) VALUES (?, ?, ?, ?, GETDATE(), ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto);
            ps.setString(2, tipo);
            ps.setDouble(3, cantidad);
            ps.setString(4, unidad);
            ps.setString(5, usuario);
            ps.executeUpdate();
        }
    }

    // ========================================================================
    // ENTRADA / SALIDA / PRODUCCION
    // ========================================================================

    public static boolean entradaInventario(String producto, double cantidad, String unidad, double costoUnitario, String proveedor, String usuario) {
        try (Connection con = obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                String upsert = "MERGE tbl_inventario_productos AS target "
                        + "USING (VALUES(?)) AS source(nombre) ON target.nombre_producto = source.nombre "
                        + "WHEN MATCHED THEN UPDATE SET cantidad_stock = target.cantidad_stock + ?, "
                        + "  ultimo_costo = ?, proveedor = ?, fecha_actualizacion = GETDATE() "
                        + "WHEN NOT MATCHED THEN INSERT (nombre_producto, categoria, cantidad_stock, cantidad_minima, unidad_medida, ultimo_costo, proveedor, fecha_actualizacion) "
                        + "VALUES (?, 'Materia Prima', ?, 10.0, ?, ?, ?, GETDATE());";
                try (PreparedStatement ps = con.prepareStatement(upsert)) {
                    ps.setString(1, producto);
                    ps.setDouble(2, cantidad);
                    ps.setDouble(3, costoUnitario);
                    ps.setString(4, proveedor);
                    ps.setString(5, producto);
                    ps.setDouble(6, cantidad);
                    ps.setString(7, unidad);
                    ps.setDouble(8, costoUnitario);
                    ps.setString(9, proveedor);
                    ps.executeUpdate();
                }
                registrarMovimiento(producto, "ENTRADA", cantidad, unidad, usuario);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Excepción detectada", e);
            return false;
        }
    }

    public static boolean salidaInventario(String producto, double cantidad, String unidad, String usuario) {
        try (Connection con = obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                double actual = obtenerStockActual(producto);
                if (actual < cantidad) {
                    throw new SQLException("Stock insuficiente: " + producto + " (disp: " + actual + ", req: " + cantidad + ")");
                }
                String sql = "UPDATE tbl_inventario_productos SET cantidad_stock = cantidad_stock - ? WHERE nombre_producto = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setDouble(1, cantidad);
                    ps.setString(2, producto);
                    ps.executeUpdate();
                }
                registrarMovimiento(producto, "SALIDA", cantidad, unidad, usuario);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Excepción detectada", e);
            return false;
        }
    }

    public static boolean ejecutarProduccion(String loteId, String tipoQueso, Map<String, Double> receta, String usuario) throws SQLException {
        try (Connection con = obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                double totalDescontado = 0;
                for (Map.Entry<String, Double> item : receta.entrySet()) {
                    String producto = item.getKey();
                    double cantidad = item.getValue();
                    if (!validarStockSuficiente(producto, cantidad)) {
                        throw new SQLException("Stock insuficiente para ingrediente: " + producto);
                    }
                    salidaInventario(producto, cantidad, obtenerUnidadProducto(con, producto), usuario);
                    totalDescontado += cantidad * obtenerCostoUnitario(con, producto);
                }
                String sqlProd = "INSERT INTO tbl_produccion (lote_id, tipo_queso, estado, fecha_inicio, costo_produccion) VALUES (?, ?, 'En Produccion', GETDATE(), ?)";
                try (PreparedStatement ps = con.prepareStatement(sqlProd)) {
                    ps.setString(1, loteId);
                    ps.setString(2, tipoQueso);
                    ps.setDouble(3, totalDescontado);
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private static String obtenerUnidadProducto(Connection con, String producto) throws SQLException {
        String sql = "SELECT unidad_medida FROM tbl_inventario_productos WHERE nombre_producto = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("unidad_medida") : "Und";
        }
    }

    private static double obtenerCostoUnitario(Connection con, String producto) throws SQLException {
        String sql = "SELECT ultimo_costo FROM tbl_inventario_productos WHERE nombre_producto = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("ultimo_costo") : 0.0;
        }
    }

    // ========================================================================
    // DASHBOARD — DATOS REALES CON FALLBACK A DEMO
    // ========================================================================

    private static boolean intentarQuery(String sql) {
        try (Connection con = obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Retorna true si al menos una de las tablas principales existe.
     */
    public static boolean hayDatosReales() {
        if (!verificarBaseDatos()) return false;
        String[] tablas = {"Productos", "Ventas", "tbl_ventas", "tbl_inventario_productos", "Movimientos_Inventario"};
        for (String t : tablas) {
            try {
                if (intentarQuery("SELECT TOP 1 1 FROM " + t)) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    // ========================================================================
    // KPI: VALOR INVENTARIO
    // ========================================================================

    public static double obtenerValorInventario() {
        String[][] tablas = {
            {"Productos", "COALESCE(SUM(stock_actual * precio_venta_base),0)"},
            {"tbl_inventario_productos", "COALESCE(SUM(cantidad_stock * ultimo_costo),0)"},
            {"tbl_productos", "COALESCE(SUM(cantidad_stock * precio),0)"}
        };
        return consultar(tablas, "", (rs, t) -> rs.next() ? rs.getDouble(1) : null, 0.0);
    }

    // ========================================================================
    // KPI: STOCK BAJO
    // ========================================================================

    public static int obtenerStockBajo() {
        String[][] tablas = {
            {"Productos", "COUNT(*)"},
            {"tbl_inventario_productos", "COUNT(*)"},
            {"tbl_productos", "COUNT(*)"}
        };
        String[][] condiciones = {
            {"stock_actual", "stock_minimo"},
            {"cantidad_stock", "cantidad_minima"},
            {"cantidad_stock", "stock_minimo"}
        };
        for (int i = 0; i < tablas.length; i++) {
            String sql = "SELECT " + tablas[i][1] + " FROM " + tablas[i][0] + " WHERE " + condiciones[i][0] + " <= " + condiciones[i][1];
            try (Connection con = obtenerConexion();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException ignored) {}
        }
        return 0;
    }

    // ========================================================================
    // KPI: PRODUCCION ACTIVA
    // ========================================================================

    public static int obtenerProduccionActiva() {
        String[][] tablas = {
            {"tbl_lotes_produccion", "estado", "'ACTIVO','EN PROCESO','En Produccion'"},
            {"tbl_produccion", "estado", "'En Produccion','ACTIVO','EN PROCESO'"},
            {"Ordenes_Produccion", "estado", "'EN PROCESO','ACTIVA','PENDIENTE'"}
        };
        for (String[] t : tablas) {
            String sql = "SELECT COUNT(*) FROM " + t[0] + " WHERE " + t[1] + " IN (" + t[2] + ")";
            try (Connection con = obtenerConexion();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException ignored) {}
        }
        return 0;
    }

    // ========================================================================
    // KPI: ORDENES PENDIENTES
    // ========================================================================

    public static int obtenerOrdenesPendientes() {
        String[][] tablas = {
            {"Compras", "estatus", "'PENDIENTE'"},
            {"tbl_ordenes_compra", "estado", "'Pendiente'"},
            {"Ordenes_Compra", "estado", "'PENDIENTE'"}
        };
        for (String[] t : tablas) {
            String sql = "SELECT COUNT(*) FROM " + t[0] + " WHERE " + t[1] + " = " + t[2];
            try (Connection con = obtenerConexion();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException ignored) {}
        }
        return 0;
    }

    // ========================================================================
    // KPI: VENTAS DEL MES ACTUAL Y ANTERIOR
    // ========================================================================

    private static double ejecutarVentasQuery(String where) {
        String[][] tablas = {
            {"Ventas", "total_venta", "fecha_venta"},
            {"tbl_ventas", "monto_total", "fecha"},
            {"tbl_ventas", "total", "fecha_venta"}
        };
        for (String[] t : tablas) {
            String sql = "SELECT COALESCE(SUM(" + t[1] + "),0) FROM " + t[0] + " WHERE " + t[2] + " >= " + where;
            try (Connection con = obtenerConexion();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return rs.getDouble(1);
            } catch (SQLException ignored) {}
        }
        return -1;
    }

    public static double obtenerVentasMesActual() {
        double v = ejecutarVentasQuery("DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()), 0)");
        return v >= 0 ? v : 0.0;
    }

    public static double obtenerVentasMesAnterior() {
        double v = ejecutarVentasQuery(
            "DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()) - 1, 0) AND " +
            "fecha_venta < DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()), 0)");
        if (v >= 0) return v;
        // fallback con otras columnas de fecha
        String[][] tablas = {
            {"Ventas", "total_venta", "fecha_venta"},
            {"tbl_ventas", "monto_total", "fecha"}
        };
        for (String[] t : tablas) {
            String sql = "SELECT COALESCE(SUM(" + t[1] + "),0) FROM " + t[0] +
                " WHERE MONTH(" + t[2] + ") = MONTH(DATEADD(MONTH, -1, GETDATE()))" +
                " AND YEAR(" + t[2] + ") = YEAR(DATEADD(MONTH, -1, GETDATE()))";
            try (Connection con = obtenerConexion();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return rs.getDouble(1);
            } catch (SQLException ignored) {}
        }
        return 0.0;
    }

    // ========================================================================
    // DISTRIBUCION POR CATEGORIA (PieChart)
    // ========================================================================

    public static Map<String, Number> obtenerDistribucionCategorias() {
        Map<String, Number> dist = new LinkedHashMap<>();
        String[][] tablas = {
            {"Productos", "categoria"},
            {"tbl_productos", "categoria"},
            {"tbl_inventario_productos", "categoria"}
        };
        for (String[] t : tablas) {
            String sql = "SELECT " + t[1] + ", COUNT(*) AS cnt FROM " + t[0] + " GROUP BY " + t[1];
            try (Connection con = obtenerConexion();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                boolean hay = false;
                while (rs.next()) {
                    dist.put(rs.getString(t[1]), rs.getInt("cnt"));
                    hay = true;
                }
                if (hay) return dist;
            } catch (SQLException ignored) {}
        }
        // Fallback: intentar con "activo = 1"
        for (String[] t : tablas) {
            String sql = "SELECT " + t[1] + ", COUNT(*) AS cnt FROM " + t[0] + " WHERE activo = 1 GROUP BY " + t[1];
            try (Connection con = obtenerConexion();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                boolean hay = false;
                while (rs.next()) {
                    dist.put(rs.getString(t[1]), rs.getInt("cnt"));
                    hay = true;
                }
                if (hay) return dist;
            } catch (SQLException ignored) {}
        }
        return dist;
    }

    // ========================================================================
    // VENTAS MENSUALES (BarChart)
    // ========================================================================

    public static Map<String, Double> obtenerVentasMensuales(int ultimosMeses) {
        Map<String, Double> ventas = new LinkedHashMap<>();
        String[][] tablas = {
            {"Ventas", "fecha_venta", "total_venta"},
            {"tbl_ventas", "fecha", "monto_total"},
            {"tbl_ventas", "fecha_venta", "total"}
        };
        for (String[] t : tablas) {
            String sql = "SELECT YEAR(" + t[1] + ") AS anio, MONTH(" + t[1] + ") AS mes, COALESCE(SUM(" + t[2] + "),0) AS total" +
                " FROM " + t[0] +
                " WHERE " + t[1] + " >= DATEADD(MONTH, -" + ultimosMeses + ", GETDATE())" +
                " GROUP BY YEAR(" + t[1] + "), MONTH(" + t[1] + ") ORDER BY anio ASC, mes ASC";
            try (Connection con = obtenerConexion();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                boolean hay = false;
                while (rs.next()) {
                    String label = String.format("%02d/%d", rs.getInt("mes"), rs.getInt("anio"));
                    ventas.put(label, rs.getDouble("total"));
                    hay = true;
                }
                if (hay) return ventas;
            } catch (SQLException ignored) {}
        }
        return ventas;
    }

    // ========================================================================
    // ALERTAS DE STOCK DETALLADAS
    // ========================================================================

    public static List<Map<String, String>> obtenerAlertasStockDetalladas() {
        List<Map<String, String>> alertas = new ArrayList<>();
        String[][] tablas = {
            {"Productos", "nombre_producto", "stock_actual", "stock_minimo", "unidad_medida"},
            {"tbl_inventario_productos", "nombre_producto", "cantidad_stock", "cantidad_minima", "unidad_medida"},
            {"tbl_productos", "nombre", "cantidad_stock", "stock_minimo", "unidad"}
        };
        for (String[] t : tablas) {
            String sql = "SELECT " + t[1] + ", " + t[2] + ", " + t[3] + ", " + t[4] +
                " FROM " + t[0] + " WHERE " + t[2] + " <= " + t[3] + " ORDER BY " + t[2] + " ASC";
            try (Connection con = obtenerConexion();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                boolean hay = false;
                while (rs.next()) {
                    double stock = rs.getDouble(t[2]);
                    double minimo = rs.getDouble(t[3]);
                    String severidad = stock <= minimo * 0.5 ? "CRITICA" : stock <= minimo * 0.75 ? "ALTA" : "MEDIA";
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("producto", rs.getString(t[1]));
                    item.put("stock", String.format("%.2f", stock));
                    item.put("minimo", String.format("%.2f", minimo));
                    item.put("unidad", rs.getString(t[4]));
                    item.put("severidad", severidad);
                    alertas.add(item);
                    hay = true;
                }
                if (hay) return alertas;
            } catch (SQLException ignored) {}
        }
        return alertas;
    }

    // ========================================================================
    // ULTIMOS MOVIMIENTOS DE INVENTARIO
    // ========================================================================

    public static List<Map<String, String>> obtenerUltimosMovimientos(int limite) {
        List<Map<String, String>> movs = new ArrayList<>();
        String[][] tablas = {
            {"Movimientos_Inventario", "fecha_movimiento", "tipo_movimiento", "cantidad", "observaciones", "id_producto"},
            {"tbl_movimientos_inventario", "fecha_movimiento", "tipo", "cantidad", "producto", "usuario"},
            {"tbl_movimientos", "fecha", "tipo_movimiento", "cantidad", "producto", "usuario"}
        };
        for (String[] t : tablas) {
            String sql = "SELECT TOP " + limite + " * FROM " + t[0] + " ORDER BY " + t[1] + " DESC";
            try (Connection con = obtenerConexion();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                boolean hay = false;
                while (rs.next()) {
                    Map<String, String> m = new LinkedHashMap<>();
                    Timestamp ts = rs.getTimestamp(t[1]);
                    m.put("fecha", ts != null ? ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "—");
                    m.put("tipo", rs.getString(t[2]) != null ? rs.getString(t[2]) : "—");
                    m.put("cantidad", String.format("%.2f", rs.getDouble(t[3])));
                    m.put("producto", rs.getObject(t[4]) != null ? rs.getString(t[4]) : "—");
                    m.put("usuario", t.length > 5 && rs.getObject(t[5]) != null ? rs.getString(t[5]) : "Sistema");
                    movs.add(m);
                    hay = true;
                }
                if (hay) return movs;
            } catch (SQLException ignored) {}
        }
        return movs;
    }

    // ========================================================================
    // DATOS DE DEMOSTRACION (cuando la BD no está disponible)
    // ========================================================================

    public static Map<String, Number> generarDemoKPIs() {
        Map<String, Number> demo = new HashMap<>();
        demo.put("valor_inventario", 284750.50);
        demo.put("stock_bajo", 4);
        demo.put("produccion_activa", 3);
        demo.put("ordenes_pendientes", 2);
        demo.put("ventas_mes", 158320.00);
        demo.put("ventas_anterior", 142100.00);
        demo.put("valor_anterior", 261200.00);
        return demo;
    }

    public static Map<String, Number> generarDemoDistribucion() {
        Map<String, Number> demo = new LinkedHashMap<>();
        demo.put("Lácteos", 8);
        demo.put("Materia Prima", 5);
        demo.put("Empaques", 4);
        demo.put("Insumos", 3);
        return demo;
    }

    public static Map<String, Double> generarDemoVentasMensuales() {
        Map<String, Double> demo = new LinkedHashMap<>();
        LocalDate hoy = LocalDate.now();
        double[] valores = {125000, 138000, 142100, 151200, 148500, 158320};
        for (int i = 5; i >= 0; i--) {
            LocalDate mes = hoy.minusMonths(i);
            demo.put(String.format("%02d/%d", mes.getMonthValue(), mes.getYear()), valores[5 - i]);
        }
        return demo;
    }

    public static List<Map<String, String>> generarDemoAlertas() {
        List<Map<String, String>> alertas = new ArrayList<>();
        String[][] data = {
            {"Queso Mozzarella", "8.00", "20.00", "LIBRAS", "CRITICA"},
            {"Queso Crema", "5.50", "15.00", "LIBRAS", "CRITICA"},
            {"Empaque Plástico", "50.00", "100.00", "UNIDADES", "ALTA"},
            {"Sal Industrial", "25.00", "40.00", "KG", "MEDIA"}
        };
        for (String[] d : data) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("producto", d[0]);
            item.put("stock", d[1]);
            item.put("minimo", d[2]);
            item.put("unidad", d[3]);
            item.put("severidad", d[4]);
            alertas.add(item);
        }
        return alertas;
    }

    public static List<Map<String, String>> generarDemoMovimientos() {
        List<Map<String, String>> movs = new ArrayList<>();
        String[][] data = {
            {"15/05/2026 08:30", "ENTRADA", "Queso Blanco", "100.00", "Admin"},
            {"14/05/2026 14:15", "SALIDA", "Queso Amarillo", "25.00", "Ventas"},
            {"14/05/2026 10:00", "ENTRADA", "Sal Industrial", "50.00", "Compras"},
            {"13/05/2026 16:45", "SALIDA", "Queso Mozzarella", "30.00", "Producción"},
            {"13/05/2026 09:20", "ENTRADA", "Empaque Plástico", "200.00", "Admin"},
            {"12/05/2026 11:30", "AJUSTE", "Queso Crema", "-2.00", "Inventario"},
            {"11/05/2026 15:00", "SALIDA", "Queso Blanco", "50.00", "Ventas"},
            {"10/05/2026 08:00", "ENTRADA", "Leche Fresca", "500.00", "Compras"},
            {"09/05/2026 13:20", "SALIDA", "Cuajo", "5.00", "Producción"},
            {"08/05/2026 10:45", "ENTRADA", "Queso Amarillo", "75.00", "Admin"}
        };
        for (String[] d : data) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("fecha", d[0]);
            m.put("tipo", d[1]);
            m.put("producto", d[2]);
            m.put("cantidad", d[3]);
            m.put("usuario", d[4]);
            movs.add(m);
        }
        return movs;
    }
}
