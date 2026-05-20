package com.example.pantallas.controllers;

import com.example.pantallas.services.FabricaBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.sql.*;
import java.io.IOException;

public class DistribucionController {

<<<<<<< HEAD
    @FXML private VBox paneDespacho, paneEntrega, paneLogistica, paneVehiculos, paneInsumos;
    @FXML private Button btnNavDespacho, btnNavEntrega, btnNavLogistica, btnNavVehiculos, btnNavInsumos;
=======
    @FXML private VBox paneDespacho, paneEntrega, paneLogistica, paneVehiculos;
    @FXML private Button btnNavDespacho, btnNavEntrega, btnNavLogistica, btnNavVehiculos;
>>>>>>> bb658e7 (Primer commit)

    @FXML private TableView<Envio> tablaEnvios;
    @FXML private ComboBox<String> cbCambiarEstadoEnvio;
    @FXML private TextField txtFiltroEnvios;
    private final ObservableList<Envio> listaEnvios = FXCollections.observableArrayList();


    @FXML private ComboBox<String> cbVehiculo, cbPedido;
    @FXML private TableView<Despacho> tablaDespachos;
    @FXML private TableColumn<Despacho, Integer> colIdDespacho;
    @FXML private TableColumn<Despacho, String> colPedidoDespacho, colVehiculoDespacho, colEstadoDespacho;


    @FXML private ComboBox<String> cbDespachosActivos;
    @FXML private CheckBox chkRecibido;
    @FXML private TextArea txtNovedades;

    // Vehículos
    @FXML private TextField txtPlaca, txtChofer, txtMarca, txtModelo;
    @FXML private TableView<Vehiculo> tablaVehiculos;
    @FXML private TableColumn<Vehiculo, String> colPlaca, colChofer, colEstadoVehiculo, colMarca, colModelo;

<<<<<<< HEAD
    // Proveedores (CRUD completo)
    @FXML private TextField txtProvNombre, txtProvTelefono, txtProvRnc, txtProvDireccion;
    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, Integer> colProvId;
    @FXML private TableColumn<Proveedor, String> colProvNombre, colProvTel, colProvRnc, colProvDir;

    // Quesos
    @FXML private TextField txtQuesoNombre, txtQuesoPrecio;
    @FXML private TableView<TipoQueso> tablaQuesos;
    @FXML private TableColumn<TipoQueso, String> colQuesoNombre;
    @FXML private TableColumn<TipoQueso, Double> colQuesoPrecio;

    private int idDespachoSeleccionado = -1;
    private String placaSeleccionada = "";
    private int idProveedorSeleccionado = -1;
=======
    private int idDespachoSeleccionado = -1;
    private String placaSeleccionada = "";
>>>>>>> bb658e7 (Primer commit)

    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;trustServerCertificate=true;encrypt=false;";
        
    @FXML
    public void initialize() {
        // Asegurar esquema de proveedores al iniciar
        FabricaBase.asegurarEsquemaProveedores();
        FabricaBase.seedFactoresConversion();

        configurarTablas();
        cargarDatosTablas();
        cargarCombos();
        cbCambiarEstadoEnvio.setItems(FXCollections.observableArrayList("En Espera", "En Ruta", "Detenido"));

        // Listeners para selección en tablas
        tablaDespachos.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                idDespachoSeleccionado = newV.getId();
                cbPedido.setValue("Pedido #" + newV.getPedido());
                cbVehiculo.setValue(newV.getVehiculo());
            }
        });

        tablaVehiculos.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                placaSeleccionada = newV.getPlaca();
                txtPlaca.setText(newV.getPlaca());
                txtChofer.setText(newV.getChofer());
                txtMarca.setText(newV.getMarca());
                txtModelo.setText(newV.getModelo());
            }
        });

<<<<<<< HEAD
        // Listener para selección de proveedores (edición)
        tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                idProveedorSeleccionado = newV.getId();
                txtProvNombre.setText(newV.getNombre());
                txtProvTelefono.setText(newV.getTelefono());
                if (txtProvRnc != null) txtProvRnc.setText(newV.getRnc() != null ? newV.getRnc() : "");
                if (txtProvDireccion != null) txtProvDireccion.setText(newV.getDireccion() != null ? newV.getDireccion() : "");
            }
        });
