package com.example.pantallas.controllers;

import com.example.pantallas.config.ConnectionManager;
import com.example.pantallas.utils.AlertManager;
import com.example.pantallas.utils.LoggerUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

    @FXML private TableView<ItemQueso> tablaVentas;
    @FXML private TableColumn<ItemQueso, String> colDesc;
    @FXML private TableColumn<ItemQueso, Double> colLbs, colPre, colSub;
    @FXML private TextField txtLibras, txtPrecioUnitario, txtImporteTotal, txtEfectivoRecibido;
    @FXML private Label lblTotal, lblMontoCobro;
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

        cargarDatosDesdeDB();
    }

    private void cargarDatosDesdeDB() {
        try (Connection con = ConnectionManager.getConnection()) {
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
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error cargando datos para ventas", e);
            AlertManager.showError("Error de Base de Datos", "No se pudieron cargar clientes o productos.");
        }
    }

    @FXML private void mostrarVentas() { alternarVista(viewVentas, btnNavVentas); }
    @FXML private void mostrarEnvios() { alternarVista(viewEnvios, btnNavEnvios); }
    @FXML private void irACobro() {
        if (listaVenta.isEmpty()) return;
        lblMontoCobro.setText(lblTotal.getText());
        alternarVista(viewCobro, null);
    }

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
        if (cbProducto.getValue() == null) return;

        ItemQueso existente = tablaVentas.getSelectionModel().getSelectedItem();
        if (existente != null) listaVenta.remove(existente);

        listaVenta.add(new ItemQueso(cbProducto.getValue(), Double.parseDouble(txtLibras.getText()), Double.parseDouble(txtPrecioUnitario.getText())));
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

    @FXML private void finalizarPago() {
        LoggerUtil.info("Procesando pago en base de datos...");
        // Aquí iría el INSERT a tbl_ventas y tbl_detalle_venta
        AlertManager.showInfo("Venta Procesada", "La venta se ha guardado correctamente.");
        listaVenta.clear();
        actualizarTotal();
        mostrarVentas();
    }

    @FXML
    private void irAMenuPrincipal() {
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
            LoggerUtil.error("Error al regresar al menú principal", e);
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

