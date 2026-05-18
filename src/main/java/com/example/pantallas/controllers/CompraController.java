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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CompraController {

    // Proveedores panel ELIMINADO — ahora vive en Distribución
    @FXML private VBox paneNuevaOrden, paneRecepcion, paneHistorial;
    @FXML private ComboBox<String> cbSuplidor, cbInsumo, cbOrdenesPendientes, cbNuevoEstado, cbUnidad;
    @FXML private TextField txtCantidad, txtNoDocumento, txtCantidadRecibida;
    @FXML private DatePicker dpFechaEntrega;
    @FXML private TextArea txtNotasRecepcion;
    @FXML private TableView<OrdenCompra> tablaOrdenesPendientes, tablaHistorial;
    @FXML private Label lblPrecioUnitario;
    @FXML private Button btnNavOrden, btnNavRecepcion, btnNavHistorial;

    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;trustServerCertificate=true;encrypt=false;";
        
    private final ObservableList<OrdenCompra> listaOrdenes = FXCollections.observableArrayList();
    private final ObservableList<String> listaPendientes = FXCollections.observableArrayList();
    private Map<String, ProductoInfo> productosActuales = new HashMap<>();
    private double precioUnitarioActual = 0.0;

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
        PRODUCTOS_FALLBACK.put("Lácteos del Yaque", lacteos);

        Map<String, ProductoInfo> insumos = new HashMap<>();
        insumos.put("Sal Industrial", new ProductoInfo(45.00, "Kilos"));
        insumos.put("Cuajo Líquido", new ProductoInfo(2500.00, "Litros"));
        insumos.put("Cuajo en Polvo", new ProductoInfo(3200.00, "Paquetes"));
        insumos.put("Fundas para Queso", new ProductoInfo(120.00, "Paquetes"));
        insumos.put("Colorante Alimenticio", new ProductoInfo(850.00, "Unidades"));
        insumos.put("Antioxidante", new ProductoInfo(650.00, "Kilos"));
        insumos.put("Cultivo Láctico", new ProductoInfo(2400.00, "Unidades"));
        insumos.put("Cloruro de Calcio", new ProductoInfo(850.00, "Kilos"));
        PRODUCTOS_FALLBACK.put("Insumos RD", insumos);

        Map<String, ProductoInfo> empaques = new HashMap<>();
        empaques.put("Fundas al Vacío", new ProductoInfo(450.00, "Rollos"));
        empaques.put("Cajas de Cartón", new ProductoInfo(75.00, "Unidades"));
        empaques.put("Etiquetas Adhesivas", new ProductoInfo(1200.00, "Rollos"));
        empaques.put("Film Plástico", new ProductoInfo(380.00, "Rollos"));
        PRODUCTOS_FALLBACK.put("Empaques Cibao", empaques);

        Map<String, ProductoInfo> ganaderia = new HashMap<>();
        ganaderia.put("Leche Cruda", new ProductoInfo(42.00, "Litros"));
        ganaderia.put("Crema de Leche", new ProductoInfo(175.00, "Litros"));
        PRODUCTOS_FALLBACK.put("Ganadería del Norte", ganaderia);
    }

    @FXML
    public void initialize() {
        FabricaBase.asegurarEsquemaProveedores();
        asegurarTablaProductos();
        FabricaBase.seedFactoresConversion();
        configurarTablas();
        cargarCombos();
        dpFechaEntrega.setValue(LocalDate.now().plusDays(3));
        cargarOrdenesHistorial();

        cbSuplidor.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) { cargarProductosPorSuplidor(newVal); limpiarCamposProducto(); }
        });
        cbInsumo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) onInsumoSeleccionado();
        });
        txtCantidad.textProperty().addListener((obs, oldVal, newVal) -> calcularTotal());
    }

    private void limpiarCamposProducto() {
        cbInsumo.getSelectionModel().clearSelection();
        txtCantidad.clear();
        lblPrecioUnitario.setText("RD$ 0.00");
        precioUnitarioActual = 0.0;
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
        String sql = "INSERT INTO tbl_suplidor_productos (id_suplidor, producto, precio_unitario, unidad) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Map.Entry<String, Map<String, ProductoInfo>> entry : PRODUCTOS_FALLBACK.entrySet()) {
                Integer idSup = getIdSuplidor(con, entry.getKey());
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
        // Intentar primero desde tbl_proveedores (nueva), luego legacy tbl_suplidores
        try (PreparedStatement ps = con.prepareStatement("SELECT provider_id FROM tbl_proveedores WHERE nombre = ?")) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("provider_id");
        } catch (Exception e) { /* fallback below */ }
        try (PreparedStatement ps = con.prepareStatement("SELECT id_suplidor FROM tbl_suplidores WHERE nombre = ?")) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_suplidor");
        } catch (Exception e) { /* ignore */ }
        return null;
    }

    /**
     * Carga productos por suplidor — intenta primero tbl_factores_conversion, luego fallback.
     */
    private void cargarProductosPorSuplidor(String suplidorNombre) {
        productosActuales.clear();
        cbInsumo.getItems().clear();
        boolean cargado = false;

        // Intentar desde tbl_factores_conversion (normalizado)
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
                cbInsumo.getItems().add(prod);
            }
            if (!productosActuales.isEmpty()) cargado = true;
        } catch (Exception e) { com.example.pantallas.utils.LoggerUtil.info("Error cargando factores_conversion: " + e.getMessage()); }

        // Fallback: tbl_suplidor_productos legacy
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
                    cbInsumo.getItems().add(prod);
                }
                if (!productosActuales.isEmpty()) cargado = true;
            } catch (Exception e) { /* ignore */ }
        }

        // Last fallback: hardcoded
        if (!cargado && PRODUCTOS_FALLBACK.containsKey(suplidorNombre)) {
            Map<String, ProductoInfo> fallback = PRODUCTOS_FALLBACK.get(suplidorNombre);
            for (Map.Entry<String, ProductoInfo> entry : fallback.entrySet()) {
                productosActuales.put(entry.getKey(), entry.getValue());
                cbInsumo.getItems().add(entry.getKey());
            }
        }
    }

    private void resetVistas() {
        paneNuevaOrden.setVisible(false); paneNuevaOrden.setManaged(false);
        paneRecepcion.setVisible(false); paneRecepcion.setManaged(false);
        paneHistorial.setVisible(false); paneHistorial.setManaged(false);
    }

    @FXML private void mostrarNuevaOrden() { alternarVista(paneNuevaOrden, btnNavOrden); }
    @FXML private void mostrarRecepcion() { alternarVista(paneRecepcion, btnNavRecepcion); cargarOrdenesPendientes(); }
    @FXML private void mostrarHistorial() { alternarVista(paneHistorial, btnNavHistorial); }

    private void alternarVista(VBox pane, Button btn) {
        resetVistas();
        if (pane != null) { pane.setVisible(true); pane.setManaged(true); }
        if (btn != null) {
            Button[] btns = {btnNavOrden, btnNavRecepcion, btnNavHistorial};
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) paneNuevaOrden.getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.setMaximized(wasMaximized);
            if (!wasMaximized) stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e);
            mostrarAlerta("Error de Navegación", "No se pudo cargar el Menú Principal.");
        }
    }

    @FXML private void onInsumoSeleccionado() {
        String insumo = cbInsumo.getValue();
        if (insumo != null && productosActuales.containsKey(insumo)) {
            ProductoInfo info = productosActuales.get(insumo);
            precioUnitarioActual = info.precio;
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
            mostrarAlerta("Campos Vacíos", "Por favor complete: Suplidor, Insumo, Cantidad y Unidad."); return;
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
            mostrarAlerta("Éxito", "Orden de compra generada correctamente.\nTotal: " + lblPrecioUnitario.getText());
        } catch (Exception e) { com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e); mostrarAlerta("Error de Base de Datos", "No se pudo generar la orden."); }
    }

    @FXML private void confirmarRecepcion() {
        String seleccion = cbOrdenesPendientes.getValue();
        if (seleccion == null) { mostrarAlerta("Selección Requerida", "Debe seleccionar una orden pendiente."); return; }
        if (txtCantidadRecibida.getText().isEmpty() || txtNotasRecepcion.getText().isEmpty()) {
            mostrarAlerta("Campos Vacíos", "Debe ingresar: Cantidad Real Recibida y Observaciones de Calidad."); return;
        }
        int idOrden = Integer.parseInt(seleccion.split(" - ")[0]);
        String sql = "UPDATE tbl_ordenes_compra SET estado = 'Recibido', notas = ?, cantidad_recibida = ?, fecha_recepcion = GETDATE() WHERE id_orden = ?";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtNotasRecepcion.getText());
            ps.setDouble(2, parsearNumero(txtCantidadRecibida.getText()));
            ps.setInt(3, idOrden);
            ps.executeUpdate();
            guardarMovimientoInventario(idOrden);
            cargarOrdenesHistorial();
            cbOrdenesPendientes.getItems().clear(); txtCantidadRecibida.clear(); txtNotasRecepcion.clear();
            mostrarAlerta("Almacén", "Mercancía ingresada al sistema.");
        } catch (Exception e) { com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e); mostrarAlerta("Error de Base de Datos", "No se pudo registrar la recepción."); }
    }

    private void guardarMovimientoInventario(int idOrden) {
        String sqlSelect = "SELECT insumo, cantidad_recibida FROM tbl_ordenes_compra WHERE id_orden = ?";
        String sqlInsert = "INSERT INTO tbl_movimientos_inventario (producto, tipo, cantidad, unidad, fecha_movimiento) VALUES (?, 'ENTRADA', ?, ?, GETDATE())";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement psSel = con.prepareStatement(sqlSelect)) {
            psSel.setInt(1, idOrden);
            ResultSet rs = psSel.executeQuery();
            if (rs.next()) {
                String insumo = rs.getString("insumo");
                double cantidad = rs.getDouble("cantidad_recibida");
                String unidad = cbUnidad.getValue() != null ? cbUnidad.getValue() : "Unidades";
                try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
                    psIns.setString(1, insumo); psIns.setDouble(2, cantidad); psIns.setString(3, unidad);
                    psIns.executeUpdate();
                }
            }
        } catch (Exception e) { com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e); }
    }

    @FXML private void actualizarEstadoMasivo() {
        OrdenCompra seleccionada = tablaHistorial.getSelectionModel().getSelectedItem();
        if (seleccionada == null) { mostrarAlerta("Selección Requerida", "Debe seleccionar una orden de la tabla."); return; }
        if (cbNuevoEstado.getValue() == null) { mostrarAlerta("Estado Requerido", "Debe seleccionar un nuevo estado."); return; }
        String sql = "UPDATE tbl_ordenes_compra SET estado = ? WHERE id_orden = ?";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cbNuevoEstado.getValue()); ps.setInt(2, seleccionada.getId());
            ps.executeUpdate(); cargarOrdenesHistorial(); mostrarAlerta("Éxito", "Estado actualizado.");
        } catch (Exception e) { com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e); }
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
            tablaHistorial.setItems(listaOrdenes);
            tablaOrdenesPendientes.setItems(listaOrdenes);
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e); }
    }

    private void cargarOrdenesPendientes() {
        listaPendientes.clear();
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_orden, suplidor, insumo, cantidad FROM tbl_ordenes_compra WHERE estado = 'Pendiente' ORDER BY id_orden DESC")) {
            while (rs.next()) {
                listaPendientes.add(rs.getInt("id_orden") + " - " + rs.getString("suplidor") + " (" + rs.getString("insumo") + ")");
            }
            cbOrdenesPendientes.setItems(listaPendientes);
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e); }
    }

    private void configurarTablas() {
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
        tablaOrdenesPendientes.getColumns().setAll(colId, colSup, colIns, colCant);
    }

    private void cargarCombos() {
        cbSuplidor.getItems().clear();
        // Cargar desde tbl_proveedores (nueva tabla unificada)
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT nombre FROM tbl_proveedores WHERE activo = 1 ORDER BY nombre")) {
            while (rs.next()) cbSuplidor.getItems().add(rs.getString("nombre"));
        } catch (Exception e) {
            // Fallback: intentar tbl_suplidores legacy
            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT nombre FROM tbl_suplidores")) {
                while (rs.next()) cbSuplidor.getItems().add(rs.getString("nombre"));
            } catch (Exception e2) {
                cbSuplidor.setItems(FXCollections.observableArrayList("Lácteos del Yaque", "Insumos RD", "Empaques Cibao", "Ganadería del Norte"));
            }
        }
        cbNuevoEstado.setItems(FXCollections.observableArrayList("Pendiente", "Recibido", "Cancelado", "En Tránsito"));
        cbUnidad.setItems(FXCollections.observableArrayList("Paquetes", "Unidades", "Litros", "Galones", "Kilos", "Libras", "Rollos"));
    }

    @FXML private void limpiarDatosOrden() {
        txtCantidad.clear(); lblPrecioUnitario.setText("RD$ 0.00");
        cbSuplidor.getSelectionModel().clearSelection(); cbInsumo.getSelectionModel().clearSelection();
        cbUnidad.getSelectionModel().clearSelection(); dpFechaEntrega.setValue(LocalDate.now().plusDays(3));
    }

    @FXML private void borrarRegistroHistorial() {
        OrdenCompra seleccionada = tablaHistorial.getSelectionModel().getSelectedItem();
        if (seleccionada == null) { mostrarAlerta("Selección Requerida", "Debe seleccionar una orden."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación"); confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro que desea eliminar esta orden?");
        confirm.initModality(Modality.APPLICATION_MODAL);
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM tbl_ordenes_compra WHERE id_orden = ?")) {
                ps.setInt(1, seleccionada.getId()); ps.executeUpdate();
                cargarOrdenesHistorial(); mostrarAlerta("Eliminado", "Registro borrado correctamente.");
            } catch (Exception e) { com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e); }
        }
    }

    private double parsearNumero(String texto) {
        try { return Double.parseDouble(texto.replaceAll("[^0-9.]", "")); } catch (Exception e) { return 0.0; }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje); a.showAndWait();
    }

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
}




