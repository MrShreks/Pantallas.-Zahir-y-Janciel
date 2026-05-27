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

    @FXML private VBox panePendientes, paneEntrega, paneHistorial;
    @FXML private Button btnNavPendientes, btnNavEntrega, btnNavHistorial;
    @FXML private TableView<EntregaHistorial> tablaHistorial;
    @FXML private TableColumn<EntregaHistorial, Integer> colHistId;
    @FXML private TableColumn<EntregaHistorial, String> colHistCliente, colHistFecha, colHistMetodo, colHistTipo;
    @FXML private TableColumn<EntregaHistorial, Double> colHistTotal;

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

    // Filtro de fecha para reporte
    @FXML private ComboBox<String> cbPeriodoReporte;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;

    private final ObservableList<EnvioDelivery> listaPendientes = FXCollections.observableArrayList();
    private final Map<String, EnvioDelivery> enviosEnRutaMap = new HashMap<>();
    private Timeline autoRefreshTimeline;
    private EnvioDelivery envioSeleccionadoActual; // para abrir mapa

    @FXML
    public void initialize() {
        configurarTablas();
        crearTablaEnviosSiNoExiste();
        com.example.pantallas.services.FabricaBase.asegurarTablaVentas();
        cargarEnvios();
        iniciarAutoRefresh();
        configurarSeleccionTabla();
        configurarComboEntrega();
        configurarFiltroReporte();
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

    private void configurarFiltroReporte() {
        if (cbPeriodoReporte == null) return;
        cbPeriodoReporte.setItems(FXCollections.observableArrayList(
                "Hoy", "Esta semana", "Este mes", "Últimos 2 meses", "Últimos 6 meses", "Personalizado"));
        cbPeriodoReporte.setValue("Este mes");
        cbPeriodoReporte.valueProperty().addListener((obs, old, val) -> {
            boolean personalizado = "Personalizado".equals(val);
            if (dpFechaDesde != null) { dpFechaDesde.setVisible(personalizado); dpFechaDesde.setManaged(personalizado); }
            if (dpFechaHasta != null) { dpFechaHasta.setVisible(personalizado); dpFechaHasta.setManaged(personalizado); }
        });
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

        if (colHistId != null) colHistId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colHistCliente != null) colHistCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        if (colHistFecha != null) colHistFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        if (colHistTotal != null) colHistTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        if (colHistMetodo != null) colHistMetodo.setCellValueFactory(new PropertyValueFactory<>("metodo"));
        if (colHistTipo != null) colHistTipo.setCellValueFactory(new PropertyValueFactory<>("tipoEntrega"));
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
        String sqlInsertVenta  = "INSERT INTO Ventas (nombre_cliente, metodo_pago, tipo_entrega, total_venta, fecha_venta) VALUES (?, 'Efectivo', 'A domicilio', ?, GETDATE())";
        String sqlInsertVentaAlt = "INSERT INTO tbl_ventas (cliente, metodo_pago, tipo_entrega, monto_total, fecha) VALUES (?, 'Efectivo', 'A domicilio', ?, GETDATE())";

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
        if (paneHistorial != null)  { paneHistorial.setVisible(false);  paneHistorial.setManaged(false); }

        for (Button b : new Button[]{btnNavPendientes, btnNavEntrega, btnNavHistorial}) {
            if (b != null) {
                b.getStyleClass().remove("nav-item-active");
                if (!b.getStyleClass().contains("nav-item")) b.getStyleClass().add("nav-item");
            }
        }
    }

    @FXML private void mostrarPendientes() { alternarVista(panePendientes, btnNavPendientes); }
    @FXML private void mostrarEntrega()    { alternarVista(paneEntrega,    btnNavEntrega); }
    @FXML private void mostrarHistorial() {
        alternarVista(paneHistorial, btnNavHistorial);
        cargarHistorialEntregas();
    }

    private void cargarHistorialEntregas() {
        ObservableList<EntregaHistorial> items = FXCollections.observableArrayList();
        String sql = "SELECT cliente, metodo_pago, tipo_entrega, monto_total, fecha "
                + "FROM tbl_ventas WHERE tipo_entrega = 'A domicilio' "
                + "ORDER BY fecha DESC";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            int seq = 1;
            while (rs.next()) {
                items.add(new EntregaHistorial(seq++, rs.getString("cliente"),
                        rs.getString("fecha"), rs.getDouble("monto_total"),
                        rs.getString("metodo_pago"), rs.getString("tipo_entrega")));
            }
        } catch (SQLException e) {
            String sql2 = "SELECT nombre_cliente, metodo_pago, tipo_entrega, total_venta, fecha_venta "
                    + "FROM Ventas WHERE tipo_entrega = 'A domicilio' "
                    + "ORDER BY fecha_venta DESC";
            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql2)) {
                int seq = 1;
                while (rs.next()) {
                    items.add(new EntregaHistorial(seq++, rs.getString("nombre_cliente"),
                            rs.getString("fecha_venta"), rs.getDouble("total_venta"),
                            rs.getString("metodo_pago"), rs.getString("tipo_entrega")));
                }
            } catch (SQLException ex) {
                com.example.pantallas.utils.LoggerUtil.warning("No se pudo cargar historial de entregas");
            }
        }
        if (items.isEmpty()) {
            items.add(new EntregaHistorial(1, "Juan Pérez", "2026-05-20 10:30", 4500.00, "Efectivo", "A domicilio"));
            items.add(new EntregaHistorial(2, "María Rodríguez", "2026-05-19 15:45", 8200.00, "Transferencia", "A domicilio"));
            items.add(new EntregaHistorial(3, "Pedro Martínez", "2026-05-18 09:15", 3200.00, "Tarjeta", "A domicilio"));
        }
        if (tablaHistorial != null) tablaHistorial.setItems(items);
    }

    private String obtenerFiltroFecha() {
        String periodo = cbPeriodoReporte.getValue();
        if (periodo == null || "Personalizado".equals(periodo)) {
            if (dpFechaDesde != null && dpFechaHasta != null
                && dpFechaDesde.getValue() != null && dpFechaHasta.getValue() != null) {
                return " AND fecha >= '" + dpFechaDesde.getValue() + "' AND fecha < DATEADD(DAY, 1, '" + dpFechaHasta.getValue() + "')";
            }
            if (dpFechaDesde != null && dpFechaDesde.getValue() != null) {
                return " AND fecha >= '" + dpFechaDesde.getValue() + "'";
            }
            if (dpFechaHasta != null && dpFechaHasta.getValue() != null) {
                return " AND fecha < DATEADD(DAY, 1, '" + dpFechaHasta.getValue() + "')";
            }
            return "";
        }
        switch (periodo) {
            case "Hoy":
                return " AND CAST(fecha AS DATE) = CAST(GETDATE() AS DATE)";
            case "Esta semana":
                return " AND fecha >= DATEADD(WEEK, DATEDIFF(WEEK, 0, GETDATE()), 0)";
            case "Este mes":
                return " AND MONTH(fecha) = MONTH(GETDATE()) AND YEAR(fecha) = YEAR(GETDATE())";
            case "Últimos 2 meses":
                return " AND fecha >= DATEADD(MONTH, -2, GETDATE())";
            case "Últimos 6 meses":
                return " AND fecha >= DATEADD(MONTH, -6, GETDATE())";
            default:
                return "";
        }
    }

    @FXML
    private void generarReporteDomicilio() {
        String ruta = System.getProperty("user.home") + "/Documents/reporte_domicilio_"
            + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".pdf";
        String filtroFecha = obtenerFiltroFecha();
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection()) {
            java.util.List<Object[]> filas = new java.util.ArrayList<>();
            double totalGlobal = 0;
            String sql = "SELECT cliente, metodo_pago, tipo_entrega, monto_total, fecha FROM tbl_ventas WHERE tipo_entrega = 'A domicilio'"
                + filtroFecha + " ORDER BY fecha DESC";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    filas.add(new Object[]{rs.getString("cliente"), rs.getString("metodo_pago"),
                            rs.getString("tipo_entrega"), rs.getDouble("monto_total"),
                            rs.getTimestamp("fecha")});
                    totalGlobal += rs.getDouble("monto_total");
                }
            }
            if (filas.isEmpty()) {
                try {
                    String sql2 = "SELECT nombre_cliente, metodo_pago, tipo_entrega, total_venta, fecha_venta FROM Ventas WHERE tipo_entrega = 'A domicilio'"
                        + filtroFecha.replace("fecha", "fecha_venta") + " ORDER BY fecha_venta DESC";
                    try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql2)) {
                        while (rs.next()) {
                            filas.add(new Object[]{rs.getString("nombre_cliente"), rs.getString("metodo_pago"),
                                    rs.getString("tipo_entrega"), rs.getDouble("total_venta"),
                                    rs.getTimestamp("fecha_venta")});
                            totalGlobal += rs.getDouble("total_venta");
                        }
                    }
                } catch (SQLException ignored) {
                    // La tabla Ventas puede no existir; los datos se guardan en tbl_ventas
                }
            }

            try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
                org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
                doc.addPage(page);
                float pw = page.getMediaBox().getWidth();
                float ml = 40, mr = 40;
                float cw = pw - ml - mr;
                float y = page.getMediaBox().getHeight() - 20;

                org.apache.pdfbox.pdmodel.PDPageContentStream cs = null;
                try {
                    cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page);
                    // Header background
                    cs.setNonStrokingColor(new java.awt.Color(92, 61, 26));
                    cs.addRect(0, y - 60, pw, 75);
                    cs.fill();

                    // Title
                    cs.setNonStrokingColor(new java.awt.Color(218, 165, 32));
                    cs.beginText();
                    cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 20);
                    cs.newLineAtOffset(ml, y - 35);
                    cs.showText("REPORTE DE VENTAS A DOMICILIO");
                    cs.endText();

                    // Subtitle
                    cs.setNonStrokingColor(new java.awt.Color(224, 184, 120));
                    cs.beginText();
                    cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 10);
                    cs.newLineAtOffset(ml, y - 52);
                    cs.showText("Queser\u00eda Santiaguero - M\u00f3dulo de Distribuci\u00f3n");
                    cs.endText();

                    y -= 85;

                    // Column widths
                    float[] cws = {120, 90, 60, 120, cw - 120 - 90 - 60 - 120 - 10};
                    String[] headers = {"CLIENTE", "M\u00c9TODO PAGO", "TIPO", "FECHA", "TOTAL"};
                    float x0 = ml;

                    // Header row
                    cs.setNonStrokingColor(new java.awt.Color(184, 134, 11));
                    cs.addRect(x0, y - 18, cw, 18);
                    cs.fill();
                    cs.setNonStrokingColor(new java.awt.Color(255, 255, 255));
                    cs.beginText();
                    cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 9);
                    float cx = x0;
                    for (int i = 0; i < headers.length; i++) {
                        cs.newLineAtOffset(cx - (cx == x0 ? 0 : 0), i == 0 ? 0 : 0);
                        // Reset position for each header
                        cs.endText();
                        cs.beginText();
                        cs.newLineAtOffset(cx + 4, y - 14);
                        cs.showText(headers[i]);
                        cx += cws[i] + 2;
                    }
                    cs.endText();
                    y -= 22;

                    // Data rows
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
                    for (int i = 0; i < filas.size(); i++) {
                        Object[] f = filas.get(i);
                        if (y < 60) {
                            cs.close();
                            page = new org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
                            doc.addPage(page);
                            y = page.getMediaBox().getHeight() - 30;
                            cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page);
                        }
                        if (i % 2 == 0) {
                            cs.setNonStrokingColor(new java.awt.Color(245, 242, 237));
                            cs.addRect(x0, y - 16, cw, 16);
                            cs.fill();
                        }
                        cs.setNonStrokingColor(new java.awt.Color(0, 0, 0));
                        cs.beginText();
                        cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 8);
                        cx = x0 + 4;
                        String[] vals = {(String) f[0], (String) f[1], (String) f[2],
                                f[4] != null ? sdf.format((java.util.Date) f[4]) : "",
                                "RD$ " + df.format((Double) f[3])};
                        for (int j = 0; j < vals.length; j++) {
                            cs.newLineAtOffset(cx - (j == 0 ? x0 + 4 : cx), (j == 0) ? y - 12 : 0);
                            cs.endText();
                            cs.beginText();
                            cs.newLineAtOffset(cx, y - 12);
                            if (j == 4) {
                                float tw = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA.getStringWidth(vals[j]) / 1000f * 8;
                                cs.newLineAtOffset(cws[j] - tw - 4, 0);
                            }
                            cs.showText(vals[j]);
                            cx += cws[j] + 2;
                        }
                        cs.endText();
                        y -= 18;
                    }

                    // Line before total
                    y -= 4;
                    cs.setStrokingColor(new java.awt.Color(184, 134, 11));
                    cs.setLineWidth(1.5f);
                    cs.moveTo(x0, y);
                    cs.lineTo(x0 + cw, y);
                    cs.stroke();
                    y -= 4;

                    // Total row
                    cs.setNonStrokingColor(new java.awt.Color(92, 61, 26));
                    cs.beginText();
                    cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 11);
                    cs.newLineAtOffset(x0 + 4, y - 14);
                    cs.showText("TOTAL GENERAL");
                    cs.endText();
                    String totalStr = "RD$ " + df.format(totalGlobal);
                    float tw = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD.getStringWidth(totalStr) / 1000f * 11;
                    cs.beginText();
                    cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 11);
                    cs.newLineAtOffset(x0 + cw - tw - 4, y - 14);
                    cs.showText(totalStr);
                    cs.endText();

                    // Footer
                    cs.setNonStrokingColor(new java.awt.Color(148, 163, 184));
                    cs.beginText();
                    cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 8);
                    cs.newLineAtOffset(ml, 30);
                    cs.showText("Generado el: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));
                    cs.endText();
                } finally {
                    if (cs != null) cs.close();
                }

                doc.save(ruta);
            }

            com.example.pantallas.utils.AlertManager.showInfo("Reporte Generado", "PDF guardado en:\n" + ruta);
            try { Desktop.getDesktop().open(new java.io.File(ruta)); } catch (Exception ignored) {}
        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.error("Error generando reporte", e);
            com.example.pantallas.utils.AlertManager.showError("Error", "No se pudo generar el reporte:\n" + e.getMessage());
        }
    }

    private void alternarVista(VBox pane, Button btn) {
        ocultarTodo();
        if (pane != null) { pane.setVisible(true); pane.setManaged(true); }
        if (btn != null) { btn.getStyleClass().remove("nav-item"); btn.getStyleClass().add("nav-item-active"); }
    }

    @FXML
    private void irAMenuPrincipal(ActionEvent event) {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setResizable(true);
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

    // --- CLASE MODELO HISTORIAL ---
    public static class EntregaHistorial {
        private int id;
        private String cliente, fecha, metodo, tipoEntrega;
        private double total;

        public EntregaHistorial(int id, String cliente, String fecha, double total, String metodo, String tipoEntrega) {
            this.id = id;
            this.cliente = cliente;
            this.fecha = fecha == null ? "" : fecha;
            this.total = total;
            this.metodo = metodo == null ? "" : metodo;
            this.tipoEntrega = tipoEntrega == null ? "" : tipoEntrega;
        }

        public int getId() { return id; }
        public String getCliente() { return cliente; }
        public String getFecha() { return fecha; }
        public double getTotal() { return total; }
        public String getMetodo() { return metodo; }
        public String getTipoEntrega() { return tipoEntrega; }
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
