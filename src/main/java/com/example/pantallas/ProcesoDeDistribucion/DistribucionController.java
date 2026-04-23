package com.example.pantallas.ProcesoDeDistribucion;

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

    @FXML private VBox paneDespacho, paneEntrega, paneVehiculos, paneInsumos;
    @FXML private Button btnNavDespacho, btnNavEntrega, btnNavVehiculos, btnNavInsumos;


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

    // Insumos (Extras)
    @FXML private TextField txtProvNombre, txtProvTelefono, txtQuesoNombre, txtQuesoPrecio;
    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, String> colProvNombre, colProvTel;
    @FXML private TableView<TipoQueso> tablaQuesos;
    @FXML private TableColumn<TipoQueso, String> colQuesoNombre;
    @FXML private TableColumn<TipoQueso, Double> colQuesoPrecio;

    private int idDespachoSeleccionado = -1;
    private String placaSeleccionada = "";

    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;encrypt=true;trustServerCertificate=true;";
    private final String USER = "janciel";
    private final String PASS = "123456789";

    @FXML
    public void initialize() {
        configurarTablas();
        cargarDatosTablas();
        cargarCombos();

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

        colProvNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProvTel.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colQuesoNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colQuesoPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
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


    @FXML private void finalizarEntrega() {
        if (cbDespachosActivos.getValue() == null) return;
        String seleccion = cbDespachosActivos.getValue();
        String idDespacho = seleccion.split(" ")[1].replace("#", "");

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            ResultSet rs = con.createStatement().executeQuery("SELECT placa_vehiculo FROM tbl_despachos WHERE id_despacho = " + idDespacho);
            if (rs.next()) {
                String placa = rs.getString("placa_vehiculo");
                con.createStatement().executeUpdate("UPDATE tbl_vehiculos SET estado = 'Disponible' WHERE placa = '" + placa + "'");
            }
            con.createStatement().executeUpdate("UPDATE tbl_despachos SET estado = 'Entregado' WHERE id_despacho = " + idDespacho);
            new Alert(Alert.AlertType.INFORMATION, "Entrega confirmada").show();
            txtNovedades.clear();
            cargarDatosTablas();
            cargarCombos();
        } catch (SQLException e) { e.printStackTrace(); }
    }


    @FXML private void agregarProveedor() {
        ejecutarSQL("INSERT INTO tbl_proveedores (nombre, telefono) VALUES (?, ?)", txtProvNombre.getText(), txtProvTelefono.getText());
        cargarDatosTablas();
    }

    @FXML private void borrarProveedor() {
        Proveedor p = tablaProveedores.getSelectionModel().getSelectedItem();
        if (p != null) ejecutarSQL("DELETE FROM tbl_proveedores WHERE nombre = ?", p.getNombre());
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

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            ResultSet rsV = con.createStatement().executeQuery("SELECT * FROM tbl_vehiculos");
            while (rsV.next()) vList.add(new Vehiculo(rsV.getString("placa"), rsV.getString("chofer"), rsV.getString("estado"), rsV.getString("marca"), rsV.getString("modelo")));
            tablaVehiculos.setItems(vList);

            ResultSet rsD = con.createStatement().executeQuery("SELECT * FROM tbl_despachos");
            while (rsD.next()) dList.add(new Despacho(rsD.getInt("id_despacho"), rsD.getString("id_pedido"), rsD.getString("placa_vehiculo"), rsD.getString("estado")));
            tablaDespachos.setItems(dList);

            ResultSet rsP = con.createStatement().executeQuery("SELECT * FROM tbl_proveedores");
            while (rsP.next()) pList.add(new Proveedor(rsP.getString("nombre"), rsP.getString("telefono")));
            tablaProveedores.setItems(pList);

            ResultSet rsQ = con.createStatement().executeQuery("SELECT * FROM tbl_tipos_queso");
            while (rsQ.next()) qList.add(new TipoQueso(rsQ.getString("nombre_queso"), rsQ.getDouble("precio_unidad")));
            tablaQuesos.setItems(qList);

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void cargarCombos() {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            cbVehiculo.getItems().clear();
            ResultSet rsV = con.createStatement().executeQuery("SELECT placa FROM tbl_vehiculos WHERE estado = 'Disponible'");
            while (rsV.next()) cbVehiculo.getItems().add(rsV.getString("placa"));

            cbPedido.getItems().clear();
            ResultSet rsP = con.createStatement().executeQuery("SELECT id_pedido FROM tbl_pedidos WHERE estado = 'Preparado'");
            while (rsP.next()) cbPedido.getItems().add("Pedido #" + rsP.getInt("id_pedido"));

            cbDespachosActivos.getItems().clear();
            ResultSet rsDA = con.createStatement().executeQuery("SELECT id_despacho, placa_vehiculo FROM tbl_despachos WHERE estado = 'En Camino'");
            while (rsDA.next()) cbDespachosActivos.getItems().add("Despacho #" + rsDA.getInt("id_despacho") + " (" + rsDA.getString("placa_vehiculo") + ")");

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void ejecutarSQL(String sql, Object... params) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }


    private void ocultarTodo() {
        VBox[] panes = {paneDespacho, paneEntrega, paneVehiculos, paneInsumos};
        for (VBox p : panes) { if(p != null) { p.setVisible(false); p.setManaged(false); } }
        if(btnNavDespacho != null) btnNavDespacho.getStyleClass().remove("nav-button-active");
        if(btnNavEntrega != null) btnNavEntrega.getStyleClass().remove("nav-button-active");
        if(btnNavVehiculos != null) btnNavVehiculos.getStyleClass().remove("nav-button-active");
        if(btnNavInsumos != null) btnNavInsumos.getStyleClass().remove("nav-button-active");
    }

    @FXML private void mostrarDespacho() { ocultarTodo(); paneDespacho.setVisible(true); paneDespacho.setManaged(true); btnNavDespacho.getStyleClass().add("nav-button-active"); }
    @FXML private void mostrarEntrega() { ocultarTodo(); paneEntrega.setVisible(true); paneEntrega.setManaged(true); btnNavEntrega.getStyleClass().add("nav-button-active"); }
    @FXML private void mostrarVehiculos() { ocultarTodo(); paneVehiculos.setVisible(true); paneVehiculos.setManaged(true); btnNavVehiculos.getStyleClass().add("nav-button-active"); }
    @FXML private void mostrarInsumos() { ocultarTodo(); paneInsumos.setVisible(true); paneInsumos.setManaged(true); btnNavInsumos.getStyleClass().add("nav-button-active"); }

    @FXML
    private void irAMenuPrincipal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml")); // Ajusta la ruta si es necesario
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            mostrarError("Error", "No se pudo cargar el Menú Principal", e.getMessage());
            e.printStackTrace();
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

    public static class Proveedor {
        private String nombre, telefono;
        public Proveedor(String n, String t) { this.nombre = n; this.telefono = t; }
        public String getNombre() { return nombre; }
        public String getTelefono() { return telefono; }
    }

    public static class TipoQueso {
        private String nombre; private double precio;
        public TipoQueso(String n, double p) { this.nombre = n; this.precio = p; }
        public String getNombre() { return nombre; }
        public double getPrecio() { return precio; }
    }
}