=======

>>>>>>> bb658e7 (Primer commit)
    }

    private void configurarTablas() {
        colIdDespacho.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPedidoDespacho.setCellValueFactory(new PropertyValueFactory<>("pedido"));
        colVehiculoDespacho.setCellValueFactory(new PropertyValueFactory<>("vehiculo"));
        colEstadoDespacho.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colChofer.setCellValueFactory(new PropertyValueFactory<>("chofer"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colEstadoVehiculo.setCellValueFactory(new PropertyValueFactory<>("estado"));

<<<<<<< HEAD
        // Proveedores — columnas expandidas
        if (colProvId != null) colProvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProvNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProvTel.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        if (colProvRnc != null) colProvRnc.setCellValueFactory(new PropertyValueFactory<>("rnc"));
        if (colProvDir != null) colProvDir.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        colQuesoNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colQuesoPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

=======
>>>>>>> bb658e7 (Primer commit)
        TableColumn colID = new TableColumn("ID"); colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn colCli = new TableColumn("Cliente"); colCli.setCellValueFactory(new PropertyValueFactory<>("cliente")); colCli.setPrefWidth(200);
        TableColumn colEst = new TableColumn("Estatus"); colEst.setCellValueFactory(new PropertyValueFactory<>("estatus"));
        TableColumn colMonto = new TableColumn("Monto Total"); colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        tablaEnvios.getColumns().setAll(colID, colCli, colEst, colMonto);
        tablaEnvios.setItems(listaEnvios);
    }

    // --- CRUD VEHÍCULOS ---
    @FXML private void registrarVehiculo() {
        ejecutarSQL("INSERT INTO tbl_vehiculos (placa, chofer, marca, modelo, estado) VALUES (?, ?, ?, ?, 'Disponible')",
                txtPlaca.getText(), txtChofer.getText(), txtMarca.getText(), txtModelo.getText());
        limpiarVehiculo();
    }

    @FXML private void actualizarVehiculo() {
        if (placaSeleccionada.isEmpty()) return;
        ejecutarSQL("UPDATE tbl_vehiculos SET chofer = ?, marca = ?, modelo = ? WHERE placa = ?",
                txtChofer.getText(), txtMarca.getText(), txtModelo.getText(), placaSeleccionada);
        limpiarVehiculo();
    }

    @FXML private void borrarVehiculo() {
        if (placaSeleccionada.isEmpty()) return;
        ejecutarSQL("DELETE FROM tbl_vehiculos WHERE placa = ?", placaSeleccionada);
        limpiarVehiculo();
    }

    @FXML private void limpiarVehiculo() {
        txtPlaca.clear(); txtChofer.clear(); txtMarca.clear(); txtModelo.clear();
        placaSeleccionada = ""; cargarDatosTablas();
    }


    @FXML private void registrarDespacho() {
        if (cbPedido.getValue() == null || cbVehiculo.getValue() == null) return;
        String idPedido = cbPedido.getValue().replace("Pedido #", "");
        ejecutarSQL("INSERT INTO tbl_despachos (id_pedido, placa_vehiculo, estado) VALUES (?, ?, 'En Camino')",
                idPedido, cbVehiculo.getValue());
        ejecutarSQL("UPDATE tbl_vehiculos SET estado = 'En Ruta' WHERE placa = ?", cbVehiculo.getValue());
        limpiarDespacho();
    }

    @FXML private void actualizarDespacho() {
        if (idDespachoSeleccionado == -1) return;
        String idPedido = cbPedido.getValue().replace("Pedido #", "");
        ejecutarSQL("UPDATE tbl_despachos SET id_pedido = ?, placa_vehiculo = ? WHERE id_despacho = ?",
                idPedido, cbVehiculo.getValue(), idDespachoSeleccionado);
        limpiarDespacho();
    }

    @FXML private void borrarDespacho() {
        if (idDespachoSeleccionado == -1) return;
        ejecutarSQL("DELETE FROM tbl_despachos WHERE id_despacho = ?", idDespachoSeleccionado);
        limpiarDespacho();
    }

    @FXML private void limpiarDespacho() { cbPedido.setValue(null); cbVehiculo.setValue(null); idDespachoSeleccionado = -1; cargarDatosTablas(); cargarCombos(); }


    // FIX: SQL Injection eliminado — ahora usa PreparedStatement
    @FXML private void finalizarEntrega() {
        if (cbDespachosActivos.getValue() == null) return;
        String seleccion = cbDespachosActivos.getValue();
        String idDespacho = seleccion.split(" ")[1].replace("#", "");

        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection()) {
            // FIX: Usar PreparedStatement en vez de concatenación
            try (PreparedStatement psSelect = con.prepareStatement("SELECT placa_vehiculo FROM tbl_despachos WHERE id_despacho = ?")) {
                psSelect.setInt(1, Integer.parseInt(idDespacho));
                ResultSet rs = psSelect.executeQuery();
                if (rs.next()) {
                    String placa = rs.getString("placa_vehiculo");
                    try (PreparedStatement psUpdate = con.prepareStatement("UPDATE tbl_vehiculos SET estado = 'Disponible' WHERE placa = ?")) {
                        psUpdate.setString(1, placa);
                        psUpdate.executeUpdate();
                    }
                }
            }
            try (PreparedStatement psDespacho = con.prepareStatement("UPDATE tbl_despachos SET estado = 'Entregado' WHERE id_despacho = ?")) {
                psDespacho.setInt(1, Integer.parseInt(idDespacho));
                psDespacho.executeUpdate();
            }
            new Alert(Alert.AlertType.INFORMATION, "Entrega confirmada").show();
            txtNovedades.clear();
            cargarDatosTablas();
            cargarCombos();
<<<<<<< HEAD
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e); }
    }

    // --- CRUD PROVEEDORES (COMPLETO CON VALIDACIONES) ---

    @FXML private void agregarProveedor() {
        // Validación de campos obligatorios
        if (txtProvNombre.getText() == null || txtProvNombre.getText().trim().isEmpty()) {
            mostrarError("Campo Obligatorio", null, "Debe ingresar el nombre del proveedor.");
            return;
        }
        if (txtProvTelefono.getText() == null || txtProvTelefono.getText().trim().isEmpty()) {
            mostrarError("Campo Obligatorio", null, "El número telefónico es obligatorio para registrar un proveedor.");
            return;
        }

        String rnc = (txtProvRnc != null && txtProvRnc.getText() != null) ? txtProvRnc.getText().trim() : "";
        String dir = (txtProvDireccion != null && txtProvDireccion.getText() != null) ? txtProvDireccion.getText().trim() : "";

        ejecutarSQL("INSERT INTO tbl_proveedores (nombre, rnc, telefono, direccion) VALUES (?, ?, ?, ?)",
                txtProvNombre.getText().trim(), rnc, txtProvTelefono.getText().trim(), dir);
        new Alert(Alert.AlertType.INFORMATION, "Proveedor registrado correctamente.").show();
        limpiarProveedor();
    }

    @FXML private void actualizarProveedor() {
        if (idProveedorSeleccionado == -1) {
            mostrarError("Selección Requerida", null, "Debe seleccionar un proveedor de la tabla para actualizar.");
            return;
        }
        if (txtProvNombre.getText() == null || txtProvNombre.getText().trim().isEmpty()) {
            mostrarError("Campo Obligatorio", null, "Debe ingresar el nombre del proveedor.");
            return;
        }
        if (txtProvTelefono.getText() == null || txtProvTelefono.getText().trim().isEmpty()) {
            mostrarError("Campo Obligatorio", null, "El número telefónico es obligatorio.");
            return;
        }

        String rnc = (txtProvRnc != null && txtProvRnc.getText() != null) ? txtProvRnc.getText().trim() : "";
        String dir = (txtProvDireccion != null && txtProvDireccion.getText() != null) ? txtProvDireccion.getText().trim() : "";

        ejecutarSQL("UPDATE tbl_proveedores SET nombre = ?, rnc = ?, telefono = ?, direccion = ? WHERE provider_id = ?",
                txtProvNombre.getText().trim(), rnc, txtProvTelefono.getText().trim(), dir, idProveedorSeleccionado);
        new Alert(Alert.AlertType.INFORMATION, "Proveedor actualizado correctamente.").show();
        limpiarProveedor();
    }

    @FXML private void borrarProveedor() {
        Proveedor p = tablaProveedores.getSelectionModel().getSelectedItem();
        if (p == null) {
            mostrarError("Selección Requerida", null, "Debe seleccionar un proveedor de la tabla para eliminar.");
            return;
        }
        ejecutarSQL("DELETE FROM tbl_proveedores WHERE provider_id = ?", p.getId());
        new Alert(Alert.AlertType.INFORMATION, "Proveedor eliminado.").show();
        limpiarProveedor();
    }

    @FXML private void limpiarProveedor() {
        txtProvNombre.clear();
        txtProvTelefono.clear();
        if (txtProvRnc != null) txtProvRnc.clear();
        if (txtProvDireccion != null) txtProvDireccion.clear();
        idProveedorSeleccionado = -1;
        tablaProveedores.getSelectionModel().clearSelection();
        cargarDatosTablas();
    }

    @FXML private void agregarQueso() {
        ejecutarSQL("INSERT INTO tbl_tipos_queso (nombre_queso, precio_unidad) VALUES (?, ?)", txtQuesoNombre.getText(), txtQuesoPrecio.getText());
        cargarDatosTablas();
    }

    @FXML private void borrarQueso() {
        TipoQueso q = tablaQuesos.getSelectionModel().getSelectedItem();
        if (q != null) ejecutarSQL("DELETE FROM tbl_tipos_queso WHERE nombre_queso = ?", q.getNombre());
        cargarDatosTablas();
    }


    private void cargarDatosTablas() {
        ObservableList<Vehiculo> vList = FXCollections.observableArrayList();
        ObservableList<Despacho> dList = FXCollections.observableArrayList();
        ObservableList<Proveedor> pList = FXCollections.observableArrayList();
        ObservableList<TipoQueso> qList = FXCollections.observableArrayList();
=======
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepción detectada", e); }
    }

    private void cargarDatosTablas() {
        ObservableList<Vehiculo> vList = FXCollections.observableArrayList();
        ObservableList<Despacho> dList = FXCollections.observableArrayList();
>>>>>>> bb658e7 (Primer commit)

        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection()) {
            ResultSet rsV = con.createStatement().executeQuery("SELECT * FROM tbl_vehiculos");
            while (rsV.next()) vList.add(new Vehiculo(rsV.getString("placa"), rsV.getString("chofer"), rsV.getString("estado"), rsV.getString("marca"), rsV.getString("modelo")));
            tablaVehiculos.setItems(vList);

            ResultSet rsD = con.createStatement().executeQuery("SELECT * FROM tbl_despachos");
            while (rsD.next()) dList.add(new Despacho(rsD.getInt("id_despacho"), rsD.getString("id_pedido"), rsD.getString("placa_vehiculo"), rsD.getString("estado")));
            tablaDespachos.setItems(dList);

<<<<<<< HEAD
            // Proveedores desde tabla unificada tbl_proveedores
            ResultSet rsP = con.createStatement().executeQuery("SELECT provider_id, nombre, rnc, telefono, direccion FROM tbl_proveedores WHERE activo = 1 ORDER BY nombre");
            while (rsP.next()) pList.add(new Proveedor(rsP.getInt("provider_id"), rsP.getString("nombre"),
                    rsP.getString("rnc"), rsP.getString("telefono"), rsP.getString("direccion")));
            tablaProveedores.setItems(pList);

            ResultSet rsQ = con.createStatement().executeQuery("SELECT * FROM tbl_tipos_queso");
            while (rsQ.next()) qList.add(new TipoQueso(rsQ.getString("nombre_queso"), rsQ.getDouble("precio_unidad")));
            tablaQuesos.setItems(qList);

=======
>>>>>>> bb658e7 (Primer commit)
            ResultSet rsE = con.createStatement().executeQuery("SELECT id_envio, cliente, estatus, monto_total FROM tbl_envios WHERE estatus != 'Entregado' ORDER BY id_envio DESC");
            listaEnvios.clear();
            while (rsE.next()) {
                listaEnvios.add(new Envio(rsE.getInt(1), rsE.getString(2), rsE.getString(3), rsE.getDouble(4)));
            }
<<<<<<< HEAD
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e); }
=======
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepción detectada", e); }
>>>>>>> bb658e7 (Primer commit)
    }

    private void cargarCombos() {
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection()) {
            cbVehiculo.getItems().clear();
            ResultSet rsV = con.createStatement().executeQuery("SELECT placa FROM tbl_vehiculos WHERE estado = 'Disponible'");
            while (rsV.next()) cbVehiculo.getItems().add(rsV.getString("placa"));

            cbPedido.getItems().clear();
            ResultSet rsP = con.createStatement().executeQuery("SELECT id_pedido FROM tbl_pedidos WHERE estado = 'Preparado'");
            while (rsP.next()) cbPedido.getItems().add("Pedido #" + rsP.getInt("id_pedido"));

            cbDespachosActivos.getItems().clear();
            ResultSet rsDA = con.createStatement().executeQuery("SELECT id_despacho, placa_vehiculo FROM tbl_despachos WHERE estado = 'En Camino'");
            while (rsDA.next()) cbDespachosActivos.getItems().add("Despacho #" + rsDA.getInt("id_despacho") + " (" + rsDA.getString("placa_vehiculo") + ")");

<<<<<<< HEAD
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e); }
=======
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepción detectada", e); }
>>>>>>> bb658e7 (Primer commit)
    }

    private void ejecutarSQL(String sql, Object... params) {
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
<<<<<<< HEAD
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e); }
=======
        } catch (SQLException e) { com.example.pantallas.utils.LoggerUtil.error("Excepción detectada", e); }
