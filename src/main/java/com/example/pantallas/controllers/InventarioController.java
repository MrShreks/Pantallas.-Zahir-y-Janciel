package com.example.pantallas.controllers;

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

import com.example.pantallas.services.ServicioInventario;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InventarioController {

    @FXML public StackPane contentArea;
    @FXML private VBox viewStock, viewMovimientos, viewAjustes, viewReportes;
    @FXML private Button btnStock, btnMovimientos, btnAjustes, btnReportes;
    @FXML private TextArea txtJustificacion;

    @FXML private TableView<Producto> tablaStock;
    @FXML private TableColumn<Producto, String> colNombre, colCategoria, colUnidad;
    @FXML private TableColumn<Producto, Double> colCantidad, colCantMinima;

    @FXML private TableView<Movimiento> tablaKardex;
    @FXML private TableColumn<Movimiento, String> colKFecha, colKProducto, colKTipo, colKUnidad, colKJustificacion;
    @FXML private TableColumn<Movimiento, Double> colKCant;

    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<String> cbFiltroKardex;

    @FXML private ComboBox<Producto> cbItemAjuste;
    @FXML private TextField txtCantidadAjuste;
    @FXML private ComboBox<String> cbUnidadMedida;

    @FXML private TableView<Salida> tablaSalidas;
    @FXML private TableColumn<Salida, String> colSProducto, colSUnidad, colSJustificacion, colSFecha;
    @FXML private TableColumn<Salida, Double> colSCantidad;

    // Reportes dashboard
    @FXML private Label lblTotalEntradas, lblTotalSalidas, lblStockTotalActual, lblValorInventarioRep, lblStockBajoRep;
    @FXML private Label lblFechaRep, lblFuenteDatosRep, lblUltimaActualizacionRep;
    @FXML private Button btnRefrescarReportes;
    @FXML private PieChart pieDistribucionRep;
    @FXML private BarChart<String, Number> barFlujoInventario;
    @FXML private TableView<Movimiento> tablaMovimientosRep;
    @FXML private TableColumn<Movimiento, String> colRepFecha, colRepProducto, colRepTipo, colRepUnidad, colRepJustificacion;
    @FXML private TableColumn<Movimiento, Double> colRepCant;

    public StackPane rootPane;

    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private ObservableList<Movimiento> listaMovimientos = FXCollections.observableArrayList();
    private ObservableList<Movimiento> listaMovimientosFiltrados = FXCollections.observableArrayList();
    private ObservableList<Salida> listaSalidas = FXCollections.observableArrayList();

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
        configurarTablasReportes();
        cargarDatosDesdeDB();
        configurarUnidadesCombo();
        configurarEventos();
    }

    private void configurarUnidadesCombo() {
        ObservableList<String> unidades = FXCollections.observableArrayList();
        for (String u : UNIDADES_POR_DEFECTO.values()) {
            if (!unidades.contains(u)) unidades.add(u);
        }
        unidades.add("Unidades (Und)");
        unidades.add("Paquetes");
        unidades.add("Cajas");
        cbUnidadMedida.setItems(unidades);
    }

    private void filtrarKardex() {
        String filtro = cbFiltroKardex != null ? cbFiltroKardex.getValue() : "Todos";
        listaMovimientosFiltrados.clear();
        for (Movimiento m : listaMovimientos) {
            if ("Todos".equals(filtro)) {
                listaMovimientosFiltrados.add(m);
            } else if ("Entradas".equals(filtro) && "ENTRADA".equals(m.getTipo())) {
                listaMovimientosFiltrados.add(m);
            } else if ("Salidas".equals(filtro) && "SALIDA".equals(m.getTipo())) {
                listaMovimientosFiltrados.add(m);
            }
        }
        tablaKardex.setItems(listaMovimientosFiltrados);
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
        colKJustificacion.setCellValueFactory(cellData -> {
            String t = cellData.getValue().getTipo();
            String j = cellData.getValue().getJustificacion();
            if ("ENTRADA".equals(t)) return new SimpleStringProperty("—");
            return new SimpleStringProperty(j != null ? j : "");
        });
        tablaKardex.setItems(listaMovimientosFiltrados);

        colSProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colSCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colSUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        colSJustificacion.setCellValueFactory(new PropertyValueFactory<>("justificacion"));
        colSFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        tablaSalidas.setItems(listaSalidas);
    }

    private void configurarEventos() {
        cbCategoria.setOnAction(e -> filtrarProductos());
        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> filtrarProductos());

        cbFiltroKardex.setItems(FXCollections.observableArrayList("Todos", "Entradas", "Salidas"));
        cbFiltroKardex.setValue("Todos");
        cbFiltroKardex.setOnAction(e -> filtrarKardex());

        cbItemAjuste.setOnAction(e -> actualizarUnidadProducto());

        tablaStock.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cbItemAjuste.setValue(newSelection);
                txtCantidadAjuste.setText(String.valueOf(newSelection.getStock()));
                cbUnidadMedida.setValue(newSelection.getUnidad());
            }
        });
    }

    private void cargarDatosDesdeDB() {
        listaProductos.clear();

        String[][] consultas = {
            {"tbl_inventario_productos", "nombre_producto", "categoria", "cantidad_stock", "cantidad_minima", "unidad_medida"},
            {"Productos", "nombre_producto", "categoria", "stock_actual", "stock_minimo", "unidad_medida"},
            {"tbl_productos", "nombre", "categoria", "cantidad_stock", "stock_minimo", "unidad"},
            {"tbl_inventario", "nombre_producto", "categoria", "cantidad", "minimo", "unidad_medida"}
        };

        java.util.Set<String> nombresCargados = new java.util.HashSet<>();
        boolean algunoCargado = false;

        for (String[] c : consultas) {
            String table = c[0];
            if (!tablaExiste(table)) continue;
            String colName = c[1];
            String colCat = c[2];
            String colStock = c[3];
            String colMin = c[4];
            String colUnidad = c[5];

            StringBuilder cols = new StringBuilder();
            cols.append(colName);
            if (columnaExiste(table, colCat)) cols.append(", ").append(colCat);
            if (columnaExiste(table, colUnidad)) cols.append(", ").append(colUnidad);
            if (columnaExiste(table, colStock)) cols.append(", ").append(colStock);
            if (columnaExiste(table, colMin)) cols.append(", ").append(colMin);

            String sql = "SELECT " + cols + " FROM " + table + " ORDER BY " + colName;

            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                boolean tieneCat = columnaExiste(table, colCat);
                boolean tieneUnd = columnaExiste(table, colUnidad);
                boolean tieneStock = columnaExiste(table, colStock);
                boolean tieneMin = columnaExiste(table, colMin);

                while (rs.next()) {
                    String nombre = rs.getString(colName);
                    if (nombresCargados.contains(nombre)) continue;
                    nombresCargados.add(nombre);

                    String categoria = tieneCat ? rs.getString(colCat) : null;
                    if (categoria == null || categoria.isEmpty()) categoria = asignarCategoria(nombre);
                    double stock = tieneStock ? rs.getDouble(colStock) : 0;
                    double min = tieneMin ? rs.getDouble(colMin) : 0;
                    String unidad = tieneUnd ? rs.getString(colUnidad) : null;
                    if (unidad == null || unidad.isEmpty()) unidad = obtenerUnidadInteligente(nombre);

                    listaProductos.add(new Producto(nombre, categoria, stock, min, unidad));
                    algunoCargado = true;
                }
            } catch (Exception ignored) { }
        }

        if (algunoCargado) {
            tablaStock.setItems(listaProductos);
            cbItemAjuste.setItems(listaProductos);
            poblarCategorias();
            cargarKardex();
        } else {
            com.example.pantallas.utils.LoggerUtil.warning("No se pudo cargar inventario desde BD, usando datos demo");
            cargarDatosDemo();
        }
    }

    private boolean tablaExiste(String nombre) {
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT OBJECT_ID('" + nombre + "') AS oid")) {
            return rs.next() && rs.getObject("oid") != null;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean columnaExiste(String tabla, String columna) {
        if (columna == null || columna.isEmpty()) return false;
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COL_LENGTH('" + tabla + "','" + columna + "') AS cl")) {
            return rs.next() && rs.getObject("cl") != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String asignarCategoria(String nombre) {
        String n = nombre.toLowerCase();
        if (n.contains("queso")) return "Quesos";
        if (n.contains("leche") || n.contains("cuajo")) return "Lácteos";
        if (n.contains("sal") || n.contains("cloro") || n.contains("detergente") || n.contains("colorante")) return "Insumos";
        if (n.contains("funda") || n.contains("bolsa")) return "Empaques";
        return "Otros";
    }

    private void poblarCategorias() {
        ObservableList<String> cats = FXCollections.observableArrayList();
        cats.add("Todas");
        for (Producto p : listaProductos) {
            String c = p.getCategoria();
            if (c != null && !c.isEmpty() && !cats.contains(c)) cats.add(c);
        }
        cbCategoria.setItems(cats);
        cbCategoria.setValue("Todas");
    }

    private void cargarDatosDemo() {
        listaProductos.addAll(
            new Producto("Queso Blanco", "Quesos", 100.0, 20.0, "Libras (Lbs)"),
            new Producto("Queso Amarillo", "Quesos", 75.5, 15.0, "Libras (Lbs)"),
            new Producto("Queso Mozzarella", "Quesos", 50.0, 10.0, "Kilos (Kg)"),
            new Producto("Queso Crema", "Quesos", 30.0, 8.0, "Libras (Lbs)"),
            new Producto("Leche Cruda", "Lácteos", 500.0, 100.0, "Litros (L)"),
            new Producto("Cuajo Líquido", "Insumos", 30.0, 10.0, "Litros (L)"),
            new Producto("Sal Industrial", "Insumos", 200.0, 50.0, "Kilos (Kg)"),
            new Producto("Detergente Industrial", "Insumos", 10.0, 5.0, "Botella/10oz"),
            new Producto("Fundas para Queso", "Empaques", 1000.0, 200.0, "Unidades (Und)"),
            new Producto("Bolsas Termoencogibles", "Empaques", 1000.0, 200.0, "Unidades (Und)")
        );
        tablaStock.setItems(listaProductos);
        cbItemAjuste.setItems(listaProductos);
        poblarCategorias();
        cargarKardex();
    }

    private void cargarKardex() {
        listaMovimientos.clear();
        String query = "SELECT producto, tipo, cantidad, unidad, fecha_movimiento, justificacion FROM tbl_movimientos_inventario ORDER BY fecha_movimiento DESC";
        
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            while (rs.next()) {
                String just = "";
                try { just = rs.getString("justificacion"); } catch (Exception e) { just = ""; }
                if (just == null) just = "";
                listaMovimientos.add(new Movimiento(
                    rs.getString("fecha_movimiento"),
                    rs.getString("producto"),
                    rs.getString("tipo"),
                    rs.getDouble("cantidad"),
                    rs.getString("unidad"),
                    just
                ));
            }
            filtrarKardex();
            
        } catch (Exception e) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            listaMovimientos.add(new Movimiento(LocalDateTime.now().format(dtf), "Leche Cruda", "ENTRADA", 500.0, "Litros (L)", ""));
            filtrarKardex();
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
            mostrarAlerta("Campo Vacío", "Debe ingresar la cantidad a retirar.");
            return;
        }

        double cantidad = parsearNumero(cantidadStr);
        if (cantidad <= 0) {
            mostrarAlerta("Cantidad Inválida", "La cantidad debe ser mayor a 0.");
            return;
        }

        String justificacion = txtJustificacion.getText();
        if (justificacion == null || justificacion.trim().isEmpty()) {
            mostrarAlerta("Justificación Requerida", "Debe ingresar el motivo de la baja/merma (caducidad, daño, pérdida, etc.).");
            return;
        }

        String unidad = cbUnidadMedida.getValue();
        if (unidad == null || unidad.isEmpty()) {
            mostrarAlerta("Unidad Requerida", "Debe seleccionar la unidad de medida.");
            return;
        }

        if (cantidad > seleccionado.getStock()) {
            mostrarAlerta("Stock Insuficiente", "No hay suficiente inventario. Stock actual: " + seleccionado.getStock() + " " + seleccionado.getUnidad());
            return;
        }

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String tipo = "SALIDA";
        seleccionado.setStock(seleccionado.getStock() - cantidad);

        listaMovimientos.add(0, new Movimiento(fecha, seleccionado.getNombre(), tipo, cantidad, unidad, justificacion));
        filtrarKardex();
        guardarMovimientoDB(seleccionado.getNombre(), tipo, cantidad, unidad, justificacion);
        guardarActualizacionStockDB(seleccionado.getNombre(), cantidad);
        guardarSalidaDB(seleccionado.getNombre(), cantidad, unidad, justificacion);

        listaSalidas.add(0, new Salida(seleccionado.getNombre(), cantidad, unidad, justificacion, fecha));

        tablaStock.refresh();
        tablaSalidas.refresh();
        txtCantidadAjuste.clear();
        txtJustificacion.clear();
        mostrarAlerta("Baja Registrada", "Se ha registrado la salida de " + cantidad + " " + seleccionado.getUnidad() + " de " + seleccionado.getNombre() + ".");
    }

    private void guardarMovimientoDB(String producto, String tipo, double cantidad, String unidad, String justificacion) {
        String sql = "INSERT INTO tbl_movimientos_inventario (producto, tipo, cantidad, unidad, fecha_movimiento, justificacion) VALUES (?, ?, ?, ?, GETDATE(), ?)";
        
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto);
            ps.setString(2, tipo);
            ps.setDouble(3, cantidad);
            ps.setString(4, unidad);
            ps.setString(5, justificacion);
            ps.executeUpdate();
        } catch (Exception e) {
            String fallbackSql = "INSERT INTO tbl_movimientos_inventario (producto, tipo, cantidad, unidad, fecha_movimiento) VALUES (?, ?, ?, ?, GETDATE())";
            try (Connection con2 = com.example.pantallas.config.ConnectionManager.getConnection();
                 PreparedStatement ps2 = con2.prepareStatement(fallbackSql)) {
                ps2.setString(1, producto);
                ps2.setString(2, tipo);
                ps2.setDouble(3, cantidad);
                ps2.setString(4, unidad);
                ps2.executeUpdate();
                try (Statement st = con2.createStatement()) {
                    st.execute("ALTER TABLE tbl_movimientos_inventario ADD justificacion VARCHAR(500)");
                } catch (Exception ex) { }
            } catch (Exception ex2) {
                // Silent fail for demo mode
            }
        }
    }

    private void guardarSalidaDB(String producto, double cantidad, String unidad, String justificacion) {
        String sql = "INSERT INTO tbl_salidas_inventario (producto, cantidad, unidad, justificacion, fecha_salida) VALUES (?, ?, ?, ?, GETDATE())";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto);
            ps.setDouble(2, cantidad);
            ps.setString(3, unidad);
            ps.setString(4, justificacion);
            ps.executeUpdate();
        } catch (Exception e) {
            // Silent fail for demo mode
        }
    }

    private void cargarSalidas() {
        listaSalidas.clear();
        String query = "SELECT producto, cantidad, unidad, justificacion, fecha_salida FROM tbl_salidas_inventario ORDER BY fecha_salida DESC";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                String just = rs.getString("justificacion");
                if (just == null) just = "";
                listaSalidas.add(new Salida(
                    rs.getString("producto"),
                    rs.getDouble("cantidad"),
                    rs.getString("unidad"),
                    just,
                    rs.getString("fecha_salida")
                ));
            }
        } catch (Exception e) {
            // Silent fail — will rely on in-memory list
        }
        tablaSalidas.setItems(listaSalidas);
    }

    private void actualizarUnidadProducto() {
        Producto sel = cbItemAjuste.getValue();
        if (sel != null) {
            cbUnidadMedida.setValue(sel.getUnidad());
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
        txtJustificacion.clear();
        cbUnidadMedida.setValue(null);
        cbItemAjuste.getSelectionModel().clearSelection();
    }

    @FXML private void mostrarStock() { alternarVistas(viewStock, btnStock); }
    @FXML private void mostrarMovimientos() { alternarVistas(viewMovimientos, btnMovimientos); }
    @FXML private void mostrarAjustes() {
        alternarVistas(viewAjustes, btnAjustes);
        cargarSalidas();
    }
    @FXML private void mostrarReportes() {
        alternarVistas(viewReportes, btnReportes);
        cargarReportes();
    }

    @FXML
    private void refrescarReportes() {
        cargarReportes();
        if (lblUltimaActualizacionRep != null)
            lblUltimaActualizacionRep.setText("Última actualización: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }

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
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setResizable(true);
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.error("Excepción detectada", e);
            mostrarAlerta("Error de Navegación", "No se pudo cargar el Menú Principal.");
        }
    }

    private void guardarActualizacionStockDB(String nombreProducto, double cantidadRetirar) {
        String[][] updates = {
            {"tbl_inventario_productos", "cantidad_stock", "nombre_producto"},
            {"Productos", "stock_actual", "nombre_producto"},
            {"tbl_productos", "cantidad_stock", "nombre"},
            {"tbl_inventario", "cantidad", "nombre_producto"}
        };
        for (String[] u : updates) {
            String sql = "UPDATE " + u[0] + " SET " + u[1] + " = " + u[1] + " - ? WHERE " + u[2] + " = ?";
            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, cantidadRetirar);
                ps.setString(2, nombreProducto);
                int rows = ps.executeUpdate();
                if (rows > 0) break;
            } catch (Exception ignored) { }
        }
    }

    // ========================================================================
    // REPORTES DASHBOARD
    // ========================================================================

    private void configurarTablasReportes() {
        if (tablaMovimientosRep == null) return;
        colRepFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colRepProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colRepTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colRepCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colRepUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        colRepJustificacion.setCellValueFactory(cellData -> {
            String t = cellData.getValue().getTipo();
            String j = cellData.getValue().getJustificacion();
            if ("ENTRADA".equals(t)) return new SimpleStringProperty("—");
            return new SimpleStringProperty(j != null ? j : "");
        });
    }

    private void cargarReportes() {
        cargarKPIReportes();
        cargarPieDistribucionReportes();
        cargarBarFlujoReportes();
        cargarTablaMovimientosReportes();
    }

    private void cargarKPIReportes() {
        boolean usandoDemo = !ServicioInventario.hayDatosReales();
        if (lblFuenteDatosRep != null) {
            if (usandoDemo) {
                lblFuenteDatosRep.setText("MODO DEMO — Datos de muestra");
                lblFuenteDatosRep.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: 700; -fx-font-size: 11;");
            } else {
                lblFuenteDatosRep.setText("Datos en vivo — Base de datos conectada");
                lblFuenteDatosRep.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: 700; -fx-font-size: 11;");
            }
        }
        if (lblFechaRep != null)
            lblFechaRep.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        double entradas, salidas, stockTotal, valorInv;
        int stockBajo;

        if (usandoDemo) {
            Map<String, Number> demoKPI = ServicioInventario.generarDemoKPIs();
            entradas = 16200;
            salidas = 10500;
            stockTotal = 1945;
            valorInv = demoKPI.get("valor_inventario").doubleValue();
            stockBajo = demoKPI.get("stock_bajo").intValue();
        } else {
            entradas = ServicioInventario.obtenerTotalEntradasMes();
            salidas = ServicioInventario.obtenerTotalSalidasMes();
            stockTotal = ServicioInventario.obtenerStockTotal();
            valorInv = ServicioInventario.obtenerValorInventario();
            stockBajo = ServicioInventario.obtenerStockBajo();
        }

        if (lblTotalEntradas != null) lblTotalEntradas.setText(String.format("%.2f", entradas));
        if (lblTotalSalidas != null) lblTotalSalidas.setText(String.format("%.2f", salidas));
        if (lblStockTotalActual != null) lblStockTotalActual.setText(String.format("%.2f und", stockTotal));
        if (lblValorInventarioRep != null) lblValorInventarioRep.setText(String.format("RD$ %,.2f", valorInv));
        if (lblStockBajoRep != null) lblStockBajoRep.setText(stockBajo + " productos");
    }

    private void cargarPieDistribucionReportes() {
        if (pieDistribucionRep == null) return;
        try {
            boolean usandoDemo = !ServicioInventario.hayDatosReales();
            Map<String, Number> dist = usandoDemo
                ? ServicioInventario.generarDemoDistribucion()
                : ServicioInventario.obtenerDistribucionCategorias();
            if (dist.isEmpty()) dist = ServicioInventario.generarDemoDistribucion();

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<String, Number> e : dist.entrySet())
                pieData.add(new PieChart.Data(e.getKey(), e.getValue().doubleValue()));
            pieDistribucionRep.setData(pieData);
            pieDistribucionRep.setTitle("Distribución por Categoría");
            pieDistribucionRep.setLabelsVisible(true);
            pieDistribucionRep.setLegendVisible(true);
            pieDistribucionRep.setAnimated(true);
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.warning("No se pudo cargar gráfico de distribución: " + e.getMessage());
        }
    }

    private void cargarBarFlujoReportes() {
        if (barFlujoInventario == null) return;
        try {
            boolean usandoDemo = !ServicioInventario.hayDatosReales();
            Map<String, double[]> flujo = usandoDemo
                ? ServicioInventario.generarDemoFlujoMensual()
                : ServicioInventario.obtenerFlujoMensual(6);
            if (flujo.isEmpty()) flujo = ServicioInventario.generarDemoFlujoMensual();

            XYChart.Series<String, Number> seriesEntradas = new XYChart.Series<>();
            seriesEntradas.setName("Entradas");
            XYChart.Series<String, Number> seriesSalidas = new XYChart.Series<>();
            seriesSalidas.setName("Salidas");

            for (Map.Entry<String, double[]> e : flujo.entrySet()) {
                seriesEntradas.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()[0]));
                seriesSalidas.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()[1]));
            }

            barFlujoInventario.getData().clear();
            barFlujoInventario.getData().addAll(seriesEntradas, seriesSalidas);
            barFlujoInventario.setTitle("Flujo de Entradas vs Salidas");
            barFlujoInventario.setAnimated(true);
            barFlujoInventario.setCategoryGap(10);
            barFlujoInventario.setBarGap(2);

            CategoryAxis xAxis = (CategoryAxis) barFlujoInventario.getXAxis();
            xAxis.setTickLabelRotation(45);
            xAxis.setTickLabelFont(javafx.scene.text.Font.font("Segoe UI", 11));
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.warning("No se pudo cargar gráfico de flujo: " + e.getMessage());
        }
    }

    private void cargarTablaMovimientosReportes() {
        if (tablaMovimientosRep == null) return;
        tablaMovimientosRep.setItems(listaMovimientos);
        tablaMovimientosRep.setRowFactory(tv -> new javafx.scene.control.TableRow<Movimiento>() {
            @Override
            protected void updateItem(Movimiento item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                String tipo = item.getTipo();
                if (tipo != null) {
                    if (tipo.contains("ENTRADA")) setStyle("-fx-text-fill: #2ecc71;");
                    else if (tipo.contains("SALIDA")) setStyle("-fx-text-fill: #e74c3c;");
                    else if (tipo.contains("AJUSTE")) setStyle("-fx-text-fill: #f39c12;");
                    else setStyle("");
                }
            }
        });
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
        private String fecha, producto, tipo, unidad, justificacion;
        private double cantidad;

        public Movimiento(String fecha, String producto, String tipo, double cantidad, String unidad, String justificacion) {
            this.fecha = fecha;
            this.producto = producto;
            this.tipo = tipo;
            this.cantidad = cantidad;
            this.unidad = unidad;
            this.justificacion = justificacion;
        }

        public String getFecha() { return fecha; }
        public String getProducto() { return producto; }
        public String getTipo() { return tipo; }
        public double getCantidad() { return cantidad; }
        public String getUnidad() { return unidad; }
        public String getJustificacion() { return justificacion; }
    }

    public static class Salida {
        private String producto, unidad, justificacion, fecha;
        private double cantidad;

        public Salida(String producto, double cantidad, String unidad, String justificacion, String fecha) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.unidad = unidad;
            this.justificacion = justificacion;
            this.fecha = fecha;
        }

        public String getProducto() { return producto; }
        public double getCantidad() { return cantidad; }
        public String getUnidad() { return unidad; }
        public String getJustificacion() { return justificacion; }
        public String getFecha() { return fecha; }
    }
}
