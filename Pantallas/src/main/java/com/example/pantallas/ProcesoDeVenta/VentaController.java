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
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import com.example.pantallas.base.ServicioVentas;

public class VentaController {

    @FXML private HBox rootPane;
    @FXML private VBox viewVentas, viewCobro, viewNuevoCliente;
    @FXML private Button btnEditar, btnAñadir;
    @FXML private TableView<ItemQueso> tablaVentas;
    @FXML private TextField txtLibras, txtPrecioUnitario, txtImporteTotal, txtEfectivoRecibido, txtNewNombre, txtNewRnc, txtNewTel;
    @FXML private Label lblTotal, lblMontoCobro, lblDevuelta;
    @FXML private ComboBox<String> cbProducto, cbCliente, comboMetodoPago, cbCambiarEstado;

    @FXML private ToggleGroup tgUnidadMedida;
    @FXML private RadioButton rbLibras, rbKilos;

    private final ObservableList<ItemQueso> listaVenta = FXCollections.observableArrayList();
    private final Map<String, Double> preciosProductos = new HashMap<>();

    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;trustServerCertificate=true;encrypt=false;";
    private final String USER = "sa"; private final String PASS = "123456";

    private boolean modoKilos = false;

    @FXML
    public void initialize() {
        configurarTablas();
        cargarDatosDesdeDB();
        comboMetodoPago.setItems(FXCollections.observableArrayList("Efectivo", "Tarjeta", "Transferencia"));
        cbCambiarEstado.setItems(FXCollections.observableArrayList("En Espera", "En Ruta", "Detenido"));

        rbLibras.setSelected(true);
        modoKilos = false;

        tgUnidadMedida.selectedToggleProperty().addListener((obs, old, newToggle) -> {
            if (rbKilos.isSelected()) {
                modoKilos = true;
                txtLibras.setPromptText("Kg");
            } else {
                modoKilos = false;
                txtLibras.setPromptText("Lbs");
            }
            cargarPrecioSugerido(); // Actualizar precio al cambiar unidad
        });

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
        TableColumn colL = new TableColumn("Cantidad"); colL.setCellValueFactory(new PropertyValueFactory<>("libras"));
        TableColumn colU = new TableColumn("Unidad"); colU.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        TableColumn colPr = new TableColumn("Precio"); colPr.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        TableColumn colS = new TableColumn("Subtotal"); colS.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        tablaVentas.getColumns().setAll(colP, colL, colU, colPr, colS);
        tablaVentas.setItems(listaVenta);
    }

