package com.example.pantallas.ProcesoDeCompras;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class CompraController {

    @FXML private VBox paneNuevaOrden, paneRecepcion, paneHistorial;
    @FXML private ComboBox<String> cbSuplidor, cbInsumo, cbOrdenesPendientes, cbNuevoEstado, cbUnidad;
    @FXML private TextField txtCantidad, txtPrecio, txtNoDocumento, txtCantidadRecibida;
    @FXML private DatePicker dpFechaEntrega;
    @FXML private TextArea txtNotasRecepcion;
    @FXML private TableView<OrdenCompra> tablaOrdenesPendientes, tablaHistorial;

    private int idSeleccionado = -1;

    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;encrypt=true;trustServerCertificate=true;";
    private final String USER = "janciel";
    private final String PASS = "123456789";

    @FXML
    public void initialize() {
        configurarTablas();
        cargarCombos();
        cargarDatosDesdeSQL();
    }

    private void resetVistas() {
        paneNuevaOrden.setVisible(false); paneNuevaOrden.setManaged(false);
        paneRecepcion.setVisible(false); paneRecepcion.setManaged(false);
        paneHistorial.setVisible(false); paneHistorial.setManaged(false);
    }

    @FXML private void mostrarNuevaOrden() { resetVistas(); paneNuevaOrden.setVisible(true); paneNuevaOrden.setManaged(true); }
    @FXML private void mostrarRecepcion() { resetVistas(); paneRecepcion.setVisible(true); paneRecepcion.setManaged(true); }
    @FXML private void mostrarHistorial() { resetVistas(); paneHistorial.setVisible(true); paneHistorial.setManaged(true); }

    @FXML
    private void irAMenuPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) paneNuevaOrden.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo cargar el Menú Principal.");
        }
    }

    @FXML
    private void generarOrden() {
        if (cbSuplidor.getValue() == null || txtCantidad.getText().isEmpty() || cbUnidad.getValue() == null) {
            mostrarAlerta("Campos Vacíos", "Por favor completa los datos básicos y la unidad.");
            return;
        }

        String sql = "INSERT INTO tbl_ordenes_compra (suplidor, insumo, cantidad, precio_unitario, fecha_pedido, fecha_entrega_esperada, estado) VALUES (?, ?, ?, ?, GETDATE(), ?, 'Pendiente')";
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Limpieza de cadena para evitar NumberFormatException
            String valorLimpio = txtCantidad.getText().replaceAll("[^0-9.]", "");
            double cantidadNum = Double.parseDouble(valorLimpio);

            ps.setString(1, cbSuplidor.getValue());
            // Concatenamos el insumo con su unidad para que se guarde en la BD
            ps.setString(2, cbInsumo.getValue() + " (" + cbUnidad.getValue() + ")");
            ps.setDouble(3, cantidadNum);
            ps.setDouble(4, Double.parseDouble(txtPrecio.getText().replaceAll("[^0-9.]", "")));
            ps.setDate(5, Date.valueOf(dpFechaEntrega.getValue()));

            ps.executeUpdate();
            cargarDatosDesdeSQL();
            limpiarDatosOrden();
            mostrarAlerta("Éxito", "Orden de compra generada correctamente.");
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato", "Ingresa solo números en la cantidad y precio.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void confirmarRecepcion() {
        String seleccion = cbOrdenesPendientes.getValue();
        if (seleccion == null) return;

        int idOrden = Integer.parseInt(seleccion.split(" - ")[0]);

        String sql = "UPDATE tbl_ordenes_compra SET estado = 'Recibido', notas = ?, cantidad_recibida = ?, no_documento = ?, fecha_recepcion = GETDATE() WHERE id_orden = ?";
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtNotasRecepcion.getText());
            ps.setDouble(2, Double.parseDouble(txtCantidadRecibida.getText().replaceAll("[^0-9.]", "")));
            ps.setString(3, txtNoDocumento.getText());
            ps.setInt(4, idOrden);

            ps.executeUpdate();
            cargarDatosDesdeSQL();
            mostrarAlerta("Almacén", "Mercancía ingresada al sistema.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void actualizarEstadoMasivo() {
        OrdenCompra seleccionada = tablaHistorial.getSelectionModel().getSelectedItem();
        if (seleccionada != null && cbNuevoEstado.getValue() != null) {
            String sql = "UPDATE tbl_ordenes_compra SET estado = ? WHERE id_orden = ?";
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, cbNuevoEstado.getValue());
                ps.setInt(2, seleccionada.getId());
                ps.executeUpdate();
                cargarDatosDesdeSQL();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void cargarDatosDesdeSQL() {
        ObservableList<OrdenCompra> listaTotal = FXCollections.observableArrayList();
        ObservableList<String> pendientesParaCombo = FXCollections.observableArrayList();

        String query = "SELECT * FROM tbl_ordenes_compra ORDER BY id_orden DESC";
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                OrdenCompra oc = new OrdenCompra(
                        rs.getInt("id_orden"), rs.getString("suplidor"),
                        rs.getString("insumo"), rs.getDouble("cantidad"),
                        rs.getString("estado"), rs.getDouble("precio_unitario")
                );
                listaTotal.add(oc);

                if (oc.getEstado().equals("Pendiente")) {
                    pendientesParaCombo.add(oc.getId() + " - " + oc.getSuplidor() + " (" + oc.getInsumo() + ")");
                }
            }
            tablaHistorial.setItems(listaTotal);
            tablaOrdenesPendientes.setItems(listaTotal);
            cbOrdenesPendientes.setItems(pendientesParaCombo);

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void configurarTablas() {
        TableColumn<OrdenCompra, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<OrdenCompra, String> colSup = new TableColumn<>("Suplidor");
        colSup.setCellValueFactory(new PropertyValueFactory<>("suplidor"));
        TableColumn<OrdenCompra, String> colIns = new TableColumn<>("Insumo");
        colIns.setCellValueFactory(new PropertyValueFactory<>("insumo"));
        TableColumn<OrdenCompra, Double> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<OrdenCompra, Double> colPre = new TableColumn<>("Precio U.");
        colPre.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        TableColumn<OrdenCompra, String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablaHistorial.getColumns().setAll(colId, colSup, colIns, colCant, colPre, colEst);
        tablaOrdenesPendientes.getColumns().setAll(colId, colSup, colIns, colCant, colPre, colEst);
    }

    private void cargarCombos() {
        cbSuplidor.setItems(FXCollections.observableArrayList("Lácteos del Yaque", "Insumos RD", "Empaques Cibao"));
        cbInsumo.setItems(FXCollections.observableArrayList("Cuajo Líquido", "Sal Industrial", "Fundas para Queso", "Cloro"));
        cbNuevoEstado.setItems(FXCollections.observableArrayList("Pendiente", "Recibido", "Cancelado", "En Tránsito"));
        cbUnidad.setItems(FXCollections.observableArrayList("Libras", "Kilos", "Litros", "Galones", "Unidades"));
    }

    @FXML private void limpiarDatosOrden() {
        txtCantidad.clear(); txtPrecio.clear();
        cbSuplidor.getSelectionModel().clearSelection();
        cbUnidad.getSelectionModel().clearSelection();
        dpFechaEntrega.setValue(null);
    }

    @FXML
    private void borrarRegistroHistorial() {
        OrdenCompra seleccionada = tablaHistorial.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;

        String sql = "DELETE FROM tbl_ordenes_compra WHERE id_orden = ?";
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, seleccionada.getId());
            ps.executeUpdate();
            cargarDatosDesdeSQL();
            mostrarAlerta("Eliminado", "Registro borrado correctamente.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    public static class OrdenCompra {
        private int id; private String suplidor, insumo, estado; private double cantidad, precioUnitario;
        public OrdenCompra(int id, String s, String i, double c, String e, double p) {
            this.id = id; this.suplidor = s; this.insumo = i; this.cantidad = c; this.estado = e; this.precioUnitario = p;
        }
        public int getId() { return id; }
        public String getSuplidor() { return suplidor; }
        public String getInsumo() { return insumo; }
        public double getCantidad() { return cantidad; }
        public String getEstado() { return estado; }
        public double getPrecioUnitario() { return precioUnitario; }
    }
}