>>>>>>> bb658e7 (Primer commit)
    }

    // FIX: Incluye paneLogistica en la lista de panes a ocultar
    private void ocultarTodo() {
<<<<<<< HEAD
        VBox[] panes = {paneDespacho, paneEntrega, paneLogistica, paneVehiculos, paneInsumos};
        for (VBox p : panes) { if(p != null) { p.setVisible(false); p.setManaged(false); } }
        Button[] btns = {btnNavDespacho, btnNavEntrega, btnNavLogistica, btnNavVehiculos, btnNavInsumos};
=======
        VBox[] panes = {paneDespacho, paneEntrega, paneLogistica, paneVehiculos};
        for (VBox p : panes) { if(p != null) { p.setVisible(false); p.setManaged(false); } }
        Button[] btns = {btnNavDespacho, btnNavEntrega, btnNavLogistica, btnNavVehiculos};
>>>>>>> bb658e7 (Primer commit)
        for (Button b : btns) {
            if (b != null) b.getStyleClass().remove("nav-item-active");
        }
    }

    @FXML private void mostrarDespacho() { alternarVista(paneDespacho, btnNavDespacho); }
    @FXML private void mostrarEntrega() { alternarVista(paneEntrega, btnNavEntrega); }
    @FXML private void mostrarLogistica() { alternarVista(paneLogistica, btnNavLogistica); }
    @FXML private void mostrarVehiculos() { alternarVista(paneVehiculos, btnNavVehiculos); }