    @FXML private void volverAlMenu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.setMaximized(wasMaximized);
            if (!wasMaximized) stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo encontrar el MenuPrincipal.fxml").show();
            e.printStackTrace();
        }
    }

    @FXML private Button btnNavFacturacion;

    @FXML private void mostrarVentas() { alternarVista(viewVentas, btnNavFacturacion); }

    private void alternarVista(VBox pane, Button btn) {
        viewVentas.setVisible(false); viewVentas.setManaged(false);
        viewCobro.setVisible(false); viewCobro.setManaged(false);
        viewNuevoCliente.setVisible(false); viewNuevoCliente.setManaged(false);
        
        if (pane != null) {
            pane.setVisible(true);
            pane.setManaged(true);
        }
        
        if (btn != null) {
            btn.getStyleClass().remove("nav-item");
            if (!btn.getStyleClass().contains("nav-item-active")) btn.getStyleClass().add("nav-item-active");
        }
    }

    @FXML private void mostrarCobro() { lblMontoCobro.setText(lblTotal.getText()); alternarVista(viewCobro, null); }
    @FXML private void mostrarNuevoCliente() { alternarVista(viewNuevoCliente, null); }

    @FXML private void cargarPrecioSugerido() {
        String prod = cbProducto.getValue();
        if (prod != null) {
            double precioBase = ServicioVentas.obtenerPrecioBase(prod);
            double precioFinal = ServicioVentas.calcularPrecioUnitario(precioBase, modoKilos);
            txtPrecioUnitario.setText(String.format("%.2f", precioFinal));
            calcularItem();
        }
    }

    @FXML private void calcularItem() {
        try {
            double l = parsearNumero(txtLibras.getText());
            double p = parsearNumero(txtPrecioUnitario.getText());
            txtImporteTotal.setText(String.format("%.2f", l * p));
        } catch (Exception e) { txtImporteTotal.setText("0.00"); }
    }

    @FXML private void calcularDevuelta() {
        try {
            String limpio = lblTotal.getText().replace("RD$", "").replace(",", "").trim();
            double tot = Double.parseDouble(limpio);
            double rec = parsearNumero(txtEfectivoRecibido.getText());
            lblDevuelta.setText(String.format("Devuelta: RD$ %.2f", rec - tot));
        } catch (Exception e) { lblDevuelta.setText("Devuelta: RD$ 0.00"); }
    }

    @FXML private void añadirItem() {
        if (validarCampoVacio(cbProducto.getValue(), "Producto") ||
            validarCampoVacio(txtLibras.getText(), "Cantidad") ||
            validarCampoVacio(txtPrecioUnitario.getText(), "Precio")) {
            return;
        }

        ItemQueso sel = tablaVentas.getSelectionModel().getSelectedItem();
        if (sel != null) listaVenta.remove(sel);

        String unidad = modoKilos ? "Kilos" : "Libras";
        listaVenta.add(new ItemQueso(cbProducto.getValue(), parsearNumero(txtLibras.getText()), parsearNumero(txtPrecioUnitario.getText()), unidad));
        actualizarTotal();
        limpiarCampos();
    }

    private boolean validarCampoVacio(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            mostrarAlerta("Campo Vacío", "Debe seleccionar: " + nombreCampo);
            return true;
        }
        return false;
    }

    @FXML private void guardarCliente() {
        if (validarCampoVacio(txtNewNombre.getText(), "Nombre del Cliente")) {
            return;
        }

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement("INSERT INTO tbl_clientes (nombre, rnc_cedula, telefono) VALUES (?,?,?)")) {
            ps.setString(1, txtNewNombre.getText());
            ps.setString(2, txtNewRnc.getText() != null ? txtNewRnc.getText() : "");
            ps.setString(3, txtNewTel.getText() != null ? txtNewTel.getText() : "");
            ps.executeUpdate();
            new Alert(Alert.AlertType.INFORMATION, "Cliente registrado").show();
            cargarDatosDesdeDB();
            mostrarVentas();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo registrar el cliente.");
        }
    }


    @FXML private void finalizarVenta() {
        if (validarCampoVacio(cbCliente.getValue(), "Cliente")) {
            return;
        }

        if (listaVenta.isEmpty()) {
            mostrarAlerta("Carrito Vacío", "Debe añadir al menos un producto.");
            return;
        }

        if (comboMetodoPago.getValue() == null) {
            mostrarAlerta("Método de Pago", "Debe seleccionar el método de pago.");
            return;
        }

        if (comboMetodoPago.getValue().equals("Efectivo")) {
            ServicioVentas.dispararNotificacionCobroEfectivo();
        }

        try {
            String limpio = lblTotal.getText().replace("RD$", "").replace(",", "").trim();
            double totalVenta = Double.parseDouble(limpio);

            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement("INSERT INTO tbl_envios (cliente, estatus, monto_total, metodo_pago) VALUES (?, 'En Espera', ?, ?)")) {
                ps.setString(1, cbCliente.getValue());
                ps.setDouble(2, totalVenta);
                ps.setString(3, comboMetodoPago.getValue());
                ps.executeUpdate();

                listaVenta.clear();
                actualizarTotal();
                mostrarVentas(); // Volver al panel principal
                new Alert(Alert.AlertType.INFORMATION, "Venta Exitosa. El pedido ha sido procesado.").show();
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void actualizarTotal() {
        double total = listaVenta.stream().mapToDouble(ItemQueso::getSubtotal).sum();
        lblTotal.setText(String.format("RD$ %.2f", total));
    }

    @FXML private void limpiarCampos() { cbProducto.setValue(null); txtLibras.clear(); txtPrecioUnitario.clear(); txtImporteTotal.setText("0.00"); }

    private double parsearNumero(String texto) {
        try {
            return Double.parseDouble(texto.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }

    public static class ItemQueso {
        private String descripcion, unidad;
        private double libras, precioUnitario, subtotal;
        
        public ItemQueso(String d, double l, double p, String u) {
            this.descripcion = d;
            this.libras = l;
            this.precioUnitario = p;
            this.unidad = u;
            this.subtotal = l * p;
        }
        
        public String getDescripcion() { return descripcion; }
        public double getLibras() { return libras; }
        public String getUnidad() { return unidad; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getSubtotal() { return subtotal; }
    }
}