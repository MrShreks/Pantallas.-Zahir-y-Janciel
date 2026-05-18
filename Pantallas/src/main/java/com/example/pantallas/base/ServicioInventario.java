package com.example.pantallas.base;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio centralizado de inventario.
 * Toda operacion que toca stock pasa por aqui.
 */
public final class ServicioInventario {

    private ServicioInventario() { }

    /** --- VALIDACIONES CRITICAS --- */

    public static boolean validarStockSuficiente(String nombreProducto, double cantidadRequerida) {
        String sql = "SELECT cantidad_stock FROM tbl_inventario_productos WHERE nombre_producto = ?";
        try (Connection con = FabricaBase.abrirConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreProducto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("cantidad_stock") >= cantidadRequerida;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static double obtenerStockActual(String nombreProducto) {
        String sql = "SELECT cantidad_stock FROM tbl_inventario_productos WHERE nombre_producto = ?";
        try (Connection con = FabricaBase.abrirConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreProducto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("cantidad_stock");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /** --- MOVIMIENTOS --- */

    public static void registrarMovimiento(String producto, String tipo, double cantidad, String unidad, String usuario) throws SQLException {
        String sql = "INSERT INTO tbl_movimientos_inventario (producto, tipo, cantidad, unidad, fecha_movimiento, usuario) VALUES (?, ?, ?, ?, GETDATE(), ?)";
        try (Connection con = FabricaBase.abrirConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto);
            ps.setString(2, tipo);
            ps.setDouble(3, cantidad);
            ps.setString(4, unidad);
            ps.setString(5, usuario);
            ps.executeUpdate();
        }
    }

    /** --- ENTRADA (Compra / Recepcion) --- */

    public static boolean entradaInventario(String producto, double cantidad, String unidad, double costoUnitario, String proveedor, String usuario) {
        try (Connection con = FabricaBase.abrirConexion()) {
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
            e.printStackTrace();
            return false;
        }
    }

    /** --- SALIDA (Venta / Produccion) --- */

    public static boolean salidaInventario(String producto, double cantidad, String unidad, String usuario) {
        try (Connection con = FabricaBase.abrirConexion()) {
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
            e.printStackTrace();
            return false;
        }
    }

    /** --- PRODUCCION: Descuenta materiales y crea producto terminado --- */

    public static boolean ejecutarProduccion(String loteId, String tipoQueso, java.util.Map<String, Double> receta, String usuario) throws SQLException {
        try (Connection con = FabricaBase.abrirConexion()) {
            con.setAutoCommit(false);
            try {
                double totalDescontado = 0;
                for (java.util.Map.Entry<String, Double> item : receta.entrySet()) {
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

    /** --- REPORTES Y ESTADISTICAS --- */

    public static java.util.Map<String, Number> obtenerEstadisticasDashboard() {
        java.util.Map<String, Number> stats = new java.util.HashMap<>();
        String[] queries = {
            "SELECT COUNT(*) AS val FROM tbl_inventario_productos WHERE cantidad_stock <= cantidad_minima",
            "SELECT COALESCE(SUM(cantidad_stock * ultimo_costo),0) AS val FROM tbl_inventario_productos",
            "SELECT COUNT(*) AS val FROM tbl_produccion WHERE estado = 'En Produccion'",
            "SELECT COUNT(*) AS val FROM tbl_ordenes_compra WHERE estado = 'Pendiente'",
            "SELECT SUM(monto_total) AS val FROM tbl_envios WHERE estatus = 'Entregado' AND MONTH(fecha) = MONTH(GETDATE())"
        };
        String[] keys = {"stock_bajo", "valor_inventario", "produccion_activa", "ordenes_pendientes", "ventas_mes"};
        for (int i = 0; i < queries.length; i++) {
            try (Connection con = FabricaBase.abrirConexion();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(queries[i])) {
                if (rs.next()) stats.put(keys[i], rs.getDouble("val"));
            } catch (SQLException e) { stats.put(keys[i], 0); }
        }
        return stats;
    }
}
