package com.example.pantallas.ProcesoDeInventario;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.layout.StackPane;

public class InventarioController {

    public StackPane contentArea;
    @FXML private VBox viewStock, viewMovimientos, viewAjustes;
    @FXML private Button btnStock, btnMovimientos, btnAjustes;

    // Tablas
    @FXML private TableView<Producto> tablaStock;
    @FXML private TableColumn<Producto, String> colNombre, colCategoria, colUnidad;
    @FXML private TableColumn<Producto, Double> colCantidad;

    @FXML private TableView<Movimiento> tablaKardex;
    @FXML private TableColumn<Movimiento, String> colKFecha, colKProducto, colKTipo, colKUnidad;
    @FXML private TableColumn<Movimiento, Double> colKCant;

    // Filtros
    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<String> cbCategoria;

    // Campos de Ajuste
    @FXML private ComboBox<Producto> cbItemAjuste;
    @FXML private ComboBox<String> cbUnidadMedida;
    @FXML private TextField txtCantidadAjuste;
    @FXML private RadioButton rbEntrada;
    @FXML private ToggleGroup tgAjuste;

    // Listas y Filtros
    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private FilteredList<Producto> listaFiltrada;
    private ObservableList<Movimiento> listaMovimientos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTablas();
        cargarBaseDeDatosEjemplo();

        // 1. Configurar la lista filtrada envolviendo la lista original
        listaFiltrada = new FilteredList<>(listaProductos, p -> true);
        tablaStock.setItems(listaFiltrada);

        // 2. Listener para el ComboBox de Categorías
        cbCategoria.setOnAction(e -> aplicarFiltros());

        // 3. Listener para la barra de búsqueda
        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        cbItemAjuste.setItems(listaProductos);

