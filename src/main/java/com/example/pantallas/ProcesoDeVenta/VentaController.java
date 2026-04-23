package com.example.pantallas.ProcesoDeVenta;

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
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class VentaController {

    @FXML private HBox rootPane;
    @FXML private VBox viewVentas, viewEnvios, viewCobro, viewNuevoCliente;
    @FXML private Button btnEditar, btnAñadir;
    @FXML private TableView<ItemQueso> tablaVentas;
    @FXML private TableView<Envio> tablaEnvios;
    @FXML private TextField txtLibras, txtPrecioUnitario, txtImporteTotal, txtEfectivoRecibido, txtNewNombre, txtNewRnc, txtNewTel;
    @FXML private Label lblTotal, lblMontoCobro, lblDevuelta;
    @FXML private ComboBox<String> cbProducto, cbCliente, comboMetodoPago, cbCambiarEstado;

    private final ObservableList<ItemQueso> listaVenta = FXCollections.observableArrayList();
    private final ObservableList<Envio> listaEnvios = FXCollections.observableArrayList();
    private final Map<String, Double> preciosProductos = new HashMap<>();

    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;encrypt=true;trustServerCertificate=true;";
    private final String USER = "janciel"; private final String PASS = "123456789";

    @FXML
    public void initialize() {
        configurarTablas();
        cargarDatosDesdeDB();
        comboMetodoPago.setItems(FXCollections.observableArrayList("Efectivo", "Tarjeta", "Transferencia"));
        cbCambiarEstado.setItems(FXCollections.observableArrayList("En Espera", "En Ruta", "Detenido", "Entregado"));

        tablaVentas.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if (newSel != null) {
                btnEditar.setVisible(true); btnEditar.setManaged(true);
                btnAñadir.setVisible(false); btnAñadir.setManaged(false);
                cbProducto.setValue(newSel.getDescripcion());
                txtLibras.setText(String.valueOf(newSel.getLibras()));
                txtPrecioUnitario.setText(String.valueOf(newSel.getPrecioUnitario()));
                calcularItem();
            } else {
                btnEditar.setVisible(false); btnEditar.setManaged(false);
                btnAñadir.setVisible(true); btnAñadir.setManaged(true);
            }
        });
    }

    private void configurarTablas() {
        TableColumn colP = new TableColumn("Producto"); colP.setCellValueFactory(new PropertyValueFactory<>("descripcion")); colP.setPrefWidth(220);
        TableColumn colL = new TableColumn("Lbs"); colL.setCellValueFactory(new PropertyValueFactory<>("libras"));
        TableColumn colPr = new TableColumn("Precio"); colPr.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        TableColumn colS = new TableColumn("Subtotal"); colS.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        tablaVentas.getColumns().setAll(colP, colL, colPr, colS);
        tablaVentas.setItems(listaVenta);

        TableColumn colID = new TableColumn("ID"); colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn colCli = new TableColumn("Cliente"); colCli.setCellValueFactory(new PropertyValueFactory<>("cliente")); colCli.setPrefWidth(200);
        TableColumn colEst = new TableColumn("Estatus"); colEst.setCellValueFactory(new PropertyValueFactory<>("estatus"));
        TableColumn colMonto = new TableColumn("Monto Total"); colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        tablaEnvios.getColumns().setAll(colID, colCli, colEst, colMonto);
        tablaEnvios.setItems(listaEnvios);
    }

    @FXML private void volverAlMenu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo encontrar el MenuPrincipal.fxml").show();
            e.printStackTrace();
        }
    }

    @FXML private void mostrarVentas() { alternarVista(viewVentas); }
    @FXML private void mostrarEnvios() { alternarVista(viewEnvios); cargarDatosDesdeDB(); }
    @FXML private void mostrarCobro() { lblMontoCobro.setText(lblTotal.getText()); alternarVista(viewCobro); }
    @FXML private void mostrarNuevoCliente() { alternarVista(viewNuevoCliente); }

    private void alternarVista(VBox vista) {
        viewVentas.setVisible(false); viewVentas.setManaged(false);
        viewEnvios.setVisible(false); viewEnvios.setManaged(false);
        viewCobro.setVisible(false); viewCobro.setManaged(false);
        viewNuevoCliente.setVisible(false); viewNuevoCliente.setManaged(false);
        vista.setVisible(true); vista.setManaged(true);
    }

    @FXML private void cargarPrecioSugerido() {
        if (preciosProductos.containsKey(cbProducto.getValue())) {
            txtPrecioUnitario.setText(preciosProductos.get(cbProducto.getValue()).toString());
            calcularItem();
        }
    }

    @FXML private void calcularItem() {
        try {
            double l = Double.parseDouble(txtLibras.getText().replace(",", "."));
            double p = Double.parseDouble(txtPrecioUnitario.getText().replace(",", "."));
            txtImporteTotal.setText(String.format("%.2f", l * p));
        } catch (Exception e) { txtImporteTotal.setText("0.00"); }
    }

    @FXML private void calcularDevuelta() {
        try {
            String limpio = lblTotal.getText().replace("RD$", "").replace(",", "").trim();
            double tot = Double.parseDouble(limpio);
            double rec = Double.parseDouble(txtEfectivoRecibido.getText());
            lblDevuelta.setText(String.format("Devuelta: RD$ %.2f", rec - tot));
        } catch (Exception e) { lblDevuelta.setText("Devuelta: RD$ 0.00"); }
    }

    @FXML private void añadirItem() {
        if (cbProducto.getValue() == null || txtLibras.getText().isEmpty()) return;
        ItemQueso sel = tablaVentas.getSelectionModel().getSelectedItem();
        if (sel != null) listaVenta.remove(sel);
        listaVenta.add(new ItemQueso(cbProducto.getValue(), Double.parseDouble(txtLibras.getText()), Double.parseDouble(txtPrecioUnitario.getText())));
        actualizarTotal(); limpiarCampos();
    }

    @FXML private void guardarCliente() {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement("INSERT INTO tbl_clientes (nombre, rnc_cedula, telefono) VALUES (?,?,?)")) {
            ps.setString(1, txtNewNombre.getText()); ps.setString(2, txtNewRnc.getText()); ps.setString(3, txtNewTel.getText());
            ps.executeUpdate();
            new Alert(Alert.AlertType.INFORMATION, "Cliente registrado").show();
            cargarDatosDesdeDB(); mostrarVentas();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void marcarEntregado() {
        Envio sel = tablaEnvios.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        actualizarEstatusBD(sel.getId(), "Entregado");
    }

    @FXML private void actualizarEstatusEnvio() {
        Envio sel = tablaEnvios.getSelectionModel().getSelectedItem();
        String nuevoEstado = cbCambiarEstado.getValue();
        if (sel != null && nuevoEstado != null) {
            actualizarEstatusBD(sel.getId(), nuevoEstado);
        }
    }

    private void actualizarEstatusBD(int id, String estado) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement("UPDATE tbl_envios SET estatus = ? WHERE id_envio = ?")) {
            ps.setString(1, estado); ps.setInt(2, id); ps.executeUpdate();
            cargarDatosDesdeDB();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void finalizarVenta() {
        if (cbCliente.getValue() == null || listaVenta.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Seleccione un cliente y añada productos.").show();
            return;
        }

        try {
            String limpio = lblTotal.getText().replace("RD$", "").replace(",", "").trim();
            double totalVenta = Double.parseDouble(limpio);

            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement("INSERT INTO tbl_envios (cliente, estatus, monto_total) VALUES (?, 'En Espera', ?)")) {
                ps.setString(1, cbCliente.getValue());
                ps.setDouble(2, totalVenta);
                ps.executeUpdate();

                listaVenta.clear();
                actualizarTotal();
                mostrarVentas();
                new Alert(Alert.AlertType.INFORMATION, "Venta Exitosa. Envío programado.").show();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error al procesar: " + e.getMessage()).show();
            e.printStackTrace();
        }
    }

    public void cargarDatosDesdeDB() {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            ResultSet rsP = con.createStatement().executeQuery("SELECT nombre, precio_libra FROM tbl_productos");
            cbProducto.getItems().clear();
            while (rsP.next()) {
                preciosProductos.put(rsP.getString("nombre"), rsP.getDouble("precio_libra"));
                cbProducto.getItems().add(rsP.getString("nombre"));
            }
            ResultSet rsC = con.createStatement().executeQuery("SELECT nombre FROM tbl_clientes");
            cbCliente.getItems().clear();
            while (rsC.next()) cbCliente.getItems().add(rsC.getString("nombre"));

            ResultSet rsE = con.createStatement().executeQuery("SELECT id_envio, cliente, estatus, monto_total FROM tbl_envios ORDER BY id_envio DESC");
            listaEnvios.clear();
            while (rsE.next()) {
                listaEnvios.add(new Envio(rsE.getInt(1), rsE.getString(2), rsE.getString(3), rsE.getDouble(4)));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void actualizarTotal() {
        double total = listaVenta.stream().mapToDouble(ItemQueso::getSubtotal).sum();
        lblTotal.setText(String.format("RD$ %.2f", total));
    }

    @FXML private void limpiarCampos() { cbProducto.setValue(null); txtLibras.clear(); txtPrecioUnitario.clear(); txtImporteTotal.setText("0.00"); }

    public static class ItemQueso {
        private String descripcion; private double libras, precioUnitario, subtotal;
        public ItemQueso(String d, double l, double p) { this.descripcion = d; this.libras = l; this.precioUnitario = p; this.subtotal = l * p; }
        public String getDescripcion() { return descripcion; }
        public double getLibras() { return libras; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getSubtotal() { return subtotal; }
    }

    public static class Envio {
        private int id; private String cliente, estatus; private double monto;
        public Envio(int id, String c, String e, double m) { this.id = id; this.cliente = c; this.estatus = e; this.monto = m; }
        public int getId() { return id; }
        public String getCliente() { return cliente; }
        public String getEstatus() { return estatus; }
        public double getMonto() { return monto; }
    }
}