<<<<<<< HEAD
    @FXML private void mostrarInsumos() { alternarVista(paneInsumos, btnNavInsumos); }
=======
>>>>>>> bb658e7 (Primer commit)

    @FXML private void marcarEntregado() {
        Envio sel = tablaEnvios.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError("Selección Requerida", null, "Debe seleccionar un envío de la tabla.");
            return;
        }
        actualizarEstatusBD(sel.getId(), "Entregado");
        new Alert(Alert.AlertType.INFORMATION, "Envío marcado como ENTREGADO.").show();
    }

    @FXML private void actualizarEstatusEnvio() {
        Envio sel = tablaEnvios.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError("Selección Requerida", null, "Debe seleccionar un envío de la tabla.");
            return;
        }
        if (cbCambiarEstadoEnvio.getValue() == null) {
            mostrarError("Estado Requerido", null, "Debe seleccionar un estado para actualizar.");
            return;
        }
        actualizarEstatusBD(sel.getId(), cbCambiarEstadoEnvio.getValue());
        new Alert(Alert.AlertType.INFORMATION, "Envío actualizado a: " + cbCambiarEstadoEnvio.getValue()).show();
    }

    private void actualizarEstatusBD(int id, String estado) {
        ejecutarSQL("UPDATE tbl_envios SET estatus = ? WHERE id_envio = ?", estado, id);
        cargarDatosTablas();
    }

    private void alternarVista(VBox pane, Button btn) {
        ocultarTodo();
        if (pane != null) {
            pane.setVisible(true);
            pane.setManaged(true);
        }
        if (btn != null) {
<<<<<<< HEAD
            Button[] btns = {btnNavDespacho, btnNavEntrega, btnNavLogistica, btnNavVehiculos, btnNavInsumos};
=======
            Button[] btns = {btnNavDespacho, btnNavEntrega, btnNavLogistica, btnNavVehiculos};
>>>>>>> bb658e7 (Primer commit)
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

    @FXML
    private void irAMenuPrincipal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.setMaximized(wasMaximized);
            if (!wasMaximized) stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            mostrarError("Error", "No se pudo cargar el Menú Principal", e.getMessage());
<<<<<<< HEAD
            com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e);
=======
            com.example.pantallas.utils.LoggerUtil.error("Excepción detectada", e);
>>>>>>> bb658e7 (Primer commit)
        }
    }

    private void mostrarError(String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    // --- CLASES MODELO ---
    public static class Vehiculo {
        private String placa, chofer, estado, marca, modelo;
        public Vehiculo(String p, String c, String e, String m, String mo) { this.placa = p; this.chofer = c; this.estado = e; this.marca = m; this.modelo = mo; }
        public String getPlaca() { return placa; }
        public String getChofer() { return chofer; }
        public String getEstado() { return estado; }
        public String getMarca() { return marca; }
        public String getModelo() { return modelo; }
    }

    public static class Despacho {
        private int id; private String pedido, vehiculo, estado;
        public Despacho(int i, String p, String v, String e) { this.id = i; this.pedido = p; this.vehiculo = v; this.estado = e; }
        public int getId() { return id; }
        public String getPedido() { return pedido; }
        public String getVehiculo() { return vehiculo; }
        public String getEstado() { return estado; }
    }

<<<<<<< HEAD
    public static class Proveedor {
        private int id;
        private String nombre, rnc, telefono, direccion;
        public Proveedor(int id, String n, String rnc, String t, String d) {
            this.id = id; this.nombre = n; this.rnc = rnc; this.telefono = t; this.direccion = d;
        }
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getRnc() { return rnc; }
        public String getTelefono() { return telefono; }
        public String getDireccion() { return direccion; }
    }

=======
>>>>>>> bb658e7 (Primer commit)
    public static class Envio {
        private int id;
        private String cliente, estatus;
        private double monto;
        public Envio(int id, String c, String e, double m) {
            this.id = id; this.cliente = c; this.estatus = e; this.monto = m;
        }
        public int getId() { return id; }
        public String getCliente() { return cliente; }
        public String getEstatus() { return estatus; }
        public double getMonto() { return monto; }
    }

<<<<<<< HEAD
    public static class TipoQueso {
        private String nombre; private double precio;
        public TipoQueso(String n, double p) { this.nombre = n; this.precio = p; }
        public String getNombre() { return nombre; }
        public double getPrecio() { return precio; }
    }
=======
>>>>>>> bb658e7 (Primer commit)
}


