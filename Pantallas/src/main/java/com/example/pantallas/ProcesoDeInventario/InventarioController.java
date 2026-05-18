package com.example.pantallas.ProcesoDeInventario;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class InventarioController {

    @FXML public StackPane contentArea;
    @FXML private VBox viewStock, viewMovimientos, viewAjustes, viewReportes;
    @FXML private Button btnStock, btnMovimientos, btnAjustes, btnReportes;

    @FXML private TableView<Producto> tablaStock;
    @FXML private TableColumn<Producto, String> colNombre, colCategoria, colUnidad;
    @FXML private TableColumn<Producto, Double> colCantidad, colCantMinima;

    @FXML private TableView<Movimiento> tablaKardex;
    @FXML private TableColumn<Movimiento, String> colKFecha, colKProducto, colKTipo, colKUnidad;
    @FXML private TableColumn<Movimiento, Double> colKCant;

    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<String> cbCategoria;

    @FXML private ComboBox<Producto> cbItemAjuste;
    @FXML private TextField txtCantidadAjuste, txtCantidadMinima;
    @FXML private RadioButton rbEntrada, rbSalida;
    @FXML private ToggleGroup tgTipoMovimiento;
    @FXML private Label lblUnidadProducto;

    public StackPane rootPane;

    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private ObservableList<Movimiento> listaMovimientos = FXCollections.observableArrayList();

    private static final Map<String, String> UNIDADES_POR_DEFECTO = new HashMap<>();
    static {
        UNIDADES_POR_DEFECTO.put("Queso Crema", "Libras (Lbs)");
        UNIDADES_POR_DEFECTO.put("Queso Mozzarella", "Kilos (Kg)");
        UNIDADES_POR_DEFECTO.put("Queso cheddar", "Kilos (Kg)");
        UNIDADES_POR_DEFECTO.put("Leche Cruda", "Litros (L)");
        UNIDADES_POR_DEFECTO.put("Leche Pasteurizada", "Litros (L)");
        UNIDADES_POR_DEFECTO.put("Cuajo Líquido", "Litros (L)");
        UNIDADES_POR_DEFECTO.put("Sal Industrial", "Kilos (Kg)");
        UNIDADES_POR_DEFECTO.put("Fundas para Queso", "Unidades (Und)");
        UNIDADES_POR_DEFECTO.put("Cloro", "Botella/10oz");
        UNIDADES_POR_DEFECTO.put("Detergente", "Botella/10oz");
        UNIDADES_POR_DEFECTO.put("colorante", "Gramos (g)");
    }

    @FXML
    public void initialize() {
        configurarTablas();
        cargarDatosDesdeDB();
        configurarEventos();
    }

    private void configurarTablas() {
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colCategoria.setCellValueFactory(cellData -> cellData.getValue().categoriaProperty());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());
        colCantMinima.setCellValueFactory(cellData -> cellData.getValue().stockMinimoProperty().asObject());
        colUnidad.setCellValueFactory(cellData -> cellData.getValue().unidadProperty());

        colKFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colKProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colKTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colKCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colKUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        tablaKardex.setItems(listaMovimientos);
    }

    private void configurarEventos() {
        cbCategoria.setOnAction(e -> filtrarProductos());
        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> filtrarProductos());

        cbItemAjuste.setOnAction(e -> actualizarUnidadProducto());

        tablaStock.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cbItemAjuste.setValue(newSelection);
                txtCantidadAjuste.setText(String.valueOf(newSelection.getStock()));
                txtCantidadMinima.setText(String.valueOf(newSelection.getStockMinimo()));
                lblUnidadProducto.setText(newSelection.getUnidad());
            }
        });
    }

    private void cargarDatosDesdeDB() {
        listaProductos.clear();
        String query = "SELECT nombre_producto, categoria, cantidad_stock, cantidad_minima, unidad_medida FROM tbl_inventario_productos ORDER BY nombre_producto";
        
        try (Connection con = DriverManager.getConnection(getURL(), USER, PASS);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                String nombre = rs.getString("nombre_producto");
                String unidad = rs.getString("unidad_medida");
                
                if (unidad == null || unidad.isEmpty()) {
                    unidad = obtenerUnidadInteligente(nombre);
                }
                
                listaProductos.add(new Producto(
                    nombre,
                    rs.getString("categoria"),
                    rs.getDouble("cantidad_stock"),
                    rs.getDouble("cantidad_minima"),
                    unidad
                ));
            }
            tablaStock.setItems(listaProductos);
            
            cargarKardex();
            
        } catch (Exception e) {
            e.printStackTrace();
            cargarDatosDemo();
        }
    }

    private void cargarDatosDemo() {
        listaProductos.addAll(
            new Producto("Queso Crema Lote A1", "Quesos Frescos", 120.5, 20.0, "Libras (Lbs)"),
            new Producto("Queso Mozzarella Lote B2", "Quesos Madurados", 85.0, 15.0, "Kilos (Kg)"),
            new Producto("Leche Cruda", "Materia Prima (Leche/Cuajo)", 500.0, 100.0, "Litros (L)"),
            new Producto("Detergente Industrial", "Insumos de Limpieza", 10.0, 5.0, "Botella/10oz"),
            new Producto("Bolsas Termoencogibles", "Empaques y Etiquetas", 1000.0, 200.0, "Unidades (Und)")
        );
        tablaStock.setItems(listaProductos);
        cargarKardex();
    }

    private void cargarKardex() {
        listaMovimientos.clear();
        String query = "SELECT producto, tipo, cantidad, unidad, fecha_movimiento FROM tbl_movimientos_inventario ORDER BY fecha_movimiento DESC";
        
        try (Connection con = DriverManager.getConnection(getURL(), USER, PASS);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            while (rs.next()) {
                listaMovimientos.add(new Movimiento(
                    rs.getString("fecha_movimiento"),
                    rs.getString("producto"),
                    rs.getString("tipo"),
                    rs.getDouble("cantidad"),
                    rs.getString("unidad")
                ));
            }
            tablaKardex.setItems(listaMovimientos);
            
        } catch (Exception e) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            listaMovimientos.add(new Movimiento(LocalDateTime.now().format(dtf), "Leche Cruda", "ENTRADA", 500.0, "Litros (L)"));
            tablaKardex.setItems(listaMovimientos);
        }
    }

    private void filtrarProductos() {
        String categoria = cbCategoria.getValue();
        String busqueda = txtBusqueda.getText().toLowerCase();

        ObservableList<Producto> filtrados = listaProductos.filtered(p -> {
            boolean coincideCat = categoria == null || categoria.equals("Todas") || p.getCategoria().equals(categoria);
            boolean coincideBus = p.getNombre().toLowerCase().contains(busqueda);
            return coincideCat && coincideBus;
        });

        tablaStock.setItems(filtrados);
    }

    @FXML
    private void procesarMovimiento() {
        Producto seleccionado = cbItemAjuste.getValue();
        if (seleccionado == null) {
            mostrarAlerta("Selección Requerida", "Debe seleccionar un producto del inventario.");
            return;
        }

        String cantidadStr = txtCantidadAjuste.getText();
        if (cantidadStr == null || cantidadStr.isEmpty()) {
            mostrarAlerta("Campo Vacío", "Debe ingresar la cantidad.");
            return;
        }

        double cantidad = parsearNumero(cantidadStr);
        if (cantidad <= 0) {
            mostrarAlerta("Cantidad Inválida", "La cantidad debe ser mayor a 0.");
            return;
        }

        String tipo = rbEntrada.isSelected() ? "ENTRADA" : "SALIDA";

        if (tipo.equals("SALIDA") && cantidad > seleccionado.getStock()) {
            mostrarAlerta("Stock Insuficiente", "No hay suficiente inventario. Stock actual: " + seleccionado.getStock());
            return;
        }

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        if (tipo.equals("ENTRADA")) {
            seleccionado.setStock(seleccionado.getStock() + cantidad);
        } else {
            seleccionado.setStock(seleccionado.getStock() - cantidad);
        }

        listaMovimientos.add(0, new Movimiento(fecha, seleccionado.getNombre(), tipo, cantidad, seleccionado.getUnidad()));
        guardarMovimientoDB(seleccionado.getNombre(), tipo, cantidad, seleccionado.getUnidad());
        
        tablaStock.refresh();
        mostrarAlerta("Movimiento Registrado", "Se ha registrado: " + tipo + " de " + cantidad + " " + seleccionado.getUnidad());
    }

    private void guardarMovimientoDB(String producto, String tipo, double cantidad, String unidad) {
        String sql = "INSERT INTO tbl_movimientos_inventario (producto, tipo, cantidad, unidad, fecha_movimiento) VALUES (?, ?, ?, ?, GETDATE())";
        
        try (Connection con = DriverManager.getConnection(getURL(), USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto);
            ps.setString(2, tipo);
            ps.setDouble(3, cantidad);
            ps.setString(4, unidad);
            ps.executeUpdate();
        } catch (Exception e) {
            // Silent fail for demo mode
        }
    }

    private void actualizarUnidadProducto() {
        Producto sel = cbItemAjuste.getValue();
        if (sel != null) {
            lblUnidadProducto.setText(sel.getUnidad());
        }
    }

    @FXML
    private void actualizarProducto() {
        Producto seleccionado = cbItemAjuste.getValue();
        if (seleccionado == null) {
            mostrarAlerta("Selección Requerida", "Debe seleccionar un producto.");
            return;
        }

        String nuevaUnidad = lblUnidadProducto.getText();
        if (nuevaUnidad != null && !nuevaUnidad.isEmpty()) {
            seleccionado.setUnidad(nuevaUnidad);
        }

        String cantMinStr = txtCantidadMinima.getText();
        if (cantMinStr != null && !cantMinStr.isEmpty()) {
            seleccionado.setStockMinimo(parsearNumero(cantMinStr));
        }

        tablaStock.refresh();
        guardarActualizacionProducto(seleccionado);
        mostrarAlerta("Actualizado", "Producto actualizado correctamente.");
    }

    private void guardarActualizacionProducto(Producto producto) {
        String sql = "UPDATE tbl_inventario_productos SET cantidad_stock = ?, cantidad_minima = ?, unidad_medida = ? WHERE nombre_producto = ?";
        
        try (Connection con = DriverManager.getConnection(getURL(), USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, producto.getStock());
            ps.setDouble(2, producto.getStockMinimo());
            ps.setString(3, producto.getUnidad());
            ps.setString(4, producto.getNombre());
            ps.executeUpdate();
        } catch (Exception e) {
            // Silent fail
        }
    }

    @FXML
    private void borrarProducto() {
        Producto seleccionado = cbItemAjuste.getValue();
        if (seleccionado == null) {
            mostrarAlerta("Selección Requerida", "Debe seleccionar un producto.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro que desea eliminar este producto del inventario?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            listaProductos.remove(seleccionado);
            cbItemAjuste.getItems().remove(seleccionado);
            limpiarCampos();
            mostrarAlerta("Eliminado", "Producto eliminado.");
        }
    }

    @FXML
    private void borrarMovimiento() {
        Movimiento sel = tablaKardex.getSelectionModel().getSelectedItem();
        if (sel != null) {
            listaMovimientos.remove(sel);
        }
    }

    @FXML
    private void limpiarCampos() {
        txtCantidadAjuste.clear();
        txtCantidadMinima.clear();
        lblUnidadProducto.setText("");
        cbItemAjuste.getSelectionModel().clearSelection();
    }

    @FXML private void mostrarStock() { alternarVistas(viewStock, btnStock); }
    @FXML private void mostrarMovimientos() { alternarVistas(viewMovimientos, btnMovimientos); }
    @FXML private void mostrarAjustes() { alternarVistas(viewAjustes, btnAjustes); }
    @FXML private void mostrarReportes() { alternarVistas(viewReportes, btnReportes); }

    private void alternarVistas(VBox vista, Button btn) {
        viewStock.setVisible(false); viewStock.setManaged(false);
        viewMovimientos.setVisible(false); viewMovimientos.setManaged(false);
        viewAjustes.setVisible(false); viewAjustes.setManaged(false);
        viewReportes.setVisible(false); viewReportes.setManaged(false);
        
        if (vista != null) {
            vista.setVisible(true);
            vista.setManaged(true);
        }
        
        if (btn != null) {
            Button[] btns = {btnStock, btnMovimientos, btnAjustes, btnReportes};
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
    private void volverAlMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.setMaximized(wasMaximized);
            if (!wasMaximized) stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo cargar el Menú Principal.");
        }
    }

    private String obtenerUnidadInteligente(String nombreProducto) {
        String nombreLower = nombreProducto.toLowerCase();
        for (Map.Entry<String, String> entry : UNIDADES_POR_DEFECTO.entrySet()) {
            if (nombreLower.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        return "Unidades (Und)";
    }

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

    private String getURL() {
        return "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;trustServerCertificate=true;encrypt=false;";
    }

    private static final String USER = "sa";
    private static final String PASS = "123456";

    public static class Producto {
        private StringProperty nombre;
        private StringProperty categoria;
        private DoubleProperty stock;
        private DoubleProperty stockMinimo;
        private StringProperty unidad;

        public Producto(String nombre, String categoria, double stock, double stockMinimo, String unidad) {
            this.nombre = new SimpleStringProperty(nombre);
            this.categoria = new SimpleStringProperty(categoria);
            this.stock = new SimpleDoubleProperty(stock);
            this.stockMinimo = new SimpleDoubleProperty(stockMinimo);
            this.unidad = new SimpleStringProperty(unidad);
        }

        public String getNombre() { return nombre.get(); }
        public StringProperty nombreProperty() { return nombre; }
        
        public String getCategoria() { return categoria.get(); }
        public StringProperty categoriaProperty() { return categoria; }
        
        public double getStock() { return stock.get(); }
        public void setStock(double s) { stock.set(s); }
        public DoubleProperty stockProperty() { return stock; }
        
        public double getStockMinimo() { return stockMinimo.get(); }
        public void setStockMinimo(double s) { stockMinimo.set(s); }
        public DoubleProperty stockMinimoProperty() { return stockMinimo; }
        
        public String getUnidad() { return unidad.get(); }
        public void setUnidad(String u) { unidad.set(u); }
        public StringProperty unidadProperty() { return unidad; }

        @Override public String toString() { return getNombre(); }
    }

    public static class Movimiento {
        private String fecha, producto, tipo, unidad;
        private double cantidad;

        public Movimiento(String fecha, String producto, String tipo, double cantidad, String unidad) {
            this.fecha = fecha;
            this.producto = producto;
            this.tipo = tipo;
            this.cantidad = cantidad;
            this.unidad = unidad;
        }

        public String getFecha() { return fecha; }
        public String getProducto() { return producto; }
        public String getTipo() { return tipo; }
        public double getCantidad() { return cantidad; }
        public String getUnidad() { return unidad; }
    }
}