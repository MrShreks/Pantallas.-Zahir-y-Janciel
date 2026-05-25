package com.example.pantallas.controllers;

import com.example.pantallas.services.FabricaBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CompraController {

    @FXML private VBox paneNuevaOrden, paneRecepcion, paneHistorial, paneProveedores;
    @FXML private ComboBox<String> cbSuplidor, cbInsumo, cbOrdenesPendientes, cbNuevoEstado, cbUnidad, cbUnidadReal;
    @FXML private TextField txtCantidad, txtFiltroRecepcion, txtCantidadReal;
    @FXML private DatePicker dpFechaEntrega;
    @FXML private TextArea txtNotasRecepcion;
    @FXML private TableView<OrdenCompra> tablaOrdenesPendientes, tablaHistorial;
    @FXML private TableView<ObservacionCalidad> tablaHistorialCalidad;
    @FXML private Label lblPrecioUnitario;
    @FXML private Button btnNavOrden, btnNavRecepcion, btnNavHistorial, btnNavProveedores;
    @FXML private CheckBox chkCantidadCorrecta, chkCantidadIncorrecta;
    @FXML private CheckBox chkFiltrarPendiente, chkFiltrarRecibido, chkFiltrarCancelado;
    @FXML private HBox hboxCantidadReal;

    // Proveedores fields
    @FXML private TextField txtProvNombre, txtProvTelefono, txtProvRnc, txtProvDireccion;
    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, Integer> colProvId;
    @FXML private TableColumn<Proveedor, String> colProvNombre, colProvTel, colProvRnc, colProvDir;

    private int idProveedorSeleccionado = -1;

    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;trustServerCertificate=true;encrypt=false;";
        
    private final ObservableList<OrdenCompra> listaOrdenes = FXCollections.observableArrayList();
    private final ObservableList<String> listaPendientes = FXCollections.observableArrayList();
    private final ObservableList<String> listaPendientesFiltradas = FXCollections.observableArrayList();
    private final ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();
    private final ObservableList<Proveedor> listaProveedoresFallback = FXCollections.observableArrayList();
    private final ObservableList<ObservacionCalidad> listaObservaciones = FXCollections.observableArrayList();
    private Map<String, ProductoInfo> productosActuales = new HashMap<>();
    private double precioUnitarioActual = 0.0;
    private String unidadBaseActual = "";
    private double precioBaseOriginal = 0.0;
    private String unidadBaseOriginal = "";

    // Factores de conversion entre unidades (base = Unidades)
    private static final Map<String, Double> FACTORES_CONVERSION = new HashMap<>();
    static {
        FACTORES_CONVERSION.put("Unidades", 1.0);
        FACTORES_CONVERSION.put("Paquetes", 1.0);
        FACTORES_CONVERSION.put("Litros", 1.0);
        FACTORES_CONVERSION.put("Galones", 3.785);
        FACTORES_CONVERSION.put("Kilos", 1.0);
        FACTORES_CONVERSION.put("Libras", 0.4536);
        FACTORES_CONVERSION.put("Rollos", 1.0);
    }

    private static final Map<String, List<String>> UNIDADES_POR_GRUPO = new HashMap<>();
    static {
        UNIDADES_POR_GRUPO.put("Litros", Arrays.asList("Litros", "Galones"));
        UNIDADES_POR_GRUPO.put("Galones", Arrays.asList("Litros", "Galones"));
        UNIDADES_POR_GRUPO.put("Kilos", Arrays.asList("Kilos", "Libras"));
        UNIDADES_POR_GRUPO.put("Libras", Arrays.asList("Kilos", "Libras"));
        UNIDADES_POR_GRUPO.put("Unidades", Arrays.asList("Unidades", "Paquetes"));
        UNIDADES_POR_GRUPO.put("Paquetes", Arrays.asList("Unidades", "Paquetes"));
        UNIDADES_POR_GRUPO.put("Rollos", Arrays.asList("Rollos"));
    }

    private static class ProductoInfo {
        double precio; String unidad;
        ProductoInfo(double precio, String unidad) { this.precio = precio; this.unidad = unidad; }
    }

    // Fallback hardcoded para seed inicial
    private static final Map<String, Map<String, ProductoInfo>> PRODUCTOS_FALLBACK = new HashMap<>();
    static {
        Map<String, ProductoInfo> lacteos = new HashMap<>();
        lacteos.put("Leche Cruda", new ProductoInfo(45.00, "Litros"));
        lacteos.put("Leche Pasteurizada", new ProductoInfo(65.00, "Litros"));
        lacteos.put("Crema de Leche", new ProductoInfo(180.00, "Litros"));
        lacteos.put("Suero de Leche", new ProductoInfo(25.00, "Litros"));
        PRODUCTOS_FALLBACK.put("Lacteos del Yaque", lacteos);

        Map<String, ProductoInfo> insumos = new HashMap<>();
        insumos.put("Sal Industrial", new ProductoInfo(45.00, "Kilos"));
        insumos.put("Cuajo Líquido", new ProductoInfo(2500.00, "Litros"));
        insumos.put("Cuajo en Polvo", new ProductoInfo(3200.00, "Paquetes"));
        insumos.put("Fundas para Queso", new ProductoInfo(120.00, "Paquetes"));
        insumos.put("Colorante Alimenticio", new ProductoInfo(850.00, "Unidades"));
        insumos.put("Antioxidante", new ProductoInfo(650.00, "Kilos"));
        insumos.put("Cultivo Lactico", new ProductoInfo(2400.00, "Unidades"));
        insumos.put("Cloruro de Calcio", new ProductoInfo(850.00, "Kilos"));
        PRODUCTOS_FALLBACK.put("Insumos RD", insumos);

        Map<String, ProductoInfo> empaques = new HashMap<>();
        empaques.put("Fundas al Vacio", new ProductoInfo(450.00, "Rollos"));
        empaques.put("Cajas de Carton", new ProductoInfo(75.00, "Unidades"));
        empaques.put("Etiquetas Adhesivas", new ProductoInfo(1200.00, "Rollos"));
        empaques.put("Film Plastico", new ProductoInfo(380.00, "Rollos"));
        PRODUCTOS_FALLBACK.put("Empaques Cibao", empaques);

        Map<String, ProductoInfo> ganaderia = new HashMap<>();
        ganaderia.put("Leche Cruda", new ProductoInfo(42.00, "Litros"));
        ganaderia.put("Crema de Leche", new ProductoInfo(175.00, "Litros"));
        PRODUCTOS_FALLBACK.put("Ganaderia del Norte", ganaderia);
    }

    @FXML
    public void initialize() {
        FabricaBase.asegurarEsquemaProveedores();
        asegurarTablaOrdenesCompra();
        asegurarTablaInventarioProductos();
        migrarNombresProductos();
        asegurarProveedoresIniciales();
        asegurarTablaProductos();
        asegurarTablaHistorialCalidad();
        FabricaBase.seedFactoresConversion();
        configurarTablas();
        cargarCombos();
        int diasRandom = ThreadLocalRandom.current().nextInt(2, 8);
        dpFechaEntrega.setValue(LocalDate.now().plusDays(diasRandom));
        dpFechaEntrega.setDisable(true);
        cargarOrdenesHistorial();
        cargarProveedores();
        cargarHistorialCalidad();

        cbSuplidor.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) { cargarProductosPorSuplidor(newVal); limpiarCamposProducto(); }
        });
        cbInsumo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) onInsumoSeleccionado();
        });
        txtCantidad.textProperty().addListener((obs, oldVal, newVal) -> calcularTotal());

        // Listener para cambio de unidad -> recalcular precio automaticamente
        cbUnidad.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && precioUnitarioActual > 0 && !unidadBaseActual.isEmpty()) {
                convertirPrecioPorUnidad(newVal);
            }
        });

        // Listener para filtro de recepcion por cliente/suplidor
        if (txtFiltroRecepcion != null)
            txtFiltroRecepcion.textProperty().addListener((obs, oldVal, newVal) -> filtrarOrdenesPendientes());

        // Checkboxes mutuamente excluyentes para entrega
        if (chkCantidadCorrecta != null && chkCantidadIncorrecta != null && hboxCantidadReal != null) {
            chkCantidadCorrecta.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    chkCantidadIncorrecta.setSelected(false);
                    hboxCantidadReal.setVisible(false);
                    hboxCantidadReal.setManaged(false);
                }
            });
            chkCantidadIncorrecta.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    chkCantidadCorrecta.setSelected(false);
                    hboxCantidadReal.setVisible(true);
                    hboxCantidadReal.setManaged(true);
                } else {
                    hboxCantidadReal.setVisible(false);
                    hboxCantidadReal.setManaged(false);
                }
            });
        }

        // Listeners para filtros de estado en Historial
        if (chkFiltrarPendiente != null && chkFiltrarRecibido != null && chkFiltrarCancelado != null) {
            chkFiltrarPendiente.setOnAction(e -> aplicarFiltroEstado());
            chkFiltrarRecibido.setOnAction(e -> aplicarFiltroEstado());
            chkFiltrarCancelado.setOnAction(e -> aplicarFiltroEstado());
        }

        // Listener para seleccion de proveedores (edicion) - usa dbId para operaciones SQL
        if (tablaProveedores != null)
            tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                idProveedorSeleccionado = newV.getDbId();
                txtProvNombre.setText(newV.getNombre());
                txtProvTelefono.setText(newV.getTelefono());
                if (txtProvRnc != null) txtProvRnc.setText(newV.getRnc() != null ? newV.getRnc() : "");
                if (txtProvDireccion != null) txtProvDireccion.setText(newV.getDireccion() != null ? newV.getDireccion() : "");
            }
        });
    }

    private void asegurarProveedoresIniciales() {
        String sqlCount = "SELECT COUNT(*) FROM tbl_proveedores";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sqlCount)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String[][] iniciales = {
                    {"Lacteos del Yaque", "001-0000001-1", "809-555-0101", "Av. Principal, Santo Domingo"},
                    {"Insumos RD", "001-0000002-2", "809-555-0102", "Calle Secundaria, Santiago"},
                    {"Empaques Cibao", "001-0000003-3", "809-555-0103", "Zona Industrial, La Vega"},
                    {"Ganaderia del Norte", "001-0000004-4", "809-555-0104", "Carretera Duarte, Puerto Plata"}
                };
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO tbl_proveedores (nombre, rnc, telefono, direccion) VALUES (?, ?, ?, ?)")) {
                    for (String[] p : iniciales) {
                        ps.setString(1, p[0]); ps.setString(2, p[1]);
                        ps.setString(3, p[2]); ps.setString(4, p[3]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.info("No se pudo sembrar proveedores iniciales: " + e.getMessage());
        }
    }

    private void asegurarTablaOrdenesCompra() {
        String sql = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_ordenes_compra' AND xtype='U') "
                + "CREATE TABLE tbl_ordenes_compra ("
                + "id_orden INT IDENTITY PRIMARY KEY, "
                + "suplidor VARCHAR(200), "
                + "insumo VARCHAR(200), "
                + "cantidad DECIMAL(10,2), "
                + "precio_unitario DECIMAL(10,2), "
                + "fecha_pedido DATETIME DEFAULT GETDATE(), "
                + "fecha_entrega_esperada DATE, "
                + "fecha_recepcion DATETIME, "
                + "estado VARCHAR(50) DEFAULT 'Pendiente', "
                + "notas TEXT, "
                + "cantidad_recibida DECIMAL(10,2))";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sql);
            // Siempre asegurar al menos 3 ordenes pendientes de ejemplo
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tbl_ordenes_compra WHERE estado = 'Pendiente'");
            if (rs.next() && rs.getInt(1) < 3) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO tbl_ordenes_compra (suplidor, insumo, cantidad, precio_unitario, fecha_entrega_esperada, estado) VALUES (?, ?, ?, ?, DATEADD(day,3,GETDATE()), 'Pendiente')")) {
                    ps.setString(1, "Lacteos del Yaque"); ps.setString(2, "Crema de Leche (Litros)");
                    ps.setDouble(3, 20); ps.setDouble(4, 180.00); ps.addBatch();
                    ps.setString(1, "Insumos RD"); ps.setString(2, "Sal Industrial (Kilos)");
                    ps.setDouble(3, 50); ps.setDouble(4, 45.00); ps.addBatch();
                    ps.setString(1, "Empaques Cibao"); ps.setString(2, "Fundas al Vacio (Rollos)");
                    ps.setDouble(3, 10); ps.setDouble(4, 450.00); ps.addBatch();
                    ps.executeBatch();
                }
            }
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.info("No se pudo crear/seed tbl_ordenes_compra: " + e.getMessage());
        }
    }

    private void asegurarTablaHistorialCalidad() {
        String sqlCreate = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_historial_calidad' AND xtype='U') "
                + "CREATE TABLE tbl_historial_calidad ("
                + "id INT IDENTITY PRIMARY KEY, "
                + "id_orden INT, "
                + "suplidor VARCHAR(200), "
                + "insumo VARCHAR(200), "
                + "cantidad_esperada DECIMAL(10,2), "
                + "cantidad_recibida DECIMAL(10,2), "
                + "unidad VARCHAR(50), "
                + "entrega_correcta BIT, "
                + "observaciones TEXT, "
                + "fecha_recepcion DATETIME DEFAULT GETDATE())";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sqlCreate);
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.info("No se pudo crear tbl_historial_calidad: " + e.getMessage());
        }
    }

    private void convertirPrecioPorUnidad(String nuevaUnidad) {
        if (unidadBaseOriginal.isEmpty()) { calcularTotal(); return; }
        if (unidadBaseOriginal.equalsIgnoreCase(nuevaUnidad)) {
            precioUnitarioActual = precioBaseOriginal;
            unidadBaseActual = nuevaUnidad;
            calcularTotal();
            return;
        }
        double factorBase = FACTORES_CONVERSION.getOrDefault(unidadBaseOriginal, 1.0);
        double factorNueva = FACTORES_CONVERSION.getOrDefault(nuevaUnidad, 1.0);
        if (factorBase > 0 && factorNueva > 0) {
            precioUnitarioActual = precioBaseOriginal * (factorBase / factorNueva);
            unidadBaseActual = nuevaUnidad;
        }
        calcularTotal();
    }

    private void limpiarCamposProducto() {
        cbInsumo.getSelectionModel().clearSelection();
        txtCantidad.clear();
        lblPrecioUnitario.setText("RD$ 0.00");
        precioUnitarioActual = 0.0;
        unidadBaseActual = "";
        precioBaseOriginal = 0.0;
        unidadBaseOriginal = "";
    }

    private void asegurarTablaProductos() {
        String sqlCreate = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_suplidor_productos' AND xtype='U') "
                + "CREATE TABLE tbl_suplidor_productos ("
                + "id INT IDENTITY PRIMARY KEY, "
                + "id_suplidor INT, "
                + "producto VARCHAR(200) NOT NULL, "
                + "precio_unitario DECIMAL(10,2), "
                + "unidad VARCHAR(50))";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sqlCreate);
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tbl_suplidor_productos");
            if (rs.next() && rs.getInt(1) == 0) seedProductos(con);
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.info("No se pudo crear/seed tbl_suplidor_productos: " + e.getMessage());
        }
    }

    private void seedProductos(Connection con) {
        String sqlInsertProv = "IF NOT EXISTS (SELECT 1 FROM tbl_proveedores WHERE nombre = ?) "
                + "INSERT INTO tbl_proveedores (nombre, rnc, telefono, direccion) VALUES (?, '', '', '')";
        String sqlGetId = "SELECT provider_id FROM tbl_proveedores WHERE nombre = ?";
        String sqlProd = "INSERT INTO tbl_suplidor_productos (id_suplidor, producto, precio_unitario, unidad) VALUES (?, ?, ?, ?)";
        try (PreparedStatement psProv = con.prepareStatement(sqlInsertProv);
             PreparedStatement psGet = con.prepareStatement(sqlGetId);
             PreparedStatement ps = con.prepareStatement(sqlProd)) {
            for (Map.Entry<String, Map<String, ProductoInfo>> entry : PRODUCTOS_FALLBACK.entrySet()) {
                psProv.setString(1, entry.getKey());
                psProv.setString(2, entry.getKey());
                try { psProv.executeUpdate(); } catch (Exception ignored) { }
                psGet.setString(1, entry.getKey());
                ResultSet rs = psGet.executeQuery();
                Integer idSup = rs.next() ? rs.getInt("provider_id") : null;
                if (idSup == null) continue;
                for (Map.Entry<String, ProductoInfo> prod : entry.getValue().entrySet()) {
                    ps.setInt(1, idSup); ps.setString(2, prod.getKey());
                    ps.setDouble(3, prod.getValue().precio); ps.setString(4, prod.getValue().unidad);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        } catch (Exception e) { com.example.pantallas.utils.LoggerUtil.info("No se pudo seed productos: " + e.getMessage()); }
    }

    private Integer getIdSuplidor(Connection con, String nombre) {
        try (PreparedStatement ps = con.prepareStatement("SELECT provider_id FROM tbl_proveedores WHERE nombre = ?")) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("provider_id");
        } catch (Exception e) { }
        try (PreparedStatement ps = con.prepareStatement("SELECT id_suplidor FROM tbl_suplidores WHERE nombre = ?")) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_suplidor");
        } catch (Exception e) { }
        return null;
    }

    private void cargarProductosPorSuplidor(String suplidorNombre) {
        productosActuales.clear();
        ObservableList<String> items = FXCollections.observableArrayList();
        boolean cargado = false;

        String sqlFactores = "SELECT fc.producto, fc.precio_unitario, fc.unidad "
                + "FROM tbl_factores_conversion fc "
                + "JOIN tbl_proveedores p ON fc.id_proveedor = p.provider_id "
                + "WHERE p.nombre = ?";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlFactores)) {
            ps.setString(1, suplidorNombre);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String prod = rs.getString("producto");
                productosActuales.put(prod, new ProductoInfo(rs.getDouble("precio_unitario"), rs.getString("unidad")));
                items.add(prod);
            }
            if (!productosActuales.isEmpty()) cargado = true;
        } catch (Exception e) { }

        if (!cargado) {
            String sqlLegacy = "SELECT sp.producto, sp.precio_unitario, sp.unidad "
                    + "FROM tbl_suplidor_productos sp "
                    + "LEFT JOIN tbl_suplidores s ON sp.id_suplidor = s.id_suplidor "
                    + "LEFT JOIN tbl_proveedores p ON sp.id_suplidor = p.provider_id "
                    + "WHERE s.nombre = ? OR p.nombre = ?";
            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sqlLegacy)) {
                ps.setString(1, suplidorNombre); ps.setString(2, suplidorNombre);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String prod = rs.getString("producto");
                    productosActuales.put(prod, new ProductoInfo(rs.getDouble("precio_unitario"), rs.getString("unidad")));
                    items.add(prod);
                }
                if (!productosActuales.isEmpty()) cargado = true;
            } catch (Exception e) { }
        }

        // Fallback: busqueda case-insensitive en PRODUCTOS_FALLBACK
        String keyMatch = null;
        for (String key : PRODUCTOS_FALLBACK.keySet()) {
            if (key.equalsIgnoreCase(suplidorNombre.trim())) {
                keyMatch = key;
                break;
            }
        }
        if (keyMatch != null) {
            Map<String, ProductoInfo> fallback = PRODUCTOS_FALLBACK.get(keyMatch);
            for (Map.Entry<String, ProductoInfo> entry : fallback.entrySet()) {
                if (!productosActuales.containsKey(entry.getKey())) {
                    productosActuales.put(entry.getKey(), entry.getValue());
                    items.add(entry.getKey());
                }
            }
        }

        // Si aun no hay productos, agregar el primer fallback disponible como muestra
        if (items.isEmpty() && !PRODUCTOS_FALLBACK.isEmpty()) {
            Map.Entry<String, Map<String, ProductoInfo>> first = PRODUCTOS_FALLBACK.entrySet().iterator().next();
            for (Map.Entry<String, ProductoInfo> entry : first.getValue().entrySet()) {
                productosActuales.put(entry.getKey(), entry.getValue());
                items.add(entry.getKey());
            }
        }

        cbInsumo.setItems(items);
    }

    private void resetVistas() {
        VBox[] panes = {paneNuevaOrden, paneRecepcion, paneHistorial, paneProveedores};
        for (VBox p : panes) {
            if (p != null) { p.setVisible(false); p.setManaged(false); }
        }
    }

    @FXML private void mostrarNuevaOrden() { alternarVista(paneNuevaOrden, btnNavOrden); }
    @FXML private void mostrarRecepcion() { alternarVista(paneRecepcion, btnNavRecepcion); cargarOrdenesPendientes(); cargarHistorialCalidad(); }
    @FXML private void mostrarHistorial() {
        alternarVista(paneHistorial, btnNavHistorial);
        cargarOrdenesHistorial();
    }
    @FXML private void mostrarProveedores() { alternarVista(paneProveedores, btnNavProveedores); cargarProveedores(); }

    private void alternarVista(VBox pane, Button btn) {
        resetVistas();
        if (pane != null) { pane.setVisible(true); pane.setManaged(true); }
        if (btn != null) {
            Button[] btns = {btnNavOrden, btnNavRecepcion, btnNavHistorial, btnNavProveedores};
            for (Button b : btns) {
                if (b != null) {
                    b.getStyleClass().remove("nav-item-active");
                    if (!b.getStyleClass().contains("nav-item")) b.getStyleClass().add("nav-item");
                }
            }
            btn.getStyleClass().remove("nav-item");
            btn.getStyleClass().add("nav-item-active");
        }
    }

    @FXML private void irAMenuPrincipal() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Stage stage = (Stage) paneNuevaOrden.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setResizable(true);
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.error("Excepcion detectada", e);
            mostrarAlerta("Error de Navegacion", "No se pudo cargar el Menu Principal.");
        }
    }

    @FXML private void onInsumoSeleccionado() {
        String insumo = cbInsumo.getValue();
        if (insumo != null && productosActuales.containsKey(insumo)) {
            ProductoInfo info = productosActuales.get(insumo);
            precioUnitarioActual = info.precio;
            precioBaseOriginal = info.precio;
            unidadBaseActual = info.unidad;
            unidadBaseOriginal = info.unidad;
            List<String> compatibles = UNIDADES_POR_GRUPO.getOrDefault(info.unidad, Arrays.asList(info.unidad));
            cbUnidad.setItems(FXCollections.observableArrayList(compatibles));
            cbUnidad.setValue(info.unidad);
            calcularTotal();
        }
    }

    private void calcularTotal() {
        String insumo = cbInsumo.getValue();
        if (insumo == null || precioUnitarioActual == 0.0) { lblPrecioUnitario.setText("RD$ 0.00"); return; }
        String textoCant = txtCantidad.getText();
        if (textoCant == null || textoCant.trim().isEmpty()) {
            lblPrecioUnitario.setText(String.format("RD$ %.2f", precioUnitarioActual)); return;
        }
        double cantidad = parsearNumero(textoCant);
        if (cantidad <= 0) { lblPrecioUnitario.setText(String.format("RD$ %.2f", precioUnitarioActual)); return; }
        double total = precioUnitarioActual * cantidad;
        lblPrecioUnitario.setText(String.format("RD$ %.2f x %.2f = RD$ %.2f", precioUnitarioActual, cantidad, total));
    }

    @FXML private void generarOrden() {
        if (cbSuplidor.getValue() == null || cbInsumo.getValue() == null || txtCantidad.getText().isEmpty() || cbUnidad.getValue() == null) {
            mostrarAlerta("Campos Vacios", "Por favor complete: Suplidor, Insumo, Cantidad y Unidad."); return;
        }
        String sql = "INSERT INTO tbl_ordenes_compra (suplidor, insumo, cantidad, precio_unitario, fecha_pedido, fecha_entrega_esperada, estado) VALUES (?, ?, ?, ?, GETDATE(), ?, 'Pendiente')";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            double cantidadNum = parsearNumero(txtCantidad.getText());
            String insumoCompleto = cbInsumo.getValue() + " (" + cbUnidad.getValue() + ")";
            ps.setString(1, cbSuplidor.getValue()); ps.setString(2, insumoCompleto);
            ps.setDouble(3, cantidadNum); ps.setDouble(4, precioUnitarioActual);
            ps.setDate(5, Date.valueOf(dpFechaEntrega.getValue()));
            ps.executeUpdate();
            cargarOrdenesHistorial(); limpiarDatosOrden();
            mostrarAlerta("Exito", "Orden de compra generada correctamente.\nTotal: " + lblPrecioUnitario.getText());
        } catch (Exception e) { com.example.pantallas.utils.LoggerUtil.error("Excepcion detectada", e); mostrarAlerta("Error de Base de Datos", "No se pudo generar la orden."); }
    }

    @FXML private void confirmarRecepcion() {
        String seleccion = cbOrdenesPendientes.getValue();
        if (seleccion == null) { mostrarAlerta("Seleccion Requerida", "Debe seleccionar una orden pendiente."); return; }

        boolean entregaCorrecta = chkCantidadCorrecta.isSelected();
        boolean entregaIncorrecta = chkCantidadIncorrecta.isSelected();
        if (!entregaCorrecta && !entregaIncorrecta) {
            mostrarAlerta("Verificacion Requerida", "Debe indicar si la cantidad entregada es correcta o no.");
            return;
        }

        double cantidadRecibida;
        String unidadRecibida = "";
        if (entregaIncorrecta) {
            if (txtCantidadReal.getText().isEmpty()) {
                mostrarAlerta("Campo Requerido", "Debe ingresar la cantidad real recibida.");
                return;
            }
            if (cbUnidadReal.getValue() == null) {
                mostrarAlerta("Campo Requerido", "Debe seleccionar la unidad de la cantidad real recibida.");
                return;
            }
            cantidadRecibida = parsearNumero(txtCantidadReal.getText());
            unidadRecibida = cbUnidadReal.getValue();
        } else {
            String[] partes = seleccion.split(" - ");
            try {
                int tempId = Integer.parseInt(partes[0]);
                cantidadRecibida = obtenerCantidadOrden(tempId);
                unidadRecibida = obtenerUnidadOrden(tempId);
            } catch (Exception e) {
                cantidadRecibida = 0;
            }
            if (cantidadRecibida == 0) { mostrarAlerta("Error", "No se pudo determinar la cantidad de la orden."); return; }
        }

        if (txtNotasRecepcion.getText().isEmpty()) {
            mostrarAlerta("Campo Requerido", "Debe ingresar las observaciones de calidad.");
            return;
        }

        int idOrden = Integer.parseInt(seleccion.split(" - ")[0]);
        String sql = "UPDATE tbl_ordenes_compra SET estado = 'Recibido', notas = ?, cantidad_recibida = ?, fecha_recepcion = GETDATE() WHERE id_orden = ?";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtNotasRecepcion.getText());
            ps.setDouble(2, cantidadRecibida);
            ps.setInt(3, idOrden);
            ps.executeUpdate();
            guardarObservacionCalidad(con, idOrden, entregaCorrecta, cantidadRecibida, unidadRecibida);
            guardarMovimientoInventario(idOrden, cantidadRecibida, unidadRecibida);
            cargarOrdenesHistorial();
            cargarHistorialCalidad();
            cbOrdenesPendientes.getItems().clear();
            txtFiltroRecepcion.clear();
            txtCantidadReal.clear();
            txtNotasRecepcion.clear();
            chkCantidadCorrecta.setSelected(false);
            chkCantidadIncorrecta.setSelected(false);
            hboxCantidadReal.setVisible(false);
            hboxCantidadReal.setManaged(false);
            mostrarAlerta("Almacen", "Mercancia ingresada al sistema.");
        } catch (Exception e) { com.example.pantallas.utils.LoggerUtil.error("Excepcion detectada", e); mostrarAlerta("Error de Base de Datos", "No se pudo registrar la recepcion."); }
    }

    private double obtenerCantidadOrden(int idOrden) {
        String sql = "SELECT cantidad FROM tbl_ordenes_compra WHERE id_orden = ?";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("cantidad");
        } catch (Exception e) { }
        return 0;
    }

    private String obtenerUnidadOrden(int idOrden) {
        String sql = "SELECT insumo FROM tbl_ordenes_compra WHERE id_orden = ?";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String insumo = rs.getString("insumo");
                int idx = insumo.lastIndexOf("(");
                if (idx > 0) return insumo.substring(idx + 1, insumo.length() - 1);
            }
        } catch (Exception e) { }
        return "Unidades";
    }

    private void guardarObservacionCalidad(Connection con, int idOrden, boolean entregaCorrecta, double cantidadRecibida, String unidad) {
        String sqlSelect = "SELECT suplidor, insumo, cantidad FROM tbl_ordenes_compra WHERE id_orden = ?";
        String sqlInsert = "INSERT INTO tbl_historial_calidad (id_orden, suplidor, insumo, cantidad_esperada, cantidad_recibida, unidad, entrega_correcta, observaciones, fecha_recepcion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
        try (PreparedStatement psSel = con.prepareStatement(sqlSelect)) {
            psSel.setInt(1, idOrden);
            ResultSet rs = psSel.executeQuery();
            if (rs.next()) {
                try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
                    psIns.setInt(1, idOrden);
                    psIns.setString(2, rs.getString("suplidor"));
                    psIns.setString(3, rs.getString("insumo"));
                    psIns.setDouble(4, rs.getDouble("cantidad"));
                    psIns.setDouble(5, cantidadRecibida);
                    psIns.setString(6, unidad);
                    psIns.setBoolean(7, entregaCorrecta);
                    psIns.setString(8, txtNotasRecepcion.getText());
                    psIns.executeUpdate();
                }
            }
        } catch (Exception e) { com.example.pantallas.utils.LoggerUtil.error("Excepcion detectada", e); }
    }

    private void guardarMovimientoInventario(int idOrden, double cantidadRecibida, String unidad) {
        asegurarTablaMovimientosInventario();
        String sqlSelect = "SELECT insumo FROM tbl_ordenes_compra WHERE id_orden = ?";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement psSel = con.prepareStatement(sqlSelect)) {
            psSel.setInt(1, idOrden);
            ResultSet rs = psSel.executeQuery();
            if (rs.next()) {
                String insumo = rs.getString("insumo");
                String nombreProducto = insumo;
                int idx = insumo.lastIndexOf(" (");
                if (idx > 0) nombreProducto = insumo.substring(0, idx);

                // Stock update runs FIRST (critical path), logs separately on failure
                actualizarStockEntrada(nombreProducto, cantidadRecibida, unidad.isEmpty() ? "Unidades" : unidad);

                // Then try to register the movement (non-critical)
                try (PreparedStatement psIns = con.prepareStatement(
                    "INSERT INTO tbl_movimientos_inventario (producto, tipo, cantidad, unidad, fecha_movimiento) VALUES (?, 'ENTRADA', ?, ?, GETDATE())")) {
                    psIns.setString(1, insumo);
                    psIns.setDouble(2, cantidadRecibida);
                    psIns.setString(3, unidad.isEmpty() ? "Unidades" : unidad);
                    psIns.executeUpdate();
                } catch (Exception e) {
                    com.example.pantallas.utils.LoggerUtil.error("No se pudo registrar el movimiento de inventario", e);
                }
            }
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.error("Error en guardarMovimientoInventario", e);
            mostrarAlerta("Error", "No se pudo procesar el ingreso al inventario:\n" + e.getMessage());
        }
    }

    private void asegurarTablaMovimientosInventario() {
        String sql = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_movimientos_inventario' AND xtype='U') "
                + "CREATE TABLE tbl_movimientos_inventario ("
                + "id_movimiento INT IDENTITY PRIMARY KEY, "
                + "producto VARCHAR(255) NOT NULL, "
                + "tipo VARCHAR(20) NOT NULL, "
                + "cantidad DECIMAL(18,2) NOT NULL, "
                + "unidad VARCHAR(50), "
                + "fecha_movimiento DATETIME DEFAULT GETDATE(), "
                + "justificacion VARCHAR(500))";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.error("Error creando tbl_movimientos_inventario", e);
        }
    }

    private void actualizarStockEntrada(String producto, double cantidad, String unidad) {
        asegurarTablaInventarioProductos();

        // UPSERT en Productos (tabla maestra de catálogo)
        String sqlProd = "MERGE INTO Productos AS target "
                + "USING (SELECT ? AS nombre) AS source ON target.nombre_producto = source.nombre "
                + "WHEN MATCHED THEN "
                + "    UPDATE SET stock_actual = COALESCE(target.stock_actual, 0) + ? "
                + "WHEN NOT MATCHED THEN "
                + "    INSERT (codigo_producto, nombre_producto, descripcion, categoria, unidad_medida, stock_actual, stock_minimo, precio_venta_base, activo) "
                + "    VALUES (?, ?, '', 'Insumos', ?, ?, 0, 1, 1);";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlProd)) {
            String codigo = "MAT" + String.format("%05d", Math.abs(producto.hashCode() % 100000));
            ps.setString(1, producto);
            ps.setDouble(2, cantidad);
            ps.setString(3, codigo);
            ps.setString(4, producto);
            ps.setString(5, unidad);
            ps.setDouble(6, cantidad);
            ps.executeUpdate();
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.error("Error al actualizar Productos", e);
            mostrarAlerta("Error de Stock", "No se pudo actualizar el stock en Productos:\n" + e.getMessage());
        }

        // UPSERT en tbl_inventario_productos (para inventario)
        String sqlInv = "MERGE INTO tbl_inventario_productos AS target "
                + "USING (SELECT ? AS nombre) AS source ON target.nombre_producto = source.nombre "
                + "WHEN MATCHED THEN "
                + "    UPDATE SET cantidad_stock = COALESCE(CAST(cantidad_stock AS DECIMAL(18,2)), 0) + ? "
                + "WHEN NOT MATCHED THEN "
                + "    INSERT (lote_id, nombre_producto, cantidad_stock, unidad_medida, categoria, fecha_entrada) "
                + "    VALUES (?, ?, ?, ?, ?, GETDATE());";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlInv)) {
            String loteId = "CMP" + String.format("%05d", Math.abs(producto.hashCode() % 100000));
            ps.setString(1, producto);
            ps.setDouble(2, cantidad);
            ps.setString(3, loteId);
            ps.setString(4, producto);  // nombre_producto
            ps.setDouble(5, cantidad);  // cantidad_stock
            ps.setString(6, unidad);    // unidad_medida
            ps.setString(7, "Insumos"); // categoria
            ps.executeUpdate();
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.error("Error al actualizar tbl_inventario_productos", e);
            mostrarAlerta("Error de Stock", "No se pudo actualizar el inventario:\n" + e.getMessage());
        }
    }

    private void migrarNombresProductos() {
        String[][] pares = {{"Cuajo Liquido", "Cuajo Líquido"}};
        for (String[] p : pares) {
            for (String tabla : new String[]{"Productos", "tbl_inventario_productos"}) {
                try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                         "UPDATE " + tabla + " SET nombre_producto = ? WHERE nombre_producto = ?")) {
                    ps.setString(1, p[1]);
                    ps.setString(2, p[0]);
                    ps.executeUpdate();
                } catch (Exception ignored) { }
            }
            // Also fix old order records so receipt confirmation extracts the correct name
            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "UPDATE tbl_ordenes_compra SET insumo = REPLACE(insumo, ?, ?) WHERE insumo LIKE ?")) {
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, "%" + p[0] + "%");
                ps.executeUpdate();
            } catch (Exception ignored) { }
        }
    }

    private void asegurarTablaInventarioProductos() {
        String sql = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_inventario_productos' AND xtype='U') "
                + "CREATE TABLE tbl_inventario_productos ("
                + "id INT IDENTITY PRIMARY KEY, "
                + "lote_id VARCHAR(50), "
                + "nombre_producto VARCHAR(200), "
                + "cantidad_stock DECIMAL(18,2) DEFAULT 0, "
                + "cantidad_disponible DECIMAL(18,2) DEFAULT 0, "
                + "unidad_medida VARCHAR(50), "
                + "categoria VARCHAR(100), "
                + "tipo_queso VARCHAR(200), "
                + "ultimo_costo DECIMAL(18,2) DEFAULT 0, "
                + "cantidad_minima DECIMAL(18,2) DEFAULT 0, "
                + "fecha_entrada DATETIME DEFAULT GETDATE())";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (Exception e) { }

        // Add missing columns if table already existed with old schema
        String[][] columnas = {
            {"nombre_producto", "VARCHAR(200)"},
            {"cantidad_stock", "DECIMAL(18,2) DEFAULT 0"},
            {"cantidad_disponible", "DECIMAL(18,2) DEFAULT 0"},
            {"unidad_medida", "VARCHAR(50)"},
            {"categoria", "VARCHAR(100)"},
            {"tipo_queso", "VARCHAR(200)"},
            {"ultimo_costo", "DECIMAL(18,2) DEFAULT 0"},
            {"cantidad_minima", "DECIMAL(18,2) DEFAULT 0"},
            {"fecha_entrada", "DATETIME DEFAULT GETDATE()"}
        };
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement()) {
            for (String[] col : columnas) {
                try {
                    st.executeUpdate("ALTER TABLE tbl_inventario_productos ADD " + col[0] + " " + col[1]);
                } catch (Exception ignored) { }
            }
        } catch (Exception e) { }
    }

    @FXML private void actualizarEstadoMasivo() {
        OrdenCompra seleccionada = tablaHistorial.getSelectionModel().getSelectedItem();
        if (seleccionada == null) { mostrarAlerta("Seleccion Requerida", "Debe seleccionar una orden de la tabla."); return; }
        if (cbNuevoEstado.getValue() == null) { mostrarAlerta("Estado Requerido", "Debe seleccionar un nuevo estado."); return; }
        String sql = "UPDATE tbl_ordenes_compra SET estado = ? WHERE id_orden = ?";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cbNuevoEstado.getValue()); ps.setInt(2, seleccionada.getId());
            ps.executeUpdate(); cargarOrdenesHistorial(); mostrarAlerta("Exito", "Estado actualizado.");
        } catch (Exception e) { com.example.pantallas.utils.LoggerUtil.error("Excepcion detectada", e); }
    }

    private void cargarOrdenesHistorial() {
        listaOrdenes.clear();
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM tbl_ordenes_compra ORDER BY id_orden DESC")) {
            while (rs.next()) {
                listaOrdenes.add(new OrdenCompra(rs.getInt("id_orden"), rs.getString("suplidor"),
                        rs.getString("insumo"), rs.getDouble("cantidad"), rs.getString("estado"), rs.getDouble("precio_unitario")));
            }
            ObservableList<OrdenCompra> soloPendientes = FXCollections.observableArrayList();
            for (OrdenCompra oc : listaOrdenes) {
                if ("Pendiente".equals(oc.getEstado())) {
                    soloPendientes.add(oc);
                }
            }
            tablaOrdenesPendientes.setItems(soloPendientes);
            aplicarFiltroEstado();
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepcion detectada", e); }
    }

    private void aplicarFiltroEstado() {
        boolean mostrarPendiente = chkFiltrarPendiente.isSelected();
        boolean mostrarRecibido = chkFiltrarRecibido.isSelected();
        boolean mostrarCancelado = chkFiltrarCancelado.isSelected();

        ObservableList<OrdenCompra> filtradas = FXCollections.observableArrayList();
        for (OrdenCompra oc : listaOrdenes) {
            String est = oc.getEstado();
            if (("Pendiente".equals(est) && mostrarPendiente) ||
                ("Recibido".equals(est) && mostrarRecibido) ||
                ("Cancelado".equals(est) && mostrarCancelado)) {
                filtradas.add(oc);
            }
        }
        tablaHistorial.setItems(filtradas);
    }

    private void filtrarOrdenesPendientes() {
        String filtro = txtFiltroRecepcion.getText().toLowerCase().trim();
        listaPendientesFiltradas.clear();
        if (filtro.isEmpty()) {
            listaPendientesFiltradas.addAll(listaPendientes);
        } else {
            for (String item : listaPendientes) {
                if (item.toLowerCase().contains(filtro)) {
                    listaPendientesFiltradas.add(item);
                }
            }
        }
        cbOrdenesPendientes.setItems(listaPendientesFiltradas);
        if (!listaPendientesFiltradas.isEmpty()) {
            cbOrdenesPendientes.getSelectionModel().selectFirst();
        }
    }

    private void cargarOrdenesPendientes() {
        listaPendientes.clear();
        listaPendientesFiltradas.clear();
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_orden, suplidor, insumo, cantidad FROM tbl_ordenes_compra WHERE estado = 'Pendiente' ORDER BY id_orden DESC")) {
            while (rs.next()) {
                listaPendientes.add(rs.getInt("id_orden") + " - " + rs.getString("suplidor") + " (" + rs.getString("insumo") + ")");
            }
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepcion detectada", e); }
        // Fallback si no hay ordenes pendientes en BD
        if (listaPendientes.isEmpty()) {
            String[][] fallback = {
                {"1", "Lacteos del Yaque", "Crema de Leche (Litros)"},
                {"2", "Insumos RD", "Sal Industrial (Kilos)"},
                {"3", "Empaques Cibao", "Fundas al Vacio (Rollos)"}
            };
            for (String[] fb : fallback) {
                listaPendientes.add(fb[0] + " - " + fb[1] + " (" + fb[2] + ")");
            }
        }
        listaPendientesFiltradas.addAll(listaPendientes);
        cbOrdenesPendientes.setItems(listaPendientesFiltradas);
        if (!listaPendientesFiltradas.isEmpty()) {
            cbOrdenesPendientes.getSelectionModel().selectFirst();
        }
    }

    private void cargarHistorialCalidad() {
        listaObservaciones.clear();
        String sql = "SELECT id, id_orden, suplidor, insumo, cantidad_esperada, cantidad_recibida, unidad, entrega_correcta, observaciones, fecha_recepcion "
                + "FROM tbl_historial_calidad ORDER BY fecha_recepcion DESC";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                listaObservaciones.add(new ObservacionCalidad(
                        rs.getInt("id"),
                        rs.getInt("id_orden"),
                        rs.getString("suplidor"),
                        rs.getString("insumo"),
                        rs.getDouble("cantidad_esperada"),
                        rs.getDouble("cantidad_recibida"),
                        rs.getString("unidad"),
                        rs.getBoolean("entrega_correcta"),
                        rs.getString("observaciones"),
                        rs.getString("fecha_recepcion")
                ));
            }
            if (tablaHistorialCalidad != null)
                tablaHistorialCalidad.setItems(listaObservaciones);
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepcion detectada", e); }
    }

    private void configurarTablas() {
        // Columnas para tablaHistorial (independientes)
        TableColumn<OrdenCompra, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<OrdenCompra, String> colSup = new TableColumn<>("Suplidor");
        colSup.setCellValueFactory(new PropertyValueFactory<>("suplidor"));
        TableColumn<OrdenCompra, String> colIns = new TableColumn<>("Insumo");
        colIns.setCellValueFactory(new PropertyValueFactory<>("insumo"));
        TableColumn<OrdenCompra, Double> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<OrdenCompra, Double> colPre = new TableColumn<>("Precio U.");
        colPre.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        TableColumn<OrdenCompra, String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablaHistorial.getColumns().setAll(colId, colSup, colIns, colCant, colPre, colEst);

        // Columnas independientes para tablaOrdenesPendientes (no compartir con tablaHistorial)
        TableColumn<OrdenCompra, Integer> colId2 = new TableColumn<>("ID");
        colId2.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<OrdenCompra, String> colSup2 = new TableColumn<>("Suplidor");
        colSup2.setCellValueFactory(new PropertyValueFactory<>("suplidor"));
        TableColumn<OrdenCompra, String> colIns2 = new TableColumn<>("Insumo");
        colIns2.setCellValueFactory(new PropertyValueFactory<>("insumo"));
        TableColumn<OrdenCompra, Double> colCant2 = new TableColumn<>("Cant.");
        colCant2.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        tablaOrdenesPendientes.getColumns().setAll(colId2, colSup2, colIns2, colCant2);

        // Tabla de historial de calidad
        TableColumn<ObservacionCalidad, Integer> colOcId = new TableColumn<>("ID");
        colOcId.setCellValueFactory(new PropertyValueFactory<>("idOrden"));
        TableColumn<ObservacionCalidad, String> colOcSup = new TableColumn<>("Suplidor");
        colOcSup.setCellValueFactory(new PropertyValueFactory<>("suplidor"));
        TableColumn<ObservacionCalidad, String> colOcIns = new TableColumn<>("Insumo");
        colOcIns.setCellValueFactory(new PropertyValueFactory<>("insumo"));
        TableColumn<ObservacionCalidad, Double> colOcEsp = new TableColumn<>("Esperado");
        colOcEsp.setCellValueFactory(new PropertyValueFactory<>("cantidadEsperada"));
        TableColumn<ObservacionCalidad, Double> colOcRec = new TableColumn<>("Recibido");
        colOcRec.setCellValueFactory(new PropertyValueFactory<>("cantidadRecibida"));
        TableColumn<ObservacionCalidad, String> colOcEst = new TableColumn<>("Entrega");
        colOcEst.setCellValueFactory(new PropertyValueFactory<>("entregaTexto"));
        TableColumn<ObservacionCalidad, String> colOcObs = new TableColumn<>("Observaciones");
        colOcObs.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        TableColumn<ObservacionCalidad, String> colOcFec = new TableColumn<>("Fecha");
        colOcFec.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        if (tablaHistorialCalidad != null)
            tablaHistorialCalidad.getColumns().setAll(colOcId, colOcSup, colOcIns, colOcEsp, colOcRec, colOcEst, colOcObs, colOcFec);

        // Tabla de proveedores
        if (colProvId != null && colProvNombre != null) {
            colProvId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colProvNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            if (colProvRnc != null) colProvRnc.setCellValueFactory(new PropertyValueFactory<>("rnc"));
            if (colProvTel != null) colProvTel.setCellValueFactory(new PropertyValueFactory<>("telefono"));
            if (colProvDir != null) colProvDir.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        }
    }

    private void cargarCombos() {
        ObservableList<String> suplidores = FXCollections.observableArrayList();
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT nombre FROM tbl_proveedores WHERE activo = 1 ORDER BY nombre")) {
            while (rs.next()) suplidores.add(rs.getString("nombre"));
        } catch (Exception e) { }
        if (suplidores.isEmpty()) {
            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT nombre FROM tbl_suplidores")) {
                while (rs.next()) suplidores.add(rs.getString("nombre"));
            } catch (Exception e2) { }
        }
        // Merge con fallback en memoria
        for (Proveedor p : listaProveedoresFallback) {
            if (!suplidores.contains(p.getNombre())) suplidores.add(p.getNombre());
        }
        // Hardcoded fallback names (siempre disponibles)
        for (String nombre : new String[]{"Lacteos del Yaque", "Insumos RD", "Empaques Cibao", "Ganaderia del Norte"}) {
            if (!suplidores.contains(nombre)) suplidores.add(nombre);
        }
        cbSuplidor.setItems(suplidores);
        cbNuevoEstado.setItems(FXCollections.observableArrayList("Pendiente", "Recibido", "Cancelado", "En Transito"));
        cbUnidad.setItems(FXCollections.observableArrayList("Paquetes", "Unidades", "Litros", "Galones", "Kilos", "Libras", "Rollos"));
        if (cbUnidadReal != null) cbUnidadReal.setItems(FXCollections.observableArrayList("Paquetes", "Unidades", "Litros", "Galones", "Kilos", "Libras", "Rollos"));
    }

    @FXML private void limpiarDatosOrden() {
        txtCantidad.clear();
        if (lblPrecioUnitario != null) lblPrecioUnitario.setText("RD$ 0.00");
        cbSuplidor.getSelectionModel().clearSelection(); cbInsumo.getSelectionModel().clearSelection();
        cbUnidad.getSelectionModel().clearSelection(); dpFechaEntrega.setValue(LocalDate.now().plusDays(3));
        precioUnitarioActual = 0.0; unidadBaseActual = "";
        precioBaseOriginal = 0.0; unidadBaseOriginal = "";
    }

    @FXML private void borrarRegistroHistorial() {
        OrdenCompra seleccionada = tablaHistorial.getSelectionModel().getSelectedItem();
        if (seleccionada == null) { mostrarAlerta("Seleccion Requerida", "Debe seleccionar una orden."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminacion"); confirm.setHeaderText(null);
        confirm.setContentText("Esta seguro que desea eliminar esta orden?");
        confirm.initModality(Modality.APPLICATION_MODAL);
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM tbl_ordenes_compra WHERE id_orden = ?")) {
                ps.setInt(1, seleccionada.getId()); ps.executeUpdate();
                cargarOrdenesHistorial(); mostrarAlerta("Eliminado", "Registro borrado correctamente.");
            } catch (Exception e) { com.example.pantallas.utils.LoggerUtil.error("Excepcion detectada", e); }
        }
    }

    // ========== CRUD PROVEEDORES ==========

    private void cargarProveedores() {
        listaProveedores.clear();
        boolean dbOk = false;
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT provider_id, nombre, rnc, telefono, direccion FROM tbl_proveedores WHERE activo = 1 ORDER BY provider_id")) {
            int seq = 1;
            while (rs.next()) {
                int dbId = rs.getInt("provider_id");
                listaProveedores.add(new Proveedor(seq++, dbId,
                    rs.getString("nombre") != null ? rs.getString("nombre") : "",
                    rs.getString("rnc") != null ? rs.getString("rnc") : "",
                    rs.getString("telefono") != null ? rs.getString("telefono") : "",
                    rs.getString("direccion") != null ? rs.getString("direccion") : ""));
            }
            if (!listaProveedores.isEmpty()) {
                listaProveedoresFallback.setAll(listaProveedores);
                if (tablaProveedores != null) tablaProveedores.setItems(listaProveedores);
                dbOk = true;
            }
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepcion detectada", e); }
        if (!dbOk) {
            if (listaProveedoresFallback.isEmpty()) {
                listaProveedoresFallback.addAll(
                    new Proveedor(1, 1, "Lacteos del Yaque", "001-0000001-1", "809-555-0101", "Av. Principal, Santo Domingo"),
                    new Proveedor(2, 2, "Insumos RD", "001-0000002-2", "809-555-0102", "Calle Secundaria, Santiago"),
                    new Proveedor(3, 3, "Empaques Cibao", "001-0000003-3", "809-555-0103", "Zona Industrial, La Vega"),
                    new Proveedor(4, 4, "Ganaderia del Norte", "001-0000004-4", "809-555-0104", "Carretera Duarte, Puerto Plata"));
            }
            if (tablaProveedores != null) tablaProveedores.setItems(listaProveedoresFallback);
        }
    }

    @FXML private void agregarProveedor() {
        if (txtProvNombre.getText() == null || txtProvNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Campo Obligatorio", "Debe ingresar el nombre del proveedor.");
            return;
        }
        if (txtProvTelefono.getText() == null || txtProvTelefono.getText().trim().isEmpty()) {
            mostrarAlerta("Campo Obligatorio", "El numero telefonico es obligatorio para registrar un proveedor.");
            return;
        }
        if (txtProvDireccion.getText() == null || txtProvDireccion.getText().trim().isEmpty()) {
            mostrarAlerta("Campo Obligatorio", "La direccion es obligatoria para registrar un proveedor.");
            return;
        }
        String nombre = txtProvNombre.getText().trim();
        String rnc = (txtProvRnc != null && txtProvRnc.getText() != null) ? txtProvRnc.getText().trim() : "";
        String telefono = txtProvTelefono.getText().trim();
        String direccion = txtProvDireccion.getText().trim();
        boolean dbOk = ejecutarSQLBool("INSERT INTO tbl_proveedores (nombre, rnc, telefono, direccion) VALUES (?, ?, ?, ?)",
                nombre, rnc, telefono, direccion);
        int nextId = listaProveedoresFallback.isEmpty() ? 1 :
            listaProveedoresFallback.stream().mapToInt(Proveedor::getId).max().orElse(0) + 1;
        int nextDbId = listaProveedoresFallback.isEmpty() ? 1 :
            listaProveedoresFallback.stream().mapToInt(Proveedor::getDbId).max().orElse(0) + 1;
        listaProveedoresFallback.add(new Proveedor(nextId, nextDbId, nombre, rnc, telefono, direccion));
        mostrarAlerta("Exito", "Proveedor registrado correctamente.");
        limpiarProveedor();
    }

    private boolean ejecutarSQLBool(String sql, Object... params) {
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            com.example.pantallas.utils.LoggerUtil.error("Excepcion detectada", e);
            return false;
        }
    }

    @FXML private void actualizarProveedor() {
        if (idProveedorSeleccionado == -1) {
            mostrarAlerta("Seleccion Requerida", "Debe seleccionar un proveedor de la tabla para actualizar.");
            return;
        }
        if (txtProvNombre.getText() == null || txtProvNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Campo Obligatorio", "Debe ingresar el nombre del proveedor.");
            return;
        }
        if (txtProvTelefono.getText() == null || txtProvTelefono.getText().trim().isEmpty()) {
            mostrarAlerta("Campo Obligatorio", "El numero telefonico es obligatorio.");
            return;
        }
        if (txtProvDireccion.getText() == null || txtProvDireccion.getText().trim().isEmpty()) {
            mostrarAlerta("Campo Obligatorio", "La direccion es obligatoria.");
            return;
        }
        String nombre = txtProvNombre.getText().trim();
        String rnc = (txtProvRnc != null && txtProvRnc.getText() != null) ? txtProvRnc.getText().trim() : "";
        String telefono = txtProvTelefono.getText().trim();
        String direccion = txtProvDireccion.getText().trim();
        ejecutarSQL("UPDATE tbl_proveedores SET nombre = ?, rnc = ?, telefono = ?, direccion = ? WHERE provider_id = ?",
                nombre, rnc, telefono, direccion, idProveedorSeleccionado);
        Proveedor seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        int displayId = (seleccionado != null) ? seleccionado.getId() : idProveedorSeleccionado;
        for (int i = 0; i < listaProveedoresFallback.size(); i++) {
            if (listaProveedoresFallback.get(i).getDbId() == idProveedorSeleccionado) {
                listaProveedoresFallback.set(i, new Proveedor(displayId, idProveedorSeleccionado, nombre, rnc, telefono, direccion));
                break;
            }
        }
        mostrarAlerta("Exito", "Proveedor actualizado correctamente.");
        limpiarProveedor();
    }

    @FXML private void borrarProveedor() {
        Proveedor p = tablaProveedores.getSelectionModel().getSelectedItem();
        if (p == null) {
            mostrarAlerta("Seleccion Requerida", "Debe seleccionar un proveedor de la tabla para eliminar.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminacion");
        confirm.setHeaderText(null);
        confirm.setContentText("Esta seguro que desea eliminar este proveedor?");
        confirm.initModality(Modality.APPLICATION_MODAL);
        if (confirm.showAndWait().get() == ButtonType.OK) {
            ejecutarSQL("DELETE FROM tbl_proveedores WHERE provider_id = ?", idProveedorSeleccionado);
            listaProveedoresFallback.removeIf(prov -> prov.getDbId() == idProveedorSeleccionado);
            mostrarAlerta("Eliminado", "Proveedor eliminado correctamente.");
            limpiarProveedor();
        }
    }

    @FXML private void limpiarProveedor() {
        txtProvNombre.clear();
        txtProvTelefono.clear();
        if (txtProvRnc != null) txtProvRnc.clear();
        if (txtProvDireccion != null) txtProvDireccion.clear();
        idProveedorSeleccionado = -1;
        tablaProveedores.getSelectionModel().clearSelection();
        cargarProveedores();
        cargarCombos();
    }

    private void ejecutarSQL(String sql, Object... params) {
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepcion detectada", e); mostrarAlerta("Error de Base de Datos", e.getMessage()); }
    }

    private double parsearNumero(String texto) {
        try { return Double.parseDouble(texto.replaceAll("[^0-9.]", "")); } catch (Exception e) { return 0.0; }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje); a.showAndWait();
    }

    // ========== CLASES MODELO ==========

    public static class OrdenCompra {
        private int id; private String suplidor, insumo, estado; private double cantidad, precioUnitario;
        public OrdenCompra(int id, String sup, String ins, double cant, String est, double precio) {
            this.id = id; this.suplidor = sup; this.insumo = ins; this.cantidad = cant; this.estado = est; this.precioUnitario = precio;
        }
        public int getId() { return id; }
        public String getSuplidor() { return suplidor; }
        public String getInsumo() { return insumo; }
        public double getCantidad() { return cantidad; }
        public String getEstado() { return estado; }
        public double getPrecioUnitario() { return precioUnitario; }
    }

    public static class Proveedor {
        private int id, dbId;
        private String nombre, rnc, telefono, direccion;
        public Proveedor(int id, String n, String rnc, String t, String d) {
            this.id = id; this.dbId = id; this.nombre = n; this.rnc = rnc; this.telefono = t; this.direccion = d;
        }
        public Proveedor(int id, int dbId, String n, String rnc, String t, String d) {
            this.id = id; this.dbId = dbId; this.nombre = n; this.rnc = rnc; this.telefono = t; this.direccion = d;
        }
        public int getId() { return id; }
        public int getDbId() { return dbId; }
        public String getNombre() { return nombre; }
        public String getRnc() { return rnc; }
        public String getTelefono() { return telefono; }
        public String getDireccion() { return direccion; }
    }

    public static class ObservacionCalidad {
        private int id, idOrden;
        private String suplidor, insumo, unidad, observaciones, fecha;
        private double cantidadEsperada, cantidadRecibida;
        private boolean entregaCorrecta;

        public ObservacionCalidad(int id, int idOrden, String sup, String ins, double esp, double rec, String und, boolean correcta, String obs, String fec) {
            this.id = id; this.idOrden = idOrden; this.suplidor = sup; this.insumo = ins;
            this.cantidadEsperada = esp; this.cantidadRecibida = rec; this.unidad = und;
            this.entregaCorrecta = correcta; this.observaciones = obs; this.fecha = fec;
        }

        public int getId() { return id; }
        public int getIdOrden() { return idOrden; }
        public String getSuplidor() { return suplidor; }
        public String getInsumo() { return insumo; }
        public double getCantidadEsperada() { return cantidadEsperada; }
        public double getCantidadRecibida() { return cantidadRecibida; }
        public String getUnidad() { return unidad; }
        public boolean isEntregaCorrecta() { return entregaCorrecta; }
        public String getEntregaTexto() { return entregaCorrecta ? "Correcta" : "Incumplida"; }
        public String getObservaciones() { return observaciones; }
        public String getFecha() { return fecha; }
    }
}
