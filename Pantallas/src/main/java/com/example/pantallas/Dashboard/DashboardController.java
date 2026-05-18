package com.example.pantallas.Dashboard;

import com.example.pantallas.SessionManager;
import com.example.pantallas.base.FabricaBase;
import com.example.pantallas.base.ServicioInventario;
import com.example.pantallas.dominio.MovimientoInventario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * DashboardController - Panel de Control Empresarial
 * Muestra estadísticas reales de inventario, producción, ventas y alertas.
 */
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

    @FXML private TableView<MovimientoInventario> tablaMovimientos;
    @FXML private TableColumn<MovimientoInventario, String> colMovFecha;
    @FXML private TableColumn<MovimientoInventario, String> colMovProducto;
    @FXML private TableColumn<MovimientoInventario, String> colMovTipo;
    @FXML private TableColumn<MovimientoInventario, String> colMovCantidad;
    @FXML private TableColumn<MovimientoInventario, String> colMovUsuario;

    @FXML private ListView<String> listaAlertasStock;

    private final ObservableList<MovimientoInventario> movimientos = FXCollections.observableArrayList();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (lblFecha != null) {
            lblFecha.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        if (lblUsuarioLogueado != null) {
            String usuario = SessionManager.getUsuarioActual();
            lblUsuarioLogueado.setText(usuario != null ? usuario.toUpperCase() : "INVITADO");
        }
        inicializarTabla();
        cargarDatosDashboard();
    }

    private void inicializarTabla() {
        if (colMovFecha != null)
            colMovFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                    c.getValue().getFecha().format(dtf)));
        if (colMovProducto != null)
            colMovProducto.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProducto()));
        if (colMovTipo != null)
            colMovTipo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTipo()));
        if (colMovCantidad != null)
            colMovCantidad.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                    String.format("%.2f %s", c.getValue().getCantidad(), c.getValue().getUnidad())));
        if (colMovUsuario != null)
            colMovUsuario.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsuario()));
    }

    private void cargarDatosDashboard() {
        cargarEstadisticas();
        cargarUltimosMovimientos();
        cargarAlertasStock();
    }

    private void cargarEstadisticas() {
        Map<String, Number> stats = ServicioInventario.obtenerEstadisticasDashboard();
        if (lblStockBajo != null)
            lblStockBajo.setText(stats.getOrDefault("stock_bajo", 0).intValue() + " productos");
        if (lblValorInventario != null)
            lblValorInventario.setText(String.format("RD$ %.2f", stats.getOrDefault("valor_inventario", 0.0).doubleValue()));
        if (lblProduccionActiva != null)
            lblProduccionActiva.setText(stats.getOrDefault("produccion_activa", 0).intValue() + " lotes");
        if (lblOrdenesPendientes != null)
            lblOrdenesPendientes.setText(stats.getOrDefault("ordenes_pendientes", 0).intValue() + " órdenes");
        if (lblVentasMes != null)
            lblVentasMes.setText(String.format("RD$ %.2f", stats.getOrDefault("ventas_mes", 0.0).doubleValue()));
    }

    private void cargarUltimosMovimientos() {
        if (tablaMovimientos == null) return;
        movimientos.clear();
        String sql = "SELECT TOP 20 * FROM tbl_movimientos_inventario ORDER BY fecha_movimiento DESC";
        try (Connection con = FabricaBase.abrirConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                movimientos.add(new MovimientoInventario(
                        rs.getInt("id_movimiento"),
                        rs.getString("producto"),
                        rs.getString("tipo"),
                        rs.getDouble("cantidad"),
                        rs.getString("unidad"),
                        rs.getTimestamp("fecha_movimiento").toLocalDateTime(),
                        rs.getObject("usuario") != null ? rs.getString("usuario") : "Sistema"
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        tablaMovimientos.setItems(movimientos);
    }

    private void cargarAlertasStock() {
        if (listaAlertasStock == null) return;
        ObservableList<String> alertas = FXCollections.observableArrayList();
        String sql = "SELECT nombre_producto, cantidad_stock, cantidad_minima, unidad_medida " +
                "FROM tbl_inventario_productos WHERE cantidad_stock <= cantidad_minima ORDER BY cantidad_stock ASC";
        try (Connection con = FabricaBase.abrirConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                alertas.add(String.format("%s - %.2f %s (mín: %.2f)",
                        rs.getString("nombre_producto"),
                        rs.getDouble("cantidad_stock"),
                        rs.getString("unidad_medida"),
                        rs.getDouble("cantidad_minima")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        listaAlertasStock.setItems(alertas);
    }

    @FXML
    private void irAMenuPrincipal() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) listaAlertasStock.getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(true);
            stage.setMaximized(wasMaximized);
            if (!wasMaximized) stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
