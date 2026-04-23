package com.example.pantallas.ProcesoDeProduccion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;

public class ProduccionController {

    @FXML private VBox paneRecepcion, paneProduccion, paneCalidad, paneInventario, paneProveedores, paneSeguimiento;
    @FXML private HBox hbBotonesRecepcion;
    @FXML private ComboBox<String> cbProveedor, cbResultadoCalidad, cbCambiarEstado, cbTipoQueso, cbLotesTerminados, cbLoteProduccion;
    @FXML private TextField txtLitrosRecibidos, txtNombreProv, txtLibrasFinales;

    @FXML private TableView<RecepcionLeche> tablaRecepcion;
    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableView<LoteProduccion> tablaSeguimiento;
    @FXML private TableView<Inventario> tablaInventario;

    @FXML private TableColumn<RecepcionLeche, Integer> colID;
    @FXML private TableColumn<RecepcionLeche, String> colProv, colEstado;
    @FXML private TableColumn<RecepcionLeche, Double> colLitros;
    @FXML private TableColumn<Proveedor, Integer> colIdProv;
    @FXML private TableColumn<Proveedor, String> colNombreProv;
    @FXML private TableColumn<LoteProduccion, String> colSegLote, colSegTipo, colSegEstado;
    @FXML private TableColumn<Inventario, String> colInvLote, colInvCant, colInvFecha;

    private int idSeleccionado = -1;
    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;encrypt=true;trustServerCertificate=true;";
    private final String USER = "janciel";
    private final String PASS = "123456789";
    @FXML
    private Node contentArea;

