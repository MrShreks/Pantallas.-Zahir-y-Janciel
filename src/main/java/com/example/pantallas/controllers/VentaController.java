package com.example.pantallas.controllers;

<<<<<<< HEAD
import com.example.pantallas.config.ConnectionManager;
=======
import com.example.pantallas.ReporteHelper;
import com.example.pantallas.config.ConnectionManager;
import com.example.pantallas.services.ServicioVentas;
>>>>>>> bb658e7 (Primer commit)
import com.example.pantallas.utils.AlertManager;
import com.example.pantallas.utils.LoggerUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
<<<<<<< HEAD
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.*;

public class VentaController {

    @FXML private VBox viewVentas, viewEnvios, viewCobro;
    @FXML private Button btnNavVentas, btnNavEnvios;
=======
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VentaController {

    @FXML private VBox viewVentas, viewCobro, viewNuevoCliente;
    @FXML private Button btnNavFacturacion;
    @FXML private RadioButton rbLibras, rbKilos;
    @FXML private ToggleGroup tgUnidadMedida;
>>>>>>> bb658e7 (Primer commit)

    @FXML private TableView<ItemQueso> tablaVentas;
    @FXML private TableColumn<ItemQueso, String> colDesc;
    @FXML private TableColumn<ItemQueso, Double> colLbs, colPre, colSub;
    @FXML private TextField txtLibras, txtPrecioUnitario, txtImporteTotal, txtEfectivoRecibido;
<<<<<<< HEAD
    @FXML private Label lblTotal, lblMontoCobro;
=======
    @FXML private TextField txtNewNombre, txtNewRnc, txtNewTel;
    @FXML private Label lblTotal, lblMontoCobro, lblDevuelta;
>>>>>>> bb658e7 (Primer commit)
    @FXML private ComboBox<String> cbProducto, cbCliente, comboMetodoPago;

    private final ObservableList<ItemQueso> listaVenta = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colLbs.setCellValueFactory(new PropertyValueFactory<>("libras"));
        colPre.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSub.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        tablaVentas.setItems(listaVenta);

        comboMetodoPago.setItems(FXCollections.observableArrayList("Efectivo", "Tarjeta", "Transferencia"));

        tablaVentas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cbProducto.setValue(newSelection.getDescripcion());
                txtLibras.setText(String.valueOf(newSelection.getLibras()));
                txtPrecioUnitario.setText(String.valueOf(newSelection.getPrecioUnitario()));
                calcularItem();
            }
        });

<<<<<<< HEAD
=======
        tgUnidadMedida.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (cbProducto.getValue() != null) cargarPrecioSugerido();
        });

