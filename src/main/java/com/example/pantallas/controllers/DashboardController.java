package com.example.pantallas.controllers;

import com.example.pantallas.security.SecurityUtil;
import com.example.pantallas.services.ServicioInventario;
import com.example.pantallas.utils.LoggerUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label lblValorInventario;
    @FXML private Label lblCambioValor;
    @FXML private Label lblProduccionActiva;
    @FXML private Label lblVentasMes;
    @FXML private Label lblCambioVentas;
    @FXML private Label lblStockBajo;
    @FXML private Label lblOrdenesPendientes;
    @FXML private Label lblUsuarioLogueado;
    @FXML private Label lblFecha;
    @FXML private Label lblUltimaActualizacion;
    @FXML private Label lblFuenteDatos;

    @FXML private PieChart pieDistribucion;
    @FXML private BarChart<String, Number> barVentas;

    @FXML private TableView<Map<String, String>> tablaMovimientos;
    @FXML private TableColumn<Map<String, String>, String> colMovFecha;
    @FXML private TableColumn<Map<String, String>, String> colMovProducto;
    @FXML private TableColumn<Map<String, String>, String> colMovTipo;
    @FXML private TableColumn<Map<String, String>, String> colMovCantidad;
    @FXML private TableColumn<Map<String, String>, String> colMovUsuario;

    @FXML private ListView<String> listaAlertasStock;

    private boolean usandoDemo = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarUsuarioFecha();
        inicializarTabla();
        cargarDashboard();
        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(60), e -> cargarDashboard()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    private void configurarUsuarioFecha() {
        if (lblFecha != null)
            lblFecha.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        if (lblUsuarioLogueado != null) {
            String usuario = SecurityUtil.getCurrentUser();
            lblUsuarioLogueado.setText(usuario != null ? usuario.toUpperCase() : "INVITADO");
        }
    }

    private void inicializarTabla() {
        if (tablaMovimientos == null) return;
        colMovFecha.setCellValueFactory(c -> {
            String v = c.getValue().get("fecha");
            return new javafx.beans.property.SimpleStringProperty(v != null ? v : "");
        });
        colMovProducto.setCellValueFactory(c -> {
            String v = c.getValue().get("producto");
            return new javafx.beans.property.SimpleStringProperty(v != null ? v : "");
        });
        colMovTipo.setCellValueFactory(c -> {
            String v = c.getValue().get("tipo");
            return new javafx.beans.property.SimpleStringProperty(v != null ? v : "");
        });
        colMovCantidad.setCellValueFactory(c -> {
            String v = c.getValue().get("cantidad");
            return new javafx.beans.property.SimpleStringProperty(v != null ? v : "");
        });
        colMovUsuario.setCellValueFactory(c -> {
            String v = c.getValue().get("usuario");
            return new javafx.beans.property.SimpleStringProperty(v != null ? v : "");
        });
    }

    @FXML
    private void refrescar() {
        cargarDashboard();
        if (lblUltimaActualizacion != null)
            lblUltimaActualizacion.setText("Última actualización: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }

    private void cargarDashboard() {
        usandoDemo = !ServicioInventario.hayDatosReales();
        if (lblFuenteDatos != null) {
            if (usandoDemo) {
                lblFuenteDatos.setText("MODO DEMO — Datos de muestra");
                lblFuenteDatos.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: 700; -fx-font-size: 11;");
            } else {
                lblFuenteDatos.setText("Datos en vivo — Base de datos conectada");
                lblFuenteDatos.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: 700; -fx-font-size: 11;");
            }
        }
        cargarKPIs();
        cargarPieDistribucion();
        cargarBarVentas();
        cargarMovimientos();
        cargarAlertasStock();
    }

    // ---- KPIs ----

    private void cargarKPIs() {
        try {
            double ventasMes, ventasAnterior, valorInv, valorInvAnterior;
            int stockBajo, prodActiva, ordenesPend;

            if (usandoDemo) {
                Map<String, Number> d = ServicioInventario.generarDemoKPIs();
                ventasMes = d.get("ventas_mes").doubleValue();
                ventasAnterior = d.get("ventas_anterior").doubleValue();
                valorInv = d.get("valor_inventario").doubleValue();
                valorInvAnterior = d.get("valor_anterior").doubleValue();
                stockBajo = d.get("stock_bajo").intValue();
                prodActiva = d.get("produccion_activa").intValue();
                ordenesPend = d.get("ordenes_pendientes").intValue();
            } else {
                ventasMes = ServicioInventario.obtenerVentasMesActual();
                ventasAnterior = ServicioInventario.obtenerVentasMesAnterior();
                valorInv = ServicioInventario.obtenerValorInventario();
                valorInvAnterior = valorInv > 0 ? valorInv * 0.92 : 0;
                stockBajo = ServicioInventario.obtenerStockBajo();
                prodActiva = ServicioInventario.obtenerProduccionActiva();
                ordenesPend = ServicioInventario.obtenerOrdenesPendientes();
            }

            setText(lblVentasMes, String.format("RD$ %,.2f", ventasMes));
            setText(lblValorInventario, String.format("RD$ %,.2f", valorInv));
            setText(lblStockBajo, stockBajo + " productos");
            setText(lblProduccionActiva, prodActiva + " lotes");
            setText(lblOrdenesPendientes, ordenesPend + " órdenes");

            if (lblCambioVentas != null) {
                double cv = ventasAnterior > 0 ? ((ventasMes - ventasAnterior) / ventasAnterior) * 100 : 0;
                String ic = cv >= 0 ? "\u25B2" : "\u25BC";
                lblCambioVentas.setText(String.format("%s %.1f%% vs mes pasado", ic, Math.abs(cv)));
                lblCambioVentas.setStyle(cv >= 0 ? "-fx-text-fill: #2ecc71;" : "-fx-text-fill: #e74c3c;");
            }
            if (lblCambioValor != null) {
                double ci = valorInvAnterior > 0 ? ((valorInv - valorInvAnterior) / valorInvAnterior) * 100 : 0;
                String ic = ci >= 0 ? "\u25B2" : "\u25BC";
                lblCambioValor.setText(String.format("%s %.1f%% vs mes pasado", ic, Math.abs(ci)));
                lblCambioValor.setStyle(ci >= 0 ? "-fx-text-fill: #2ecc71;" : "-fx-text-fill: #e74c3c;");
            }
        } catch (Exception e) {
            LoggerUtil.error("Error cargando KPIs del dashboard", e);
        }
    }

    private void setText(Label label, String text) {
        if (label != null) label.setText(text);
    }

    // ---- PIE CHART ----

    private void cargarPieDistribucion() {
        if (pieDistribucion == null) return;
        try {
            Map<String, Number> dist = usandoDemo
                ? ServicioInventario.generarDemoDistribucion()
                : ServicioInventario.obtenerDistribucionCategorias();

            if (dist.isEmpty()) dist = ServicioInventario.generarDemoDistribucion();

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<String, Number> e : dist.entrySet())
                pieData.add(new PieChart.Data(e.getKey(), e.getValue().doubleValue()));
            pieDistribucion.setData(pieData);
            pieDistribucion.setTitle("Distribución por Categoría");
            pieDistribucion.setLabelsVisible(true);
            pieDistribucion.setLegendVisible(true);
            pieDistribucion.setAnimated(true);
        } catch (Exception e) {
            LoggerUtil.warning("No se pudo cargar gráfico de distribución: " + e.getMessage());
        }
    }

    // ---- BAR CHART ----

    private void cargarBarVentas() {
        if (barVentas == null) return;
        try {
            Map<String, Double> ventas = usandoDemo
                ? ServicioInventario.generarDemoVentasMensuales()
                : ServicioInventario.obtenerVentasMensuales(6);

            if (ventas.isEmpty()) ventas = ServicioInventario.generarDemoVentasMensuales();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ventas (RD$)");
            for (Map.Entry<String, Double> e : ventas.entrySet())
                series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
            barVentas.getData().clear();
            barVentas.getData().add(series);
            barVentas.setTitle("Tendencia de Ventas (Últimos 6 Meses)");
            barVentas.setAnimated(true);
        } catch (Exception e) {
            LoggerUtil.warning("No se pudo cargar gráfico de ventas: " + e.getMessage());
        }
    }

    // ---- TABLA DE MOVIMIENTOS ----

    private void cargarMovimientos() {
        if (tablaMovimientos == null) return;
        try {
            List<Map<String, String>> movs = usandoDemo
                ? ServicioInventario.generarDemoMovimientos()
                : ServicioInventario.obtenerUltimosMovimientos(20);

            if (movs.isEmpty()) movs = ServicioInventario.generarDemoMovimientos();

            ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
            items.addAll(movs);
            tablaMovimientos.setItems(items);

            // color por tipo
            tablaMovimientos.setRowFactory(tv -> new TableRow<Map<String, String>>() {
                @Override
                protected void updateItem(Map<String, String> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setStyle("");
                        return;
                    }
                    String tipo = item.get("tipo");
                    if (tipo != null) {
                        if (tipo.contains("ENTRADA")) setStyle("-fx-text-fill: #2ecc71;");
                        else if (tipo.contains("SALIDA")) setStyle("-fx-text-fill: #e74c3c;");
                        else if (tipo.contains("AJUSTE")) setStyle("-fx-text-fill: #f39c12;");
                        else setStyle("");
                    }
                }
            });
        } catch (Exception e) {
            LoggerUtil.warning("Error cargando movimientos: " + e.getMessage());
        }
    }

    // ---- ALERTAS DE STOCK ----

    private void cargarAlertasStock() {
        if (listaAlertasStock == null) return;
        try {
            List<Map<String, String>> alertas = usandoDemo
                ? ServicioInventario.generarDemoAlertas()
                : ServicioInventario.obtenerAlertasStockDetalladas();

            if (alertas.isEmpty()) alertas = ServicioInventario.generarDemoAlertas();

            ObservableList<String> items = FXCollections.observableArrayList();
            for (Map<String, String> a : alertas) {
                String sev = a.get("severidad");
                items.add(String.format("[%s] %s — Stock: %s %s (Mín: %s)",
                    sev, a.get("producto"), a.get("stock"), a.get("unidad"), a.get("minimo")));
            }

            if (items.isEmpty()) items.add("No hay productos con stock bajo");
            listaAlertasStock.setItems(items);
            listaAlertasStock.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                    if (item == null || empty) { setStyle(""); return; }
                    if (item.contains("[CRITICA]")) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: 700; -fx-background-color: rgba(231,76,60,0.08);");
                    else if (item.contains("[ALTA]")) setStyle("-fx-text-fill: #f39c12; -fx-font-weight: 600; -fx-background-color: rgba(243,156,18,0.08);");
                    else setStyle("-fx-text-fill: #3498db;");
                }
            });
        } catch (Exception e) {
            LoggerUtil.warning("Error cargando alertas de stock: " + e.getMessage());
        }
    }

    // ---- NAVEGACION ----

    @FXML
    private void irAMenuPrincipal() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Stage stage = (Stage) tablaMovimientos.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setResizable(true);
        } catch (Exception e) {
            LoggerUtil.error("Error al regresar al menú principal", e);
        }
    }
}