    @FXML
    public void initialize() {
        configurarTablas();
        cargarDatosGlobales();

        if (tablaRecepcion != null) {
            tablaRecepcion.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    idSeleccionado = newVal.getId();
                    cbProveedor.setValue(newVal.getProveedor());
                    txtLitrosRecibidos.setText(String.valueOf(newVal.getLitros()));
                    hbBotonesRecepcion.setVisible(true);
                }
            });
        }

        cbResultadoCalidad.setItems(FXCollections.observableArrayList("Aprobado", "Rechazado"));
        cbCambiarEstado.setItems(FXCollections.observableArrayList("En Producción", "Completado"));
        cbTipoQueso.setItems(FXCollections.observableArrayList("Hoja", "Freir", "Crema"));
    }

    private void configurarTablas() {
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProv.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
        colLitros.setCellValueFactory(new PropertyValueFactory<>("litros"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colIdProv.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreProv.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colSegLote.setCellValueFactory(new PropertyValueFactory<>("loteId"));
        colSegTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colSegEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colInvLote.setCellValueFactory(new PropertyValueFactory<>("loteId"));
        colInvCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colInvFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
    }

    private void cargarDatosGlobales() {
        cargarRecepcion();
        cargarProveedores();
        cargarLotesSeguimiento();
        cargarInventario();
        cargarLotesParaCalidad();
        cargarLotesParaProduccion();
    }

    private void cargarRecepcion() {
        ObservableList<RecepcionLeche> lista = FXCollections.observableArrayList();
        try (Connection con = getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT id_recepcion, proveedor, cantidad_litros, estado FROM tbl_recepcion_leche")) {
            while (rs.next()) lista.add(new RecepcionLeche(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getString(4)));
            tablaRecepcion.setItems(lista);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarProveedores() {
        ObservableList<Proveedor> lista = FXCollections.observableArrayList();
        cbProveedor.getItems().clear();
        try (Connection con = getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT id_suplidor, nombre FROM tbl_suplidores")) {
            while (rs.next()) {
                lista.add(new Proveedor(rs.getInt(1), rs.getString(2)));
                cbProveedor.getItems().add(rs.getString(2));
            }
            tablaProveedores.setItems(lista);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarLotesSeguimiento() {
        ObservableList<LoteProduccion> lista = FXCollections.observableArrayList();
        try (Connection con = getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT lote_id, tipo_queso, estado FROM tbl_produccion")) {
            while (rs.next()) lista.add(new LoteProduccion(rs.getString(1), rs.getString(2), rs.getString(3)));
            tablaSeguimiento.setItems(lista);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarLotesParaCalidad() {
        cbLotesTerminados.getItems().clear();
        String sql = "SELECT lote_id FROM tbl_produccion WHERE estado = 'Completado'";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                cbLotesTerminados.getItems().add(rs.getString("lote_id"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarLotesParaProduccion() {
        cbLoteProduccion.getItems().clear();
        // Carga todos los lotes disponibles para iniciar proceso
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT lote_id FROM tbl_produccion")) {
            while (rs.next()) {
                cbLoteProduccion.getItems().add(rs.getString("lote_id"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarInventario() {
        ObservableList<Inventario> lista = FXCollections.observableArrayList();
        try (Connection con = getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT lote_id, cantidad_disponible, fecha_entrada FROM tbl_inventario_productos")) {
            while (rs.next()) lista.add(new Inventario(rs.getString(1), rs.getString(2), rs.getString(3)));
            tablaInventario.setItems(lista);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void iniciarLote() {
        if (cbLoteProduccion.getValue() == null || cbTipoQueso.getValue() == null) {
            mostrarAlerta("Error", "Seleccione el ID de lote y el tipo de queso.");
            return;
        }
        ejecutarUpdate("UPDATE tbl_produccion SET tipo_queso = ?, estado = 'En Producción' WHERE lote_id = ?",
                cbTipoQueso.getValue(), cbLoteProduccion.getValue());
        cargarLotesSeguimiento();
        mostrarAlerta("Éxito", "Proceso de producción iniciado.");
    }

    @FXML private void actualizarEstadoProduccion() {
        LoteProduccion seleccionado = tablaSeguimiento.getSelectionModel().getSelectedItem();
        if (seleccionado != null && cbCambiarEstado.getValue() != null) {
            ejecutarUpdate("UPDATE tbl_produccion SET estado = ? WHERE lote_id = ?",
                    cbCambiarEstado.getValue(), seleccionado.getLoteId());
            cargarLotesSeguimiento();
            cargarLotesParaCalidad();
        }
    }

    @FXML private void aprobarLote() {
        String lote = cbLotesTerminados.getValue();
        String libras = txtLibrasFinales.getText();
        String resultado = cbResultadoCalidad.getValue();

        if (lote == null || libras.isEmpty() || resultado == null) {
            mostrarAlerta("Error", "Complete todos los campos de calidad.");
            return;
        }

        if (resultado.equals("Aprobado")) {
            ejecutarUpdate("INSERT INTO tbl_inventario_productos (lote_id, cantidad_disponible, fecha_entrada) VALUES (?, ?, ?)",
                    lote, libras, Date.valueOf(LocalDate.now()));
            ejecutarUpdate("UPDATE tbl_produccion SET estado = 'Finalizado' WHERE lote_id = ?", lote);
            mostrarAlerta("Calidad", "Lote aprobado y movido a inventario.");
        } else {
            ejecutarUpdate("UPDATE tbl_produccion SET estado = 'Rechazado' WHERE lote_id = ?", lote);
            mostrarAlerta("Calidad", "Lote rechazado.");
        }

        txtLibrasFinales.clear();
        cargarLotesParaCalidad();
        cargarLotesSeguimiento();
        cargarInventario();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML private void registrarEntrada() { ejecutarUpdate("INSERT INTO tbl_recepcion_leche (proveedor, cantidad_litros, estado) VALUES (?, ?, 'Pendiente')", cbProveedor.getValue(), txtLitrosRecibidos.getText()); cargarRecepcion(); limpiarDatos(); }
    @FXML private void guardarProveedor() { ejecutarUpdate("INSERT INTO tbl_suplidores (nombre) VALUES (?)", txtNombreProv.getText()); cargarProveedores(); txtNombreProv.clear(); }
    @FXML private void actualizarRegistro() { if(idSeleccionado != -1) { ejecutarUpdate("UPDATE tbl_recepcion_leche SET proveedor=?, cantidad_litros=? WHERE id_recepcion=?", cbProveedor.getValue(), txtLitrosRecibidos.getText(), idSeleccionado); cargarRecepcion(); } }
    @FXML private void borrarRegistro() { if(idSeleccionado != -1) { ejecutarUpdate("DELETE FROM tbl_recepcion_leche WHERE id_recepcion=?", idSeleccionado); cargarRecepcion(); limpiarDatos(); } }
    @FXML private void limpiarDatos() { cbProveedor.getSelectionModel().clearSelection(); txtLitrosRecibidos.clear(); deseleccionarTablas(); }
    @FXML private void deseleccionarTablas() { tablaRecepcion.getSelectionModel().clearSelection(); idSeleccionado = -1; }

    @FXML private void actualizarProveedor() { cargarProveedores(); }
    @FXML private void borrarProveedor() { cargarProveedores(); }

    @FXML
    private void irAMenuPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void resetVistas() {
        VBox[] panes = {paneRecepcion, paneProduccion, paneCalidad, paneInventario, paneProveedores, paneSeguimiento};
        for (VBox p : panes) { if(p != null) { p.setVisible(false); p.setManaged(false); } }
    }

    @FXML private void mostrarRecepcion() { resetVistas(); paneRecepcion.setVisible(true); paneRecepcion.setManaged(true); }
    @FXML private void mostrarProveedores() { resetVistas(); paneProveedores.setVisible(true); paneProveedores.setManaged(true); }
    @FXML private void mostrarProduccion() {
        resetVistas();
        paneProduccion.setVisible(true);
        paneProduccion.setManaged(true);
        cargarLotesParaProduccion();
    }
    @FXML private void mostrarSeguimiento() { resetVistas(); paneSeguimiento.setVisible(true); paneSeguimiento.setManaged(true); }

    @FXML private void mostrarCalidad() {
        resetVistas();
        paneCalidad.setVisible(true);
        paneCalidad.setManaged(true);
        cargarLotesParaCalidad();
    }

    @FXML private void mostrarInventario() { resetVistas(); paneInventario.setVisible(true); paneInventario.setManaged(true); }

    private Connection getConnection() throws SQLException { return DriverManager.getConnection(URL, USER, PASS); }
    private void ejecutarUpdate(String sql, Object... params) { try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]); ps.executeUpdate(); } catch (Exception e) { e.printStackTrace(); } }

    public static class RecepcionLeche {
        private int id; private String proveedor, estado; private double litros;
        public RecepcionLeche(int id, String p, double l, String e) { this.id = id; this.proveedor = p; this.litros = l; this.estado = e; }
        public int getId() { return id; } public String getProveedor() { return proveedor; } public double getLitros() { return litros; } public String getEstado() { return estado; }
    }
    public static class Proveedor {
        private int id; private String nombre;
        public Proveedor(int id, String n) { this.id = id; this.nombre = n; }
        public int getId() { return id; } public String getNombre() { return nombre; }
    }
    public static class LoteProduccion {
        private String loteId, tipo, estado;
        public LoteProduccion(String i, String t, String e) { this.loteId = i; this.tipo = t; this.estado = e; }
        public String getLoteId() { return loteId; } public String getTipo() { return tipo; } public String getEstado() { return estado; }
    }
    public static class Inventario {
        private String loteId, cantidad, fecha;
        public Inventario(String l, String c, String f) { this.loteId = l; this.cantidad = c; this.fecha = f; }
        public String getLoteId() { return loteId; } public String getCantidad() { return cantidad; } public String getFecha() { return fecha; }
    }
}