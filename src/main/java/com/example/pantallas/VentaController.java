package com.example.pantallas;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.sql.*;

public class VentaController {

    // Paneles para Navegación
    @FXML private VBox viewVentas, viewEnvios, viewCobro;
    @FXML private Button btnNavVentas, btnNavEnvios;

    // Componentes Ventas
    @FXML private TableView<ItemQueso> tablaVentas;
    @FXML private TableColumn<ItemQueso, String> colDesc;
    @FXML private TableColumn<ItemQueso, Double> colLbs, colPre, colSub;
    @FXML private TextField txtLibras, txtPrecioUnitario, txtImporteTotal, txtEfectivoRecibido;
    @FXML private Label lblTotal, lblMontoCobro;
    @FXML private ComboBox<String> cbProducto, cbCliente, comboMetodoPago;

    private final ObservableList<ItemQueso> listaVenta = FXCollections.observableArrayList();

    // Configuración JDBC
    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;encrypt=true;trustServerCertificate=true;";
    private final String USER = "janciel";
    private final String PASS = "123456789";

    @FXML
    public void initialize() {
        // Configurar Tabla
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colLbs.setCellValueFactory(new PropertyValueFactory<>("libras"));
        colPre.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSub.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        tablaVentas.setItems(listaVenta);

        // Combos
        comboMetodoPago.setItems(FXCollections.observableArrayList("Efectivo", "Tarjeta", "Transferencia"));

        // Listener para EDITAR: Al hacer click en la tabla, sube los datos a los campos
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
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            // Clientes
            ResultSet rsC = con.createStatement().executeQuery("SELECT nombre FROM tbl_clientes");
            cbCliente.getItems().clear();
            while (rsC.next()) cbCliente.getItems().add(rsC.getString("nombre"));

            // Productos
            ResultSet rsP = con.createStatement().executeQuery("SELECT nombre FROM tbl_productos");
            cbProducto.getItems().clear();
            while (rsP.next()) cbProducto.getItems().add(rsP.getString("nombre"));
        } catch (SQLException e) { System.err.println("Error DB: " + e.getMessage()); }
    }

    // --- NAVEGACIÓN ---
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
            btnNavVentas.getStyleClass().remove("nav-button-active");
            btnNavEnvios.getStyleClass().remove("nav-button-active");
            btn.getStyleClass().add("nav-button-active");
        }
    }

    // --- FUNCIONES CORE ---
    @FXML private void calcularItem() {
        try {
            double lbs = Double.parseDouble(txtLibras.getText());
            double pre = Double.parseDouble(txtPrecioUnitario.getText());
            txtImporteTotal.setText(String.format("%.2f", lbs * pre));
        } catch (Exception e) { txtImporteTotal.setText("0.00"); }
    }

    @FXML private void añadirItem() {
        if (cbProducto.getValue() == null) return;

        // LÓGICA ACTUALIZAR: Si el item ya existe en la tabla, lo reemplazamos
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
        System.out.println("Venta procesada con éxito");
        listaVenta.clear();
        actualizarTotal();
        mostrarVentas();
    }

    // Clase Modelo
    public static class ItemQueso {
        private String descripcion; private double libras, precioUnitario, subtotal;
        public ItemQueso(String d, double l, double p) { this.descripcion = d; this.libras = l; this.precioUnitario = p; this.subtotal = l * p; }
        public String getDescripcion() { return descripcion; }
        public double getLibras() { return libras; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getSubtotal() { return subtotal; }
    }
}