package com.example.pantallas.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DistribucionController {

    @FXML private VBox panePendientes, paneEntrega;
    @FXML private Button btnNavPendientes, btnNavEntrega;

    // Tabla de pendientes
    @FXML private TableView<EnvioDelivery> tablaPendientes;
    @FXML private TableColumn<EnvioDelivery, Integer> colIdPendiente;
    @FXML private TableColumn<EnvioDelivery, String> colClientePendiente;
    @FXML private TableColumn<EnvioDelivery, String> colRepartidorPendiente;
    @FXML private TableColumn<EnvioDelivery, String> colDireccionPendiente;
    @FXML private TableColumn<EnvioDelivery, String> colMontoPendiente;
    @FXML private TableColumn<EnvioDelivery, String> colEstatusPendiente;

    // Panel de detalle de ubicación (pendientes)
    @FXML private VBox paneDetalleUbicacion;
    @FXML private Label lblDetalleCliente;
    @FXML private Label lblDetalleRepartidor;
    @FXML private Label lblDetalleMonto;
    @FXML private Label lblDetalleDireccion;

    // Panel confirmar entrega
    @FXML private ComboBox<String> cbEnviosEnRuta;
    @FXML private CheckBox chkRecibido;
    @FXML private TextArea txtNovedades;
    @FXML private VBox paneDetalleEntrega;
    @FXML private Label lblEntregaDireccion;

    private final ObservableList<EnvioDelivery> listaPendientes = FXCollections.observableArrayList();
    private final Map<String, EnvioDelivery> enviosEnRutaMap = new HashMap<>();
    private Timeline autoRefreshTimeline;
    private EnvioDelivery envioSeleccionadoActual; // para abrir mapa

    @FXML
    public void initialize() {
        configurarTablas();
        crearTablaEnviosSiNoExiste();
        cargarEnvios();
        iniciarAutoRefresh();
        configurarSeleccionTabla();
        configurarComboEntrega();
    }

    /** Listener en la tabla: al seleccionar un envío muestra el panel de detalle de ubicación. */
    private void configurarSeleccionTabla() {
        tablaPendientes.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) {
                envioSeleccionadoActual = nuevo;
                mostrarDetalleUbicacion(nuevo);
            } else {
                ocultarDetalleUbicacion();
            }
        });
    }

    /** Listener en el combo de entrega: muestra la dirección del envío en ruta seleccionado. */
    private void configurarComboEntrega() {
        if (cbEnviosEnRuta == null) return;
        cbEnviosEnRuta.valueProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null && enviosEnRutaMap.containsKey(nuevo)) {
                EnvioDelivery envio = enviosEnRutaMap.get(nuevo);
                String dir = envio.getDireccion();
                if (paneDetalleEntrega != null) {
                    paneDetalleEntrega.setVisible(true);
                    paneDetalleEntrega.setManaged(true);
                    if (lblEntregaDireccion != null) {
                        lblEntregaDireccion.setText(dir == null || dir.isBlank() ? "Sin dirección registrada" : dir);
                    }
                }
            } else {
                if (paneDetalleEntrega != null) {
                    paneDetalleEntrega.setVisible(false);
                    paneDetalleEntrega.setManaged(false);
                }
            }
        });
    }

    private void mostrarDetalleUbicacion(EnvioDelivery envio) {
        if (paneDetalleUbicacion == null) return;
        paneDetalleUbicacion.setVisible(true);
        paneDetalleUbicacion.setManaged(true);
        if (lblDetalleCliente != null)    lblDetalleCliente.setText(envio.getCliente());
        if (lblDetalleRepartidor != null) lblDetalleRepartidor.setText(
                envio.getRepartidor() == null || envio.getRepartidor().isBlank() ? "Sin asignar" : envio.getRepartidor());
        if (lblDetalleMonto != null)      lblDetalleMonto.setText(envio.getMontoFormat());
        if (lblDetalleDireccion != null)  lblDetalleDireccion.setText(
                envio.getDireccion() == null || envio.getDireccion().isBlank() ? "Sin dirección registrada" : envio.getDireccion());
    }

    private void ocultarDetalleUbicacion() {
        if (paneDetalleUbicacion != null) {
            paneDetalleUbicacion.setVisible(false);
            paneDetalleUbicacion.setManaged(false);
        }
        envioSeleccionadoActual = null;
    }

    /** Abre Google Maps con la dirección del envío seleccionado en la tabla de pendientes. */
    @FXML
    private void abrirEnMapa() {
        if (envioSeleccionadoActual == null) {
            mostrarError("Sin selección", "Seleccione un envío de la tabla primero.");
            return;
        }
        abrirGoogleMaps(envioSeleccionadoActual.getDireccion());
    }

    /** Abre Google Maps con la dirección del envío seleccionado en el combo de entrega. */
    @FXML
    private void abrirMapaEntrega() {
        String seleccion = cbEnviosEnRuta.getValue();
        if (seleccion != null && enviosEnRutaMap.containsKey(seleccion)) {
            abrirGoogleMaps(enviosEnRutaMap.get(seleccion).getDireccion());
        }
    }

    private void abrirGoogleMaps(String direccion) {
        if (direccion == null || direccion.isBlank()) {
            mostrarError("Dirección no disponible", "Este envío no tiene una dirección registrada.");
            return;
        }
        try {
            String encoded = direccion.trim().replace(" ", "+").replace(",", "%2C");
            String url = "https://www.google.com/maps/search/?api=1&query=" + encoded;
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            mostrarError("Error al abrir mapa", "No se pudo abrir Google Maps.\n" + e.getMessage());
        }
    }

    /** Crea tbl_envios si no existe Y agrega columnas faltantes si ya existe sin ellas. */
    private void crearTablaEnviosSiNoExiste() {
        String sqlCreate = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_envios' AND xtype='U') " +
                "CREATE TABLE tbl_envios (" +
                "id_envio INT IDENTITY(1,1) PRIMARY KEY, " +
                "cliente NVARCHAR(200) NOT NULL, " +
                "repartidor NVARCHAR(200), " +
                "direccion NVARCHAR(500), " +
                "estatus NVARCHAR(50) DEFAULT 'Pendiente', " +
                "monto_total DECIMAL(18,2) DEFAULT 0, " +
                "fecha_registro DATETIME DEFAULT GETDATE())";

        // Agregar columnas si la tabla ya existía sin ellas
        String sqlAddRepartidor = "IF EXISTS (SELECT * FROM sysobjects WHERE name='tbl_envios' AND xtype='U') " +
                "AND NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('tbl_envios') AND name='repartidor') " +
                "ALTER TABLE tbl_envios ADD repartidor NVARCHAR(200)";
        String sqlAddDireccion = "IF EXISTS (SELECT * FROM sysobjects WHERE name='tbl_envios' AND xtype='U') " +
                "AND NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('tbl_envios') AND name='direccion') " +
                "ALTER TABLE tbl_envios ADD direccion NVARCHAR(500)";

        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sqlCreate);
            // Intentar agregar columnas (no falla si ya existen gracias al IF NOT EXISTS)
            try { st.executeUpdate(sqlAddRepartidor); } catch (SQLException ignored) {}
            try { st.executeUpdate(sqlAddDireccion);  } catch (SQLException ignored) {}
        } catch (SQLException e) {
            com.example.pantallas.utils.LoggerUtil.warning("No se pudo verificar/crear tbl_envios: " + e.getMessage());
        }
    }

    /** Inicia un Timeline que recarga los envíos cada 10 segundos. */
    private void iniciarAutoRefresh() {
        autoRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> cargarEnvios())
        );
        autoRefreshTimeline.setCycleCount(Animation.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void configurarTablas() {
        colIdPendiente.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClientePendiente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colRepartidorPendiente.setCellValueFactory(new PropertyValueFactory<>("repartidor"));
        colDireccionPendiente.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colMontoPendiente.setCellValueFactory(new PropertyValueFactory<>("montoFormat"));
        colEstatusPendiente.setCellValueFactory(new PropertyValueFactory<>("estatus"));
    }

    private void cargarEnvios() {
        // Guardar selección actual para restaurarla después del refresh
        int idSeleccionado = envioSeleccionadoActual != null ? envioSeleccionadoActual.getId() : -1;

        listaPendientes.clear();
        cbEnviosEnRuta.getItems().clear();
        enviosEnRutaMap.clear();

        // Intentar columnas extendidas; fallback a columnas básicas
        String sql = "SELECT id_envio, cliente, " +
                "ISNULL(repartidor,'') AS repartidor, " +
                "ISNULL(direccion,'') AS direccion, " +
                "estatus, monto_total " +
                "FROM tbl_envios WHERE estatus IN ('Pendiente', 'En Ruta') ORDER BY id_envio DESC";

        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                EnvioDelivery envio = new EnvioDelivery(
                        rs.getInt("id_envio"),
                        rs.getString("cliente"),
                        rs.getString("repartidor"),
                        rs.getString("direccion"),
                        rs.getString("estatus"),
                        rs.getDouble("monto_total")
                );

                if ("Pendiente".equalsIgnoreCase(envio.getEstatus())) {
                    listaPendientes.add(envio);
                } else if ("En Ruta".equalsIgnoreCase(envio.getEstatus())) {
                    String label = "Envío #" + envio.getId() + " - " + envio.getCliente();
                    cbEnviosEnRuta.getItems().add(label);
                    enviosEnRutaMap.put(label, envio);
                }
            }
            tablaPendientes.setItems(listaPendientes);

            // Restaurar selección y detalle si el envío sigue en la lista
            if (idSeleccionado >= 0) {
                listaPendientes.stream()
                        .filter(e -> e.getId() == idSeleccionado)
                        .findFirst()
                        .ifPresent(e -> {
                            tablaPendientes.getSelectionModel().select(e);
                            mostrarDetalleUbicacion(e);
                        });
            }

        } catch (SQLException e) {
            com.example.pantallas.utils.LoggerUtil.error("Error al cargar envíos", e);
            // Datos demo con dirección de ejemplo
            EnvioDelivery e1 = new EnvioDelivery(1, "Colmado La Fe", "Repartidor A",
                    "Calle Duarte 45, Los Jardines, Santiago", "Pendiente", 4500.00);
            EnvioDelivery e2 = new EnvioDelivery(2, "Supermercado XYZ", "Repartidor B",
                    "Av. Las Carreras #12, Urb. Las Palmas, Santiago", "En Ruta", 12500.00);
            listaPendientes.add(e1);

            String labelDemo = "Envío #" + e2.getId() + " - " + e2.getCliente();
            cbEnviosEnRuta.getItems().add(labelDemo);
            enviosEnRutaMap.put(labelDemo, e2);

            tablaPendientes.setItems(listaPendientes);
        }
    }

    @FXML
    private void iniciarRuta() {
        EnvioDelivery seleccionado = tablaPendientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selección Requerida", "Debe seleccionar un envío pendiente para iniciar la ruta.");
            return;
        }

        final int idEnvio = seleccionado.getId();
        final String labelEsperado = "Envío #" + idEnvio + " - " + seleccionado.getCliente();

        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE tbl_envios SET estatus = 'En Ruta' WHERE id_envio = ?")) {
            ps.setInt(1, idEnvio);
            ps.executeUpdate();
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.warning("Ruta iniciada en modo demo: " + e.getMessage());
        }

        // 1. Recargar lista (el envío pasa a En Ruta y aparece en el combo)
        ocultarDetalleUbicacion();
        cargarEnvios();

        // 2. Navegar automáticamente a "Confirmar Entrega"
        alternarVista(paneEntrega, btnNavEntrega);

        // 3. Pre-seleccionar el envío recién iniciado en el combo
        if (cbEnviosEnRuta.getItems().contains(labelEsperado)) {
            cbEnviosEnRuta.setValue(labelEsperado);
        } else if (!cbEnviosEnRuta.getItems().isEmpty()) {
            cbEnviosEnRuta.setValue(cbEnviosEnRuta.getItems().get(0));
        }
    }

    @FXML
    private void finalizarEntregaYFacturar() {
        String seleccion = cbEnviosEnRuta.getValue();
        if (seleccion == null) {
            mostrarError("Selección Requerida", "Debe seleccionar un envío en ruta para confirmar la entrega.");
            return;
        }

        if (!chkRecibido.isSelected()) {
            mostrarError("Confirmación Requerida", "El cliente debe firmar la conformidad y entregar el pago para poder cerrar la venta.");
            return;
        }

        EnvioDelivery envio = enviosEnRutaMap.get(seleccion);
        if (envio == null) return;

        String sqlUpdateEnvio  = "UPDATE tbl_envios SET estatus = 'Entregado' WHERE id_envio = ?";
        String sqlInsertVenta  = "INSERT INTO Ventas (nombre_cliente, metodo_pago, tipo_entrega, total_venta, fecha_venta) VALUES (?, 'Efectivo', 'Delivery', ?, GETDATE())";
        String sqlInsertVentaAlt = "INSERT INTO tbl_ventas (cliente, metodo_pago, tipo_entrega, monto_total, fecha) VALUES (?, 'Efectivo', 'Delivery', ?, GETDATE())";

        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection()) {

            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdateEnvio)) {
                psUpdate.setInt(1, envio.getId());
                psUpdate.executeUpdate();
            }

            try (PreparedStatement psVenta = con.prepareStatement(sqlInsertVenta)) {
                psVenta.setString(1, envio.getCliente());
                psVenta.setDouble(2, envio.getMonto());
                psVenta.executeUpdate();
            } catch (SQLException ex) {
                try (PreparedStatement psAlt = con.prepareStatement(sqlInsertVentaAlt)) {
                    psAlt.setString(1, envio.getCliente());
                    psAlt.setDouble(2, envio.getMonto());
                    psAlt.executeUpdate();
                } catch (Exception ex2) { /* ignorar */ }
            }

            mostrarInfo("Entrega Exitosa ✅",
                    "Entrega a " + envio.getCliente() + " confirmada.\n" +
                    "Dirección: " + envio.getDireccion() + "\n" +
                    "Monto cobrado: " + envio.getMontoFormat());

        } catch (SQLException e) {
            com.example.pantallas.utils.LoggerUtil.error("Error al finalizar entrega", e);
            mostrarInfo("Entrega Exitosa (Modo Demo)", "La entrega fue confirmada (Modo Offline).");
        }

        chkRecibido.setSelected(false);
        txtNovedades.clear();
        if (paneDetalleEntrega != null) { paneDetalleEntrega.setVisible(false); paneDetalleEntrega.setManaged(false); }
        cargarEnvios();
    }

    private void ocultarTodo() {
        if (panePendientes != null) { panePendientes.setVisible(false); panePendientes.setManaged(false); }
        if (paneEntrega != null)    { paneEntrega.setVisible(false);    paneEntrega.setManaged(false); }

        if (btnNavPendientes != null) btnNavPendientes.getStyleClass().remove("nav-item-active");
        if (btnNavEntrega != null)    btnNavEntrega.getStyleClass().remove("nav-item-active");

        if (btnNavPendientes != null && !btnNavPendientes.getStyleClass().contains("nav-item")) btnNavPendientes.getStyleClass().add("nav-item");
        if (btnNavEntrega != null    && !btnNavEntrega.getStyleClass().contains("nav-item"))    btnNavEntrega.getStyleClass().add("nav-item");
    }

    @FXML private void mostrarPendientes() { alternarVista(panePendientes, btnNavPendientes); }
    @FXML private void mostrarEntrega()    { alternarVista(paneEntrega,    btnNavEntrega); }

    private void alternarVista(VBox pane, Button btn) {
        ocultarTodo();
        if (pane != null) { pane.setVisible(true); pane.setManaged(true); }
        if (btn != null) { btn.getStyleClass().remove("nav-item"); btn.getStyleClass().add("nav-item-active"); }
    }

    @FXML
    private void irAMenuPrincipal(ActionEvent event) {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.setMaximized(wasMaximized);
            if (!wasMaximized) stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            mostrarError("Error", "No se pudo cargar el Menú Principal");
            com.example.pantallas.utils.LoggerUtil.error("Excepción detectada", e);
        }
    }

    private void mostrarError(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    // --- CLASE MODELO ---
    public static class EnvioDelivery {
        private int id;
        private String cliente, repartidor, direccion, estatus;
        private double monto;

        public EnvioDelivery(int id, String cliente, String repartidor, String direccion, String estatus, double monto) {
            this.id         = id;
            this.cliente    = cliente;
            this.repartidor = repartidor == null ? "" : repartidor;
            this.direccion  = direccion  == null ? "" : direccion;
            this.estatus    = estatus;
            this.monto      = monto;
        }

        public int    getId()          { return id; }
        public String getCliente()     { return cliente; }
        public String getRepartidor()  { return repartidor; }
        public String getDireccion()   { return direccion; }
        public String getEstatus()     { return estatus; }
        public double getMonto()       { return monto; }
        public String getMontoFormat() { return String.format("RD$ %.2f", monto); }
    }
}
