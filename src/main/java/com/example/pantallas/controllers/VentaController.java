package com.example.pantallas.controllers;

import com.example.pantallas.ReporteHelper;
import com.example.pantallas.config.ConnectionManager;
import com.example.pantallas.models.ItemQueso;
import com.example.pantallas.utils.AlertManager;
import com.example.pantallas.utils.LoggerUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VentaController {

    private static final Map<String, String> IMAGEN_QUESOS = new HashMap<>();
    static {
        IMAGEN_QUESOS.put("queso crema", "QuesoCrema");
        IMAGEN_QUESOS.put("queso mozzarella", "QuesoMozarrella");
        IMAGEN_QUESOS.put("queso blanco", "QuesoBlancoFresco");
        IMAGEN_QUESOS.put("queso blanco fresco", "QuesoBlancoFresco");
        IMAGEN_QUESOS.put("queso parmesano", "QuesoParmesano");
        IMAGEN_QUESOS.put("queso gouda", "QuesoGouda");
        IMAGEN_QUESOS.put("queso camembert", "QuesoCamembert");
        IMAGEN_QUESOS.put("queso de bola", "QuesoDeBola");
        IMAGEN_QUESOS.put("queso ricotta", "QuesoRicota");
        IMAGEN_QUESOS.put("queso romano", "QuesoRomano");
        IMAGEN_QUESOS.put("queso campesino", "QuesoCampesino");
        IMAGEN_QUESOS.put("queso amarillo", "QuesoAmarillo");
        IMAGEN_QUESOS.put("queso cheddar", "QuesoCheddar");
        IMAGEN_QUESOS.put("queso de hoja", "QuesoDeHoja");
        IMAGEN_QUESOS.put("queso suizo", "QuesoSuizo");
    }

    @FXML private VBox viewVentas, viewEnvios, viewCobro, viewNuevoCliente, viewHistorial;
    @FXML private Button btnNavVentas, btnNavEnvios, btnNavFacturacion, btnNavHistorial;
    @FXML private TableView<VentaHistorial> tablaHistorialVentas;
    @FXML private TableColumn<VentaHistorial, Integer> colHvId;
    @FXML private TableColumn<VentaHistorial, String> colHvCliente, colHvFecha, colHvMetodo, colHvTipo;
    @FXML private TableColumn<VentaHistorial, Double> colHvTotal;
    @FXML private FlowPane productGrid;
    @FXML private Label lblProductoSeleccionado;
    @FXML private TextField txtCantidad, txtPrecioUnitario, txtImporteTotal, txtEfectivoRecibido;
    @FXML private TextField txtLibras;
    @FXML private TextField txtNewNombre, txtNewRnc, txtNewTel;
    @FXML private ComboBox<String> cbUnidadMedida, cbCliente, comboMetodoPago;
    @FXML private Label lblTotal, lblMontoCobro, lblDevuelta;
    @FXML private TableView<ItemQueso> tablaVentas;
    @FXML private TableColumn<ItemQueso, String> colDesc;
    @FXML private TableColumn<ItemQueso, Double> colLbs, colPre, colSub;
    @FXML private TableColumn<ItemQueso, String> colUnidad;
    @FXML private ToggleGroup tgEntrega;
    @FXML private RadioButton rbTienda, rbEnvio;
    @FXML private VBox panelEnvio;
    @FXML private ComboBox<String> cbRepartidor;
    @FXML private TextField txtDireccion, txtApartamento, txtSector, txtCiudad;

    private final ObservableList<ItemQueso> listaVenta = FXCollections.observableArrayList();
    private String productoSeleccionado;
    private static final String COLOR_CARD_BG = "#ffffff";
    private static final String COLOR_STOCK_OK = "#2ecc71";
    private static final String COLOR_NO_STOCK = "#e74c3c";

    @FXML
    public void initialize() {
        if (colDesc != null) colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        if (colLbs != null) colLbs.setCellValueFactory(new PropertyValueFactory<>("libras"));
        if (colPre != null) colPre.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        if (colSub != null) colSub.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        if (colUnidad != null) colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidadMedida"));
        if (tablaVentas != null) tablaVentas.setItems(listaVenta);

        if (comboMetodoPago != null)
            comboMetodoPago.setItems(FXCollections.observableArrayList("Efectivo", "Tarjeta de Débito", "Tarjeta de Crédito"));
        if (cbUnidadMedida != null)
            cbUnidadMedida.setItems(FXCollections.observableArrayList("Libras", "Kilos", "Paquete"));

        if (tablaVentas != null) {
            tablaVentas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null && lblProductoSeleccionado != null) {
                    lblProductoSeleccionado.setText(newSelection.getDescripcion());
                    productoSeleccionado = newSelection.getDescripcion();
                    if (txtCantidad != null) txtCantidad.setText(String.valueOf(newSelection.getCantidad()));
                    if (txtPrecioUnitario != null) txtPrecioUnitario.setText(String.valueOf(newSelection.getPrecioUnitario()));
                    if (cbUnidadMedida != null) cbUnidadMedida.setValue(newSelection.getUnidadMedida());
                    calcularItem();
                }
            });
        }

        if (tgEntrega != null) tgEntrega.selectedToggleProperty().addListener((obs, oldVal, newVal) -> toggleEnvio());

        if (cbCliente != null) cargarDatosDesdeDB();
        if (cbRepartidor != null) cargarRepartidores();

        if (productGrid != null && cbCliente != null) cargarDatosDesdeDB();
        else if (productGrid != null) cargarProductosDesdeDB();

        if (colHvId != null) colHvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colHvCliente != null) colHvCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        if (colHvFecha != null) colHvFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        if (colHvTotal != null) colHvTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        if (colHvMetodo != null) colHvMetodo.setCellValueFactory(new PropertyValueFactory<>("metodo"));
        if (colHvTipo != null) colHvTipo.setCellValueFactory(new PropertyValueFactory<>("tipoEntrega"));
    }

    private void cargarProductosDesdeDB() {
        try (Connection con = ConnectionManager.getConnection()) {
            cargarProductos(con);
        } catch (SQLException e) {
            cargarProductosDemo();
        }
    }

    private void toggleEnvio() {
        if (rbEnvio == null || panelEnvio == null) return;
        boolean envio = rbEnvio.isSelected();
        panelEnvio.setVisible(envio);
        panelEnvio.setManaged(envio);
        panelEnvio.setDisable(!envio);
    }

    private void cargarDatosDesdeDB() {
        try (Connection con = ConnectionManager.getConnection()) {
            cbCliente.getItems().clear();
            if (!cargarLista(con, cbCliente, "Clientes", "nombre_cliente")
                && !cargarLista(con, cbCliente, "tbl_clientes", "nombre")) {
                LoggerUtil.warning("No se encontraron clientes en la base de datos.");
            }
            cargarProductos(con);
        } catch (SQLException e) {
            LoggerUtil.error("Error cargando datos para ventas", e);
            AlertManager.showError("Error de Base de Datos", "No se pudieron cargar datos.");
        }
    }

    private void cargarProductos(Connection con) {
        productGrid.getChildren().clear();
        Set<String> agregados = new HashSet<>();
        String[][] tablas = {
            {"Productos", "nombre_producto", "precio_venta_base", "stock_actual", "nombre_producto LIKE '%Queso%' AND activo = 1"},
            {"tbl_productos", "nombre", "precio_libra", "cantidad_stock", "nombre LIKE '%Queso%'"},
            {"tbl_inventario_productos", "nombre_producto", "ultimo_costo", "cantidad_stock", "nombre_producto LIKE '%Queso%'"}
        };
        boolean cargo = false;
        for (String[] t : tablas) {
            String sql = "SELECT " + t[1] + ", " + t[2] + ", " + t[3] + " FROM " + t[0] + " WHERE " + t[4];
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String nombre = rs.getString(t[1]);
                    if (!agregados.add(nombre)) continue;
                    double precio = rs.getDouble(t[2]);
                    double stock = rs.getDouble(t[3]);
                    productGrid.getChildren().add(crearCardProducto(nombre, precio, stock));
                    cargo = true;
                }
                if (cargo) break;
            } catch (SQLException ignored) {}
        }
        if (!cargo) {
            cargarProductosDemo();
        }
    }

    private void cargarProductosDemo() {
        String[][] demo = {
            {"Queso Crema", "280.00", "15"},
            {"Queso Mozzarella", "320.00", "5"},
            {"Queso Blanco", "250.00", "12"},
            {"Queso Blanco Fresco", "270.00", "10"},
            {"Queso Amarillo", "290.00", "5"},
            {"Queso Cheddar", "350.00", "0"},
            {"Queso Parmesano", "420.00", "3"},
            {"Queso Gouda", "380.00", "7"},
            {"Queso Camembert", "450.00", "0"},
            {"Queso de Bola", "520.00", "4"},
            {"Queso Ricotta", "230.00", "10"}
        };
        for (String[] d : demo) {
            productGrid.getChildren().add(
                crearCardProducto(d[0], Double.parseDouble(d[1]), Double.parseDouble(d[2]))
            );
        }
    }

    private VBox crearCardProducto(String nombre, double precioLibra, double stock) {
        VBox card = new VBox(8);
        card.setPrefWidth(170);
        card.setPrefHeight(240);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: " + COLOR_CARD_BG + "; -fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        card.setEffect(new DropShadow(8, 0, 2, Color.rgb(0, 0, 0, 0.08)));

        ImageView imgView = new ImageView();
        imgView.setFitWidth(80);
        imgView.setFitHeight(80);
        imgView.setPreserveRatio(true);
        String imgBase = IMAGEN_QUESOS.get(nombre.trim().toLowerCase());
        if (imgBase != null) {
            Image loaded = null;
            String[] exts = {".png", ".jpg", ".jpeg", ".webp"};
            for (String ext : exts) {
                String path = "/imagenes quesos/" + imgBase + ext;
                try (java.io.InputStream is = getClass().getResourceAsStream(path)) {
                    if (is != null) {
                        Image img = new Image(is);
                        if (!img.isError()) { loaded = img; break; }
                    }
                } catch (Exception ignored) {}
            }
            if (loaded != null) imgView.setImage(loaded);
            else imgView = null;
        }
        if (imgView == null) {
            StackPane fallback = new StackPane();
            fallback.setPrefSize(60, 60);
            fallback.setStyle("-fx-background-color: #f0f4f8; -fx-background-radius: 30;");
            Label icon = new Label("🧀");
            icon.setStyle("-fx-font-size: 28;");
            fallback.getChildren().add(icon);
            card.getChildren().add(fallback);
        } else {
            card.getChildren().add(imgView);
        }

        Label lblNombre = new Label(nombre);
        lblNombre.setWrapText(true);
        lblNombre.setTextAlignment(TextAlignment.CENTER);
        lblNombre.setStyle("-fx-font-weight: 700; -fx-font-size: 13; -fx-text-fill: #1e293b;");

        Label lblPrecio = new Label("RD$ " + String.format("%.2f", precioLibra) + " / lb");
        lblPrecio.setStyle("-fx-font-weight: 600; -fx-font-size: 14; -fx-text-fill: #0f3d24;");

        Label lblStock;
        Button btnAdd = new Button("Añadir al carrito");
        btnAdd.setStyle("-fx-background-color: #0f3d24; -fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 11; -fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;");

        if (stock <= 0) {
            lblStock = new Label("No disponible");
            lblStock.setStyle("-fx-font-weight: 700; -fx-font-size: 12; -fx-text-fill: " + COLOR_NO_STOCK + ";");
            btnAdd.setDisable(true);
            btnAdd.setStyle("-fx-background-color: #cbd5e1; -fx-text-fill: #94a3b8; -fx-font-weight: 700; -fx-font-size: 11; -fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: default;");
        } else {
            String stockText = (int) stock + (stock == 1 ? " paquete" : " paquetes") + " en stock";
            lblStock = new Label(stockText);
            lblStock.setStyle("-fx-font-weight: 600; -fx-font-size: 11; -fx-text-fill: " + COLOR_STOCK_OK + ";");
            btnAdd.setOnAction(e -> seleccionarProducto(nombre, precioLibra));
        }

        btnAdd.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(lblNombre, lblPrecio, lblStock, btnAdd);
        VBox.setVgrow(btnAdd, Priority.ALWAYS);
        return card;
    }

    private void seleccionarProducto(String nombre, double precio) {
        productoSeleccionado = nombre;
        lblProductoSeleccionado.setText(nombre);
        txtCantidad.setText("1");
        cbUnidadMedida.setValue("Libras");
        txtPrecioUnitario.setText(String.format("%.2f", precio));
        calcularItem();
    }

    @FXML
    private void mostrarVentas() { alternarVista(viewVentas, btnNavFacturacion); }

    @FXML
    private void mostrarCobro() {
        if (listaVenta.isEmpty()) {
            AlertManager.showError("Carrito vacio", "Agregue productos al carrito antes de procesar el pago.");
            return;
        }
        lblMontoCobro.setText(lblTotal.getText());
        alternarVista(viewCobro, null);
    }

    @FXML
    private void mostrarEnvios() { alternarVista(viewEnvios, btnNavEnvios); }

    @FXML
    private void mostrarHistorialVentas() {
        alternarVista(viewHistorial, btnNavHistorial);
        cargarHistorialVentas();
    }

    private void cargarHistorialVentas() {
        ObservableList<VentaHistorial> items = FXCollections.observableArrayList();
        String sql = "SELECT nombre_cliente, metodo_pago, tipo_entrega, total_venta, fecha_venta "
                + "FROM Ventas WHERE fecha_venta >= DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()), 0) "
                + "ORDER BY fecha_venta DESC";
        try (Connection con = ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            int seq = 1;
            while (rs.next()) {
                items.add(new VentaHistorial(seq++, rs.getString("nombre_cliente"),
                        rs.getString("fecha_venta"), rs.getDouble("total_venta"),
                        rs.getString("metodo_pago"), rs.getString("tipo_entrega")));
            }
        } catch (SQLException e) {
            String sql2 = "SELECT cliente, metodo_pago, tipo_entrega, monto_total, fecha "
                    + "FROM tbl_ventas WHERE fecha >= DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()), 0) "
                    + "ORDER BY fecha DESC";
            try (Connection con = ConnectionManager.getConnection();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql2)) {
                int seq = 1;
                while (rs.next()) {
                    items.add(new VentaHistorial(seq++, rs.getString("cliente"),
                            rs.getString("fecha"), rs.getDouble("monto_total"),
                            rs.getString("metodo_pago"), rs.getString("tipo_entrega")));
                }
            } catch (SQLException ex) {
                LoggerUtil.warning("No se pudo cargar historial de ventas");
            }
        }
        if (items.isEmpty()) {
            items.add(new VentaHistorial(1, "Juan Pérez", "2026-05-20 10:30", 4500.00, "Efectivo", "En tienda (Caja)"));
            items.add(new VentaHistorial(2, "María Rodríguez", "2026-05-19 15:45", 8200.00, "Transferencia", "A domicilio"));
            items.add(new VentaHistorial(3, "Supermercado XYZ", "2026-05-18 09:15", 15600.00, "Crédito", "En tienda (Caja)"));
        }
        if (tablaHistorialVentas != null) tablaHistorialVentas.setItems(items);
    }

    @FXML
    private void mostrarNuevoCliente() {
        txtNewNombre.clear();
        txtNewRnc.clear();
        txtNewTel.clear();
        alternarVista(viewNuevoCliente, null);
    }

    private void alternarVista(VBox vista, Button btn) {
        for (VBox v : new VBox[]{viewVentas, viewEnvios, viewCobro, viewNuevoCliente, viewHistorial}) {
            if (v != null) { v.setVisible(false); v.setManaged(false); }
        }
        vista.setVisible(true); vista.setManaged(true);
        if (btn != null) {
            for (Button b : new Button[]{btnNavVentas, btnNavEnvios, btnNavFacturacion, btnNavHistorial}) {
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
    private void calcularItem() {
        try {
            double cant = Double.parseDouble(txtCantidad.getText());
            double pre = Double.parseDouble(txtPrecioUnitario.getText());
            txtImporteTotal.setText(String.format("%.2f", cant * pre));
        } catch (Exception e) { txtImporteTotal.setText("0.00"); }
    }

    @FXML
    private void añadirItem() {
        if (productoSeleccionado == null) {
            AlertManager.showError("Producto requerido", "Seleccione un producto de la tienda.");
            return;
        }
        String cantText = txtCantidad.getText();
        String preText = txtPrecioUnitario.getText();
        String unidad = cbUnidadMedida.getValue();
        if (cantText == null || cantText.trim().isEmpty()) {
            AlertManager.showError("Cantidad requerida", "Ingrese la cantidad.");
            return;
        }
        if (preText == null || preText.trim().isEmpty()) {
            AlertManager.showError("Precio requerido", "El precio unitario es requerido.");
            return;
        }
        if (unidad == null) {
            AlertManager.showError("Unidad requerida", "Seleccione la unidad de medida.");
            return;
        }
        double cant, pre;
        try {
            cant = Double.parseDouble(cantText.trim());
            pre = Double.parseDouble(preText.trim());
        } catch (NumberFormatException e) {
            AlertManager.showError("Valor invalido", "Ingrese valores numericos validos.");
            return;
        }
        ItemQueso existente = tablaVentas.getSelectionModel().getSelectedItem();
        if (existente != null) listaVenta.remove(existente);
        listaVenta.add(new ItemQueso(productoSeleccionado, cant, pre, unidad));
        actualizarTotal();
        limpiarCampos();
    }

    @FXML
    private void borrarItem() {
        ItemQueso sel = tablaVentas.getSelectionModel().getSelectedItem();
        if (sel != null) {
            listaVenta.remove(sel);
            actualizarTotal();
            limpiarCampos();
        }
    }

    @FXML
    private void limpiarCampos() {
        productoSeleccionado = null;
        lblProductoSeleccionado.setText("Seleccione un producto...");
        txtCantidad.clear();
        txtPrecioUnitario.clear();
        txtImporteTotal.clear();
        cbUnidadMedida.setValue(null);
        tablaVentas.getSelectionModel().clearSelection();
    }

    private void actualizarTotal() {
        double total = listaVenta.stream().mapToDouble(ItemQueso::getSubtotal).sum();
        lblTotal.setText(String.format("RD$ %.2f", total));
    }

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

    private void cargarRepartidores() {
        try (Connection con = ConnectionManager.getConnection()) {
            cbRepartidor.getItems().clear();
            String sql = "SELECT chofer FROM tbl_vehiculos WHERE estado = 'Disponible'";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    cbRepartidor.getItems().add(rs.getString("chofer"));
                }
            }
            if (cbRepartidor.getItems().isEmpty()) {
                cbRepartidor.getItems().addAll("Repartidor por asignar", "Repartidor A", "Repartidor B");
            }
        } catch (SQLException e) {
            cbRepartidor.getItems().addAll("Repartidor por asignar", "Repartidor A", "Repartidor B");
            LoggerUtil.warning("No se pudieron cargar repartidores: " + e.getMessage());
        }
    }

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

        boolean esEnvio = rbEnvio != null && rbEnvio.isSelected();
        if (esEnvio) {
            if (cbRepartidor.getValue() == null) {
                AlertManager.showError("Repartidor requerido", "Seleccione un repartidor para el envio a domicilio.");
                return;
            }
            if (txtDireccion.getText() == null || txtDireccion.getText().trim().isEmpty()) {
                AlertManager.showError("Direccion requerida", "Ingrese la direccion de envio.");
                return;
            }
        }

        String metodo = comboMetodoPago.getValue();
        String tipoEntrega = esEnvio ? "Envio" : "Venta en sucursal";

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
            params.put("tipoEntrega", tipoEntrega);

            String rutaPdf = System.getProperty("java.io.tmpdir") + File.separator + facturaNo + ".pdf";
            ReporteHelper.generarReporteConDatos("factura.jrxml", params, listaVenta, rutaPdf);

            LoggerUtil.info("Factura generada: " + rutaPdf);
            AlertManager.showInfo("Venta Procesada", "Factura " + facturaNo + " generada correctamente.");

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(rutaPdf));
            }

            if (esEnvio) {
                guardarEnvio(cliente, totalVal);
            }

            registrarVentaEnBD(cliente, metodo, tipoEntrega, totalVal);
            descontarStock();
            refrescarProductos();

        } catch (Exception e) {
            LoggerUtil.error("Error generando factura", e);
            AlertManager.showError("Error de Factura",
                "La venta se proceso pero no se pudo generar la factura.\n" + e.getMessage());
        }

        listaVenta.clear();
        actualizarTotal();
        limpiarCampos();
        if (cbCliente != null) cbCliente.setValue(null);
        if (rbTienda != null) rbTienda.setSelected(true);
        if (txtDireccion != null) txtDireccion.clear();
        if (txtApartamento != null) txtApartamento.clear();
        if (txtSector != null) txtSector.clear();
        if (txtCiudad != null) txtCiudad.clear();
        if (cbRepartidor != null) cbRepartidor.setValue(null);
        if (txtEfectivoRecibido != null) txtEfectivoRecibido.clear();
        if (lblDevuelta != null) lblDevuelta.setText("Devuelta: RD$ 0.00");
        if (comboMetodoPago != null) comboMetodoPago.setValue(null);
        toggleEnvio();
        mostrarVentas();
    }

    private void guardarEnvio(String cliente, double total) {
        try (Connection con = ConnectionManager.getConnection()) {
            // Crear la tabla si no existe
            String createSql = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_envios' AND xtype='U') " +
                "CREATE TABLE tbl_envios (" +
                "id_envio INT IDENTITY(1,1) PRIMARY KEY, " +
                "cliente NVARCHAR(200) NOT NULL, " +
                "repartidor NVARCHAR(200), " +
                "direccion NVARCHAR(500), " +
                "estatus NVARCHAR(50) DEFAULT 'Pendiente', " +
                "monto_total DECIMAL(18,2) DEFAULT 0, " +
                "fecha_registro DATETIME DEFAULT GETDATE())";
            try (Statement st = con.createStatement()) {
                st.executeUpdate(createSql);
            }

            String repartidor = (cbRepartidor != null && cbRepartidor.getValue() != null) ? cbRepartidor.getValue() : "";
            // Construir dirección completa
            StringBuilder sb = new StringBuilder();
            if (txtDireccion  != null && !txtDireccion.getText().trim().isEmpty())  sb.append(txtDireccion.getText().trim());
            if (txtApartamento != null && !txtApartamento.getText().trim().isEmpty()) sb.append(", Apto: ").append(txtApartamento.getText().trim());
            if (txtSector != null && !txtSector.getText().trim().isEmpty())          sb.append(", ").append(txtSector.getText().trim());
            if (txtCiudad != null && !txtCiudad.getText().trim().isEmpty())          sb.append(", ").append(txtCiudad.getText().trim());
            String direccion = sb.toString();

            String sql = "INSERT INTO tbl_envios (cliente, repartidor, direccion, estatus, monto_total) VALUES (?, ?, ?, 'Pendiente', ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, cliente);
                ps.setString(2, repartidor);
                ps.setString(3, direccion);
                ps.setDouble(4, total);
                ps.executeUpdate();
                LoggerUtil.info("Envio registrado en cola de Distribucion para: " + cliente);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error registrando envio", e);
            AlertManager.showError("Error de Envio", "Venta procesada pero no se pudo registrar el envio.\n" + e.getMessage());
        }
    }

    private void registrarVentaEnBD(String cliente, String metodo, String tipoEntrega, double total) {
        String sql = "INSERT INTO Ventas (nombre_cliente, metodo_pago, tipo_entrega, total_venta, fecha_venta) VALUES (?, ?, ?, ?, GETDATE())";
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente);
            ps.setString(2, metodo);
            ps.setString(3, tipoEntrega);
            ps.setDouble(4, total);
            ps.executeUpdate();
        } catch (SQLException e) {
            try (Connection con = ConnectionManager.getConnection();
                 PreparedStatement ps2 = con.prepareStatement(
                     "INSERT INTO tbl_ventas (cliente, metodo_pago, tipo_entrega, monto_total, fecha) VALUES (?, ?, ?, ?, GETDATE())")) {
                ps2.setString(1, cliente);
                ps2.setString(2, metodo);
                ps2.setString(3, tipoEntrega);
                ps2.setDouble(4, total);
                ps2.executeUpdate();
            } catch (SQLException e2) {
                LoggerUtil.warning("No se pudo registrar venta en BD: " + e2.getMessage());
            }
        }
    }

    private void descontarStock() {
        for (ItemQueso item : listaVenta) {
            String sql = "UPDATE Productos SET stock_actual = stock_actual - ? WHERE nombre_producto = ? AND stock_actual >= ?";
            try (Connection con = ConnectionManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, item.getCantidad());
                ps.setString(2, item.getDescripcion());
                ps.setDouble(3, item.getCantidad());
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String sql2 = "UPDATE tbl_inventario_productos SET cantidad_stock = cantidad_stock - ? WHERE nombre_producto = ? AND cantidad_stock >= ?";
                    try (PreparedStatement ps2 = con.prepareStatement(sql2)) {
                        ps2.setDouble(1, item.getCantidad());
                        ps2.setString(2, item.getDescripcion());
                        ps2.setDouble(3, item.getCantidad());
                        ps2.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                LoggerUtil.warning("No se pudo descontar stock de " + item.getDescripcion() + ": " + e.getMessage());
            }
        }
    }

    private void refrescarProductos() {
        if (productGrid == null) return;
        try (Connection con = ConnectionManager.getConnection()) {
            cargarProductos(con);
        } catch (SQLException e) {
            cargarProductosDesdeDB();
        }
    }

    @FXML
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
            AlertManager.showInfo("Cliente Registrado", "Cliente guardado correctamente.");
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
            AlertManager.showInfo("Cliente Registrado", "Cliente guardado correctamente.");
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
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Stage stage = (Stage) tablaVentas.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setResizable(true);
        } catch (Exception e) {
            LoggerUtil.error("Error al regresar al menu principal", e);
        }
    }

    public static class VentaHistorial {
        private int id;
        private String cliente, fecha, metodo, tipoEntrega;
        private double total;

        public VentaHistorial(int id, String cliente, String fecha, double total, String metodo, String tipoEntrega) {
            this.id = id;
            this.cliente = cliente;
            this.fecha = fecha;
            this.total = total;
            this.metodo = metodo;
            this.tipoEntrega = tipoEntrega;
        }

        public int getId() { return id; }
        public String getCliente() { return cliente; }
        public String getFecha() { return fecha; }
        public double getTotal() { return total; }
        public String getMetodo() { return metodo; }
        public String getTipoEntrega() { return tipoEntrega; }
    }
}