>>>>>>> bb658e7 (Primer commit)
        cargarDatosDesdeDB();
    }

    private void cargarDatosDesdeDB() {
        try (Connection con = ConnectionManager.getConnection()) {
<<<<<<< HEAD
            try (ResultSet rsC = con.createStatement().executeQuery("SELECT nombre_cliente as nombre FROM Clientes")) {
                cbCliente.getItems().clear();
                while (rsC.next()) cbCliente.getItems().add(rsC.getString("nombre"));
            } catch (SQLException ignored) {
                LoggerUtil.warning("No se pudo cargar tabla Clientes, intentando tbl_clientes");
                try (ResultSet rsC2 = con.createStatement().executeQuery("SELECT nombre FROM tbl_clientes")) {
                    while (rsC2.next()) cbCliente.getItems().add(rsC2.getString("nombre"));
                }
            }

            try (ResultSet rsP = con.createStatement().executeQuery("SELECT nombre_producto as nombre FROM Productos")) {
                cbProducto.getItems().clear();
                while (rsP.next()) cbProducto.getItems().add(rsP.getString("nombre"));
            } catch (SQLException ignored) {
                LoggerUtil.warning("No se pudo cargar tabla Productos, intentando tbl_productos");
                try (ResultSet rsP2 = con.createStatement().executeQuery("SELECT nombre FROM tbl_productos")) {
                    while (rsP2.next()) cbProducto.getItems().add(rsP2.getString("nombre"));
                }
=======
            cbCliente.getItems().clear();
            if (!cargarLista(con, cbCliente, "Clientes", "nombre_cliente")
                && !cargarLista(con, cbCliente, "tbl_clientes", "nombre")) {
                LoggerUtil.warning("No se encontraron clientes en la base de datos.");
            }

            cbProducto.getItems().clear();
            if (!cargarLista(con, cbProducto, "Productos", "nombre_producto")
                && !cargarLista(con, cbProducto, "tbl_productos", "nombre")) {
                LoggerUtil.warning("No se encontraron productos en la base de datos.");
>>>>>>> bb658e7 (Primer commit)
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error cargando datos para ventas", e);
            AlertManager.showError("Error de Base de Datos", "No se pudieron cargar clientes o productos.");
        }
    }

<<<<<<< HEAD
    @FXML private void mostrarVentas() { alternarVista(viewVentas, btnNavVentas); }
    @FXML private void mostrarEnvios() { alternarVista(viewEnvios, btnNavEnvios); }
    @FXML private void irACobro() {
=======
    private boolean cargarLista(Connection con, ComboBox<String> combo, String tabla, String columna) {
        try (ResultSet rs = con.createStatement().executeQuery(
                "SELECT " + columna + " AS nombre FROM " + tabla)) {
            boolean found = false;
            while (rs.next()) {
                combo.getItems().add(rs.getString("nombre"));
                found = true;
            }
            return found;
        } catch (SQLException e) {
            return false;
        }
    }

    private double obtenerPrecioProducto(String nombreProducto) {
        double precio = 0.0;
        try (Connection con = ConnectionManager.getConnection()) {
            precio = consultarPrecio(con, "Productos", "nombre_producto", "precio_venta_base", nombreProducto);
            if (precio == 0.0) precio = consultarPrecio(con, "tbl_productos", "nombre", "precio_libra", nombreProducto);
            if (precio == 0.0) precio = consultarPrecio(con, "tbl_productos", "nombre", "precio_venta_base", nombreProducto);
        } catch (SQLException e) {
            LoggerUtil.warning("Error consultando precio: " + e.getMessage());
        }
        return precio;
    }

    private double consultarPrecio(Connection con, String tabla, String colNombre, String colPrecio, String producto) {
        String sql = "SELECT " + colPrecio + " FROM " + tabla + " WHERE " + colNombre + " = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(colPrecio);
        } catch (SQLException e) {
            LoggerUtil.warning("No se pudo consultar " + tabla + "." + colPrecio + ": " + e.getMessage());
        }
        return 0.0;
    }

    @FXML
    private void cargarPrecioSugerido() {
        String producto = cbProducto.getValue();
        if (producto == null || producto.isEmpty()) return;
        double precioBase = obtenerPrecioProducto(producto);
        boolean esKilos = rbKilos.isSelected();
        double precioFinal = ServicioVentas.calcularPrecioUnitario(precioBase, esKilos);
        txtPrecioUnitario.setText(String.format("%.2f", precioFinal));
        calcularItem();
    }

    @FXML private void mostrarVentas() { alternarVista(viewVentas, btnNavFacturacion); }

    @FXML private void mostrarCobro() {
>>>>>>> bb658e7 (Primer commit)
        if (listaVenta.isEmpty()) return;
        lblMontoCobro.setText(lblTotal.getText());
        alternarVista(viewCobro, null);
    }

<<<<<<< HEAD
    private void alternarVista(VBox vista, Button btn) {
        viewVentas.setVisible(false); viewVentas.setManaged(false);
        viewEnvios.setVisible(false); viewEnvios.setManaged(false);
        viewCobro.setVisible(false); viewCobro.setManaged(false);
        vista.setVisible(true); vista.setManaged(true);
        if(btn != null) {
            btnNavVentas.getStyleClass().remove("nav-item-active");
            btnNavEnvios.getStyleClass().remove("nav-item-active");
            if (!btnNavVentas.getStyleClass().contains("nav-item")) btnNavVentas.getStyleClass().add("nav-item");
            if (!btnNavEnvios.getStyleClass().contains("nav-item")) btnNavEnvios.getStyleClass().add("nav-item");
            
=======
    @FXML private void mostrarNuevoCliente() {
        txtNewNombre.clear();
        txtNewRnc.clear();
        txtNewTel.clear();
        alternarVista(viewNuevoCliente, null);
    }

    private void alternarVista(VBox vista, Button btn) {
        viewVentas.setVisible(false); viewVentas.setManaged(false);
        viewCobro.setVisible(false); viewCobro.setManaged(false);
        viewNuevoCliente.setVisible(false); viewNuevoCliente.setManaged(false);
        vista.setVisible(true); vista.setManaged(true);
        if (btn != null) {
            btnNavFacturacion.getStyleClass().remove("nav-item-active");
            if (!btnNavFacturacion.getStyleClass().contains("nav-item")) btnNavFacturacion.getStyleClass().add("nav-item");
>>>>>>> bb658e7 (Primer commit)
            btn.getStyleClass().remove("nav-item");
            btn.getStyleClass().add("nav-item-active");
        }
    }

    @FXML private void calcularItem() {
        try {
            double lbs = Double.parseDouble(txtLibras.getText());
            double pre = Double.parseDouble(txtPrecioUnitario.getText());
            txtImporteTotal.setText(String.format("%.2f", lbs * pre));
        } catch (Exception e) { txtImporteTotal.setText("0.00"); }
    }

    @FXML private void añadirItem() {
<<<<<<< HEAD
        if (cbProducto.getValue() == null) return;

        ItemQueso existente = tablaVentas.getSelectionModel().getSelectedItem();
        if (existente != null) listaVenta.remove(existente);

        listaVenta.add(new ItemQueso(cbProducto.getValue(), Double.parseDouble(txtLibras.getText()), Double.parseDouble(txtPrecioUnitario.getText())));
=======
        if (cbProducto.getValue() == null) {
            AlertManager.showError("Producto requerido", "Seleccione un producto.");
            return;
        }
        String lbsText = txtLibras.getText();
        String preText = txtPrecioUnitario.getText();
        if (lbsText == null || lbsText.trim().isEmpty()) {
            AlertManager.showError("Cantidad requerida", "Ingrese la cantidad.");
            return;
        }
        if (preText == null || preText.trim().isEmpty()) {
            AlertManager.showError("Precio requerido", "El precio unitario es requerido.");
            return;
        }
        double lbs, pre;
        try {
            lbs = Double.parseDouble(lbsText.trim());
            pre = Double.parseDouble(preText.trim());
        } catch (NumberFormatException e) {
            AlertManager.showError("Valor invalido", "Ingrese valores numericos validos para cantidad y precio.");
            return;
        }
        ItemQueso existente = tablaVentas.getSelectionModel().getSelectedItem();
        if (existente != null) listaVenta.remove(existente);
        listaVenta.add(new ItemQueso(cbProducto.getValue(), lbs, pre));
>>>>>>> bb658e7 (Primer commit)
        actualizarTotal();
        limpiarCampos();
    }

    @FXML private void borrarItem() {
        ItemQueso sel = tablaVentas.getSelectionModel().getSelectedItem();
        if (sel != null) {
            listaVenta.remove(sel);
            actualizarTotal();
            limpiarCampos();
        }
    }

    @FXML private void limpiarCampos() {
        cbProducto.setValue(null);
        txtLibras.clear();
        txtPrecioUnitario.clear();
        txtImporteTotal.clear();
        tablaVentas.getSelectionModel().clearSelection();
    }

    private void actualizarTotal() {
        double total = listaVenta.stream().mapToDouble(ItemQueso::getSubtotal).sum();
        lblTotal.setText(String.format("RD$ %.2f", total));
    }

<<<<<<< HEAD
    @FXML private void finalizarPago() {
        LoggerUtil.info("Procesando pago en base de datos...");
        // Aquí iría el INSERT a tbl_ventas y tbl_detalle_venta
        AlertManager.showInfo("Venta Procesada", "La venta se ha guardado correctamente.");
=======
    @FXML
    private void finalizarVenta() {
        if (comboMetodoPago.getValue() == null) {
            AlertManager.showError("Metodo de Pago", "Seleccione un metodo de pago.");
            return;
        }
        if (cbCliente.getValue() == null || cbCliente.getValue().isEmpty()) {
            AlertManager.showError("Cliente requerido", "Seleccione un cliente antes de procesar el pago.");
            return;
        }
        if (listaVenta.isEmpty()) {
            AlertManager.showError("Carrito vacio", "Agregue productos al carrito antes de procesar el pago.");
            return;
        }

        String metodo = comboMetodoPago.getValue();

        if ("Efectivo".equals(metodo)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Gestion de Cobro - Empresa");
            alert.setHeaderText("Aviso de Notificacion de Pago");
            alert.setContentText("se le llamara cerca de la fecha de entrega para hacer el pago y recibir su producto.");
            alert.show();
        }

        try {
            String facturaNo = "VTA-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            String cliente = cbCliente.getValue();
            double subtotalVal = listaVenta.stream().mapToDouble(ItemQueso::getSubtotal).sum();
            double itbisVal = subtotalVal * 0.18;
            double totalVal = subtotalVal + itbisVal;
            Date hoy = new Date();

            Map<String, Object> params = new HashMap<>();
            params.put("numeroFactura", facturaNo);
            params.put("cliente", cliente);
            params.put("fecha", hoy);
            params.put("subtotal", subtotalVal);
            params.put("itbis", itbisVal);
            params.put("total", totalVal);
            params.put("metodoPago", metodo);

            String rutaPdf = System.getProperty("java.io.tmpdir") + File.separator + facturaNo + ".pdf";
            ReporteHelper.generarReporteConDatos("factura.jrxml", params, listaVenta, rutaPdf);

            LoggerUtil.info("Factura generada: " + rutaPdf);
            AlertManager.showInfo("Venta Procesada", "Factura " + facturaNo + " generada correctamente.");

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(rutaPdf));
            }
        } catch (Exception e) {
            LoggerUtil.error("Error generando factura", e);
            AlertManager.showError("Error de Factura",
                "La venta se proceso pero no se pudo generar la factura.\n" + e.getMessage());
        }

>>>>>>> bb658e7 (Primer commit)
        listaVenta.clear();
        actualizarTotal();
        mostrarVentas();
    }

    @FXML
<<<<<<< HEAD
    private void irAMenuPrincipal() {
=======
    private void guardarCliente() {
        String nombre = txtNewNombre.getText();
        String rnc = txtNewRnc.getText();
        String tel = txtNewTel.getText();
        if (nombre == null || nombre.trim().isEmpty()) {
            AlertManager.showError("Campo requerido", "El nombre del cliente es obligatorio.");
            return;
        }
        String rncVal = (rnc == null || rnc.trim().isEmpty()) ? "S/N" : rnc.trim();
        String telVal = (tel == null || tel.trim().isEmpty()) ? "" : tel.trim();

        String sql = "INSERT INTO Clientes (nombre_cliente, rnc_cedula, telefono) VALUES (?, ?, ?)";
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setString(2, rncVal);
            ps.setString(3, telVal);
            ps.executeUpdate();
            AlertManager.showInfo("Cliente Registrado", "Cliente guardado correctamente en Clientes.");
            txtNewNombre.clear(); txtNewRnc.clear(); txtNewTel.clear();
            cargarDatosDesdeDB();
            mostrarVentas();
            return;
        } catch (SQLException e) {
            LoggerUtil.error("Error guardando en Clientes: " + e.getMessage());
        }
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO tbl_clientes (nombre, rnc_cedula, telefono) VALUES (?, ?, ?)")) {
            ps.setString(1, nombre.trim());
            ps.setString(2, rncVal);
            ps.setString(3, telVal);
            ps.executeUpdate();
            AlertManager.showInfo("Cliente Registrado", "Cliente guardado correctamente en tbl_clientes.");
            txtNewNombre.clear(); txtNewRnc.clear(); txtNewTel.clear();
            cargarDatosDesdeDB();
            mostrarVentas();
        } catch (SQLException e2) {
            AlertManager.showError("Error al guardar cliente",
                "No se pudo guardar en ninguna tabla.\n" + e2.getMessage());
        }
    }

    @FXML
    private void calcularDevuelta() {
        try {
            String totalText = lblTotal.getText().replace("RD$ ", "").trim();
            double total = Double.parseDouble(totalText);
            double recibido = Double.parseDouble(txtEfectivoRecibido.getText());
            double devuelta = recibido - total;
            lblDevuelta.setText(String.format("Devuelta: RD$ %.2f", devuelta));
        } catch (Exception e) {
            lblDevuelta.setText("Devuelta: RD$ 0.00");
        }
    }

    @FXML
    private void volverAlMenu() {
>>>>>>> bb658e7 (Primer commit)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tablaVentas.getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.setMaximized(wasMaximized);
            if (!wasMaximized) stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
<<<<<<< HEAD
            LoggerUtil.error("Error al regresar al menú principal", e);
=======
            LoggerUtil.error("Error al regresar al menu principal", e);
>>>>>>> bb658e7 (Primer commit)
        }
    }

    public static class ItemQueso {
        private String descripcion; private double libras, precioUnitario, subtotal;
        public ItemQueso(String d, double l, double p) { this.descripcion = d; this.libras = l; this.precioUnitario = p; this.subtotal = l * p; }
        public String getDescripcion() { return descripcion; }
        public double getLibras() { return libras; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getSubtotal() { return subtotal; }
    }
}
<<<<<<< HEAD

=======
>>>>>>> bb658e7 (Primer commit)