        // Cargar datos en formulario al seleccionar en tabla
        tablaStock.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cbItemAjuste.setValue(newSelection);
                txtCantidadAjuste.setText(String.valueOf(newSelection.getStock()));
                cbUnidadMedida.setValue(newSelection.getUnidad());
            }
        });
    }

    private void aplicarFiltros() {
        String categoriaSeleccionada = cbCategoria.getValue();
        String textoBusqueda = txtBusqueda.getText().toLowerCase();

        listaFiltrada.setPredicate(producto -> {
            boolean coincideCategoria = (categoriaSeleccionada == null ||
                    categoriaSeleccionada.equals("Todas las Categorías") ||
                    producto.getCategoria().equals(categoriaSeleccionada));

            boolean coincideTexto = producto.getNombre().toLowerCase().contains(textoBusqueda);

            return coincideCategoria && coincideTexto;
        });
    }

    private void configurarTablas() {
        // Vinculación con Properties
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colCategoria.setCellValueFactory(cellData -> cellData.getValue().categoriaProperty());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());
        colUnidad.setCellValueFactory(cellData -> cellData.getValue().unidadProperty());

        colKFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colKProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colKTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colKCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colKUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        tablaKardex.setItems(listaMovimientos);
    }

    private void cargarBaseDeDatosEjemplo() {
        listaProductos.addAll(
                new Producto("Queso Crema Lote A1", "Quesos Frescos", 120.5, "Libras (Lbs)"),
                new Producto("Queso Mozzarella Lote B2", "Quesos Madurados", 85.0, "Kilos (Kg)"),
                new Producto("Leche Cruda", "Materia Prima (Leche/Cuajo)", 500.0, "Litros (L)"),
                new Producto("Detergente Industrial", "Insumos de Limpieza", 10.0, "Litros (L)"),
                new Producto("Bolsas Termoencogibles", "Empaques y Etiquetas", 1000.0, "Unidades (Und)")
        );

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        listaMovimientos.add(new Movimiento(LocalDateTime.now().format(dtf), "Leche Cruda", "ENTRADA", 500.0, "Litros (L)"));
    }

    @FXML
    private void procesarAjuste() {
        Producto seleccionado = cbItemAjuste.getValue();
        if (seleccionado != null && !txtCantidadAjuste.getText().isEmpty()) {
            double cant = Double.parseDouble(txtCantidadAjuste.getText());
            String tipo = rbEntrada.isSelected() ? "ENTRADA" : "SALIDA";

            if (tipo.equals("ENTRADA")) {
                seleccionado.setStock(seleccionado.getStock() + cant);
            } else {
                seleccionado.setStock(seleccionado.getStock() - cant);
            }

            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            listaMovimientos.add(0, new Movimiento(fecha, seleccionado.getNombre(), tipo, cant, seleccionado.getUnidad()));

            tablaStock.refresh();
            mostrarAlerta("Éxito", "Movimiento registrado.");
        }
    }

    @FXML
    private void actualizarAjuste() {
        Producto seleccionado = cbItemAjuste.getValue();
        if (seleccionado != null) {
            seleccionado.setStock(Double.parseDouble(txtCantidadAjuste.getText()));
            seleccionado.setUnidad(cbUnidadMedida.getValue());
            tablaStock.refresh();
            mostrarAlerta("Actualizado", "Datos base modificados.");
        }
    }

    @FXML
    private void borrarAjuste() {
        Producto seleccionado = cbItemAjuste.getValue();
        if (seleccionado != null) {
            listaProductos.remove(seleccionado);
            limpiarFormulario();
        }
    }

    @FXML
    private void borrarMovimientoSeleccionado() {
        Movimiento sel = tablaKardex.getSelectionModel().getSelectedItem();
        if (sel != null) listaMovimientos.remove(sel);
    }

    @FXML
    private void limpiarFormulario() {
        txtCantidadAjuste.clear();
        cbItemAjuste.getSelectionModel().clearSelection();
    }

    @FXML private void mostrarStock() { alternar(viewStock); }
    @FXML private void mostrarMovimientos() { alternar(viewMovimientos); }
    @FXML private void mostrarAjustes() { alternar(viewAjustes); }

    private void alternar(VBox vista) {
        viewStock.setVisible(false); viewStock.setManaged(false);
        viewMovimientos.setVisible(false); viewMovimientos.setManaged(false);
        viewAjustes.setVisible(false); viewAjustes.setManaged(false);
        vista.setVisible(true); vista.setManaged(true);
    }

    @FXML
    private void volverAlMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo cargar el Menú Principal.");
        }
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setContentText(m); a.show();
    }

    // MODELO PRODUCTO ACTUALIZADO CON PROPERTIES
    public static class Producto {
        private final StringProperty nombre;
        private final StringProperty categoria;
        private final DoubleProperty stock;
        private final StringProperty unidad;

        public Producto(String n, String c, double s, String u) {
            this.nombre = new SimpleStringProperty(n);
            this.categoria = new SimpleStringProperty(c);
            this.stock = new SimpleDoubleProperty(s);
            this.unidad = new SimpleStringProperty(u);
        }

        public String getNombre() { return nombre.get(); }
        public StringProperty nombreProperty() { return nombre; }

        public String getCategoria() { return categoria.get(); }
        public StringProperty categoriaProperty() { return categoria; }

        public double getStock() { return stock.get(); }
        public void setStock(double s) { this.stock.set(s); }
        public DoubleProperty stockProperty() { return stock; }

        public String getUnidad() { return unidad.get(); }
        public void setUnidad(String u) { this.unidad.set(u); }
        public StringProperty unidadProperty() { return unidad; }

        @Override public String toString() { return getNombre(); }
    }

    public static class Movimiento {
        private String fecha, producto, tipo, unidad;
        private double cantidad;
        public Movimiento(String f, String p, String t, double c, String u) {
            this.fecha = f; this.producto = p; this.tipo = t; this.cantidad = c; this.unidad = u;
        }
        public String getFecha() { return fecha; }
        public String getProducto() { return producto; }
        public String getTipo() { return tipo; }
        public double getCantidad() { return cantidad; }
        public String getUnidad() { return unidad; }
    }
}