package com.example.pantallas.ProcesoDeProduccion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProduccionController {

    @FXML private VBox paneRecepcion, paneProduccion, paneCalidad, paneInventario, paneProveedores, paneSeguimiento;
    @FXML private Node contentArea;
    @FXML private ComboBox<String> cbProveedor, cbResultadoCalidad, cbCambiarEstado, cbTipoQueso, cbLotesTerminados, cbLoteProduccion;
    @FXML private TextField txtLitrosRecibidos, txtNombreProv, txtLibrasFinales, txtLoteId;
    
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
    
    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;trustServerCertificate=true;encrypt=false;";
    private final String USER = "sa";
    private final String PASS = "123456";

    private final ObservableList<RecepcionLeche> listaRecepcion = FXCollections.observableArrayList();
    private final ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();
    private final ObservableList<LoteProduccion> listaLotes = FXCollections.observableArrayList();
    private final ObservableList<Inventario> listaInventario = FXCollections.observableArrayList();

    private static final double RENDIMIENTO_LECHE = 0.12;
    private static final double[] BOMQUESO = {10.0, 0.8, 0.3, 0.05};
    private static final String[] MATERIALES_BOM = {"Leche Cruda", "Sal Industrial", "Cuajo Líquido", "Fundas para Queso"};

    @FXML
    public void initialize() {
        configurarTablas();
        cargarDatosGlobales();
        cbResultadoCalidad.setItems(FXCollections.observableArrayList("Aprobado", "Rechazado"));
        cbCambiarEstado.setItems(FXCollections.observableArrayList("En Producción", "Completado"));
        cbTipoQueso.setItems(FXCollections.observableArrayList("Hoja", "Freir", "Crema", "Mozzarella", "Cheddar"));
        
        tablaRecepcion.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                idSeleccionado = newVal.getId();
                cbProveedor.setValue(newVal.getProveedor());
                txtLitrosRecibidos.setText(String.valueOf(newVal.getLitros()));
            }
        });
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
        cargarRecepcionLeche();
        cargarProveedores();
        cargarLotesSeguimiento();
        cargarInventario();
    }

    private void cargarRecepcionLeche() {
        listaRecepcion.clear();
        String sql = "SELECT id_recepcion, proveedor, cantidad_litros, estado FROM tbl_recepcion_leche ORDER BY id_recepcion DESC";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                listaRecepcion.add(new RecepcionLeche(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getString(4)));
            }
            tablaRecepcion.setItems(listaRecepcion);
            
        } catch (Exception e) {
            listaRecepcion.add(new RecepcionLeche(1, "Granja SantaRosa", 250.0, "Pendiente"));
            listaRecepcion.add(new RecepcionLeche(2, "Lácteos del Yaque", 180.0, "Recibido"));
            tablaRecepcion.setItems(listaRecepcion);
        }
    }

    private void cargarProveedores() {
        listaProveedores.clear();
        cbProveedor.getItems().clear();
        
        String sql = "SELECT id_suplidor, nombre FROM tbl_suplidores ORDER BY nombre";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                listaProveedores.add(new Proveedor(rs.getInt(1), rs.getString("nombre")));
                cbProveedor.getItems().add(rs.getString("nombre"));
            }
            tablaProveedores.setItems(listaProveedores);
            
        } catch (Exception e) {
            listaProveedores.add(new Proveedor(1, "Granja SantaRosa"));
            listaProveedores.add(new Proveedor(2, "Lácteos del Yaque"));
            cbProveedor.getItems().addAll("Granja SantaRosa", "Lácteos del Yaque");
            tablaProveedores.setItems(listaProveedores);
        }
    }

    private void cargarLotesSeguimiento() {
        listaLotes.clear();
        cbLotesTerminados.getItems().clear();
        cbLoteProduccion.getItems().clear();
        
        String sql = "SELECT lote_id, tipo_queso, estado FROM tbl_produccion ORDER BY lote_id DESC";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                String lid = rs.getString("lote_id");
                String est = rs.getString("estado");
                listaLotes.add(new LoteProduccion(lid, rs.getString("tipo_queso"), est));
                
                if ("Completado".equalsIgnoreCase(est)) {
                    cbLotesTerminados.getItems().add(lid);
                }
                cbLoteProduccion.getItems().add(lid);
            }
            tablaSeguimiento.setItems(listaLotes);
            
        } catch (Exception e) {
            listaLotes.add(new LoteProduccion("Lote-A-2026-001", "Crema", "En Producción"));
            listaLotes.add(new LoteProduccion("Lote-A-2026-002", "Freir", "Completado"));
            cbLotesTerminados.getItems().add("Lote-A-2026-002");
            cbLoteProduccion.getItems().addAll("Lote-A-2026-001", "Lote-A-2026-002");
            tablaSeguimiento.setItems(listaLotes);
        }
    }

    private void cargarInventario() {
        listaInventario.clear();
        String sql = "SELECT lote_id, cantidad_disponible, fecha_entrada FROM tbl_inventario_productos";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                listaInventario.add(new Inventario(rs.getString("lote_id"), rs.getString("cantidad_disponible"), rs.getString("fecha_entrada")));
            }
            tablaInventario.setItems(listaInventario);
            
        } catch (Exception e) {
            listaInventario.add(new Inventario("Lote-A-2026-001", "25.5", "2026-04-20"));
            tablaInventario.setItems(listaInventario);
        }
    }

    private void cargarLotesParaCalidad() {
        cbLotesTerminados.getItems().clear();
        String sql = "SELECT lote_id FROM tbl_produccion WHERE estado = 'Completado'";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                cbLotesTerminados.getItems().add(rs.getString("lote_id"));
            }
        } catch (Exception e) {
            cbLotesTerminados.getItems().addAll("Lote-A-2026-001", "Lote-A-2026-002");
        }
    }

    private void cargarLotesParaProduccion() {
        cbLoteProduccion.getItems().clear();
        String sql = "SELECT lote_id FROM tbl_produccion";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                cbLoteProduccion.getItems().add(rs.getString("lote_id"));
            }
        } catch (Exception e) {
            cbLoteProduccion.getItems().addAll("Lote-A-2026-001", "Lote-A-2026-002");
        }
    }

    @FXML
    private void registrarEntrada() {
        if (cbProveedor.getValue() == null || txtLitrosRecibidos.getText().isEmpty()) {
            mostrarAlerta("Campos Vacíos", "Debe seleccionar un proveedor e ingresar la cantidad de litros.");
            return;
        }

        double litros = parsearNumero(txtLitrosRecibidos.getText());
        if (litros <= 0) {
            mostrarAlerta("Cantidad Inválida", "Debe ingresar una cantidad mayor a 0.");
            return;
        }

        String sql = "INSERT INTO tbl_recepcion_leche (proveedor, cantidad_litros, estado, fecha_entrada) VALUES (?, ?, 'Pendiente', GETDATE())";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, cbProveedor.getValue());
            ps.setDouble(2, litros);
            ps.executeUpdate();
            
            cargarRecepcionLeche();
            limpiarCamposRecepcion();
            mostrarAlerta("Éxito", "Entrada de leche registrada.");
            
        } catch (Exception e) {
            listaRecepcion.add(0, new RecepcionLeche(listaRecepcion.size() + 1, cbProveedor.getValue(), litros, "Pendiente"));
            cargarRecepcionLeche();
            limpiarCamposRecepcion();
            mostrarAlerta("Éxito", "Entrada de leche registrada (modo demo).");
        }
    }

    @FXML
    private void iniciarProduccion() {
        if (cbLoteProduccion.getValue() == null || cbTipoQueso.getValue() == null) {
            mostrarAlerta("Campos Vacíos", "Debe seleccionar un lote y el tipo de queso a producir.");
            return;
        }

        String loteId = cbLoteProduccion.getValue();
        String tipoQueso = cbTipoQueso.getValue();
        
        double rendimientoEstimado = 0.0;
        for (int i = 0; i < MATERIALES_BOM.length; i++) {
            if (MATERIALES_BOM[i].equals("Leche Cruda")) {
                if (!verificarStockSuficiente("Leche Cruda", BOMQUESO[i])) {
                    mostrarAlerta("Stock Insuficiente", "No hay suficiente Leche Cruda para iniciar la producción.");
                    return;
                }
            }
        }
        
        String sqlUpdate = "UPDATE tbl_produccion SET tipo_queso = ?, estado = 'En Producción', fecha_inicio = GETDATE() WHERE lote_id = ?";
        String sqlInsert = "INSERT INTO tbl_produccion (lote_id, tipo_queso, estado, fecha_inicio) VALUES (?, ?, 'En Producción', GETDATE())";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement psCheck = con.prepareStatement("SELECT COUNT(*) FROM tbl_produccion WHERE lote_id = ?")) {
            
            psCheck.setString(1, loteId);
            ResultSet rs = psCheck.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                    ps.setString(1, tipoQueso);
                    ps.setString(2, loteId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                    ps.setString(1, loteId);
                    ps.setString(2, tipoQueso);
                    ps.executeUpdate();
                }
            }
            
            descontarMaterialesStock();
            cargarLotesSeguimiento();
            mostrarAlerta("Producción Iniciada", "Orden de producción iniciada. Materiales descontados del inventario.");
            
        } catch (Exception e) {
            listaLotes.add(0, new LoteProduccion(loteId, tipoQueso, "En Producción"));
            cargarLotesSeguimiento();
            descontarMaterialesStockDemo();
            mostrarAlerta("Producción Iniciada", "Orden de producción iniciada (modo demo).");
        }
    }

    private boolean verificarStockSuficiente(String producto, double cantidadRequerida) {
        String sql = "SELECT cantidad_stock FROM tbl_inventario_productos WHERE nombre_producto = ?";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, producto);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("cantidad_stock") >= cantidadRequerida;
            }
            
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    private void descontarMaterialesStock() {
        String sql = "INSERT INTO tbl_movimientos_inventario (producto, tipo, cantidad, unidad, fecha_movimiento) VALUES (?, 'SALIDA', ?, ?, GETDATE())";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            for (int i = 0; i < MATERIALES_BOM.length; i++) {
                ps.setString(1, MATERIALES_BOM[i]);
                ps.setDouble(2, BOMQUESO[i]);
                ps.setString(3, obtenerUnidadMaterial(MATERIALES_BOM[i]));
                ps.addBatch();
            }
            ps.executeBatch();
            
        } catch (Exception e) {
            // Silent fail in demo mode
        }
    }

    private void descontarMaterialesStockDemo() {
        // Silently log in demo mode
    }

    private String obtenerUnidadMaterial(String material) {
        switch (material) {
            case "Leche Cruda": return "Litros (L)";
            case "Sal Industrial": return "Kilos (Kg)";
            case "Cuajo Líquido": return "Litros (L)";
            case "Fundas para Queso": return "Unidades (Und)";
            default: return "Unidades";
        }
    }

    @FXML
    private void aprobarLote() {
        if (cbLotesTerminados.getValue() == null) {
            mostrarAlerta("Selección Requerida", "Debe seleccionar un lote para aprobar.");
            return;
        }

        if (txtLibrasFinales.getText().isEmpty() || cbResultadoCalidad.getValue() == null) {
            mostrarAlerta("Campos Vacíos", "Debe ingresar las libras finales y el resultado de calidad.");
            return;
        }

        double libras = parsearNumero(txtLibrasFinales.getText());
        String loteId = cbLotesTerminados.getValue();
        String resultado = cbResultadoCalidad.getValue();

        if (resultado.equals("Aprobado")) {
            String sqlInv = "INSERT INTO tbl_inventario_productos (lote_id, cantidad_disponible, fecha_entrada, tipo_queso) VALUES (?, ?, GETDATE(), ?)";
            
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sqlInv)) {
                
                ps.setString(1, loteId);
                ps.setDouble(2, libras);
                ps.setString(3, obtenerTipoQueso(loteId));
                ps.executeUpdate();
                
                String sqlProd = "UPDATE tbl_produccion SET estado = 'Finalizado' WHERE lote_id = ?";
                try (PreparedStatement ps2 = con.prepareStatement(sqlProd)) {
                    ps2.setString(1, loteId);
                    ps2.executeUpdate();
                }
                
            } catch (Exception e) {
                listaInventario.add(0, new Inventario(loteId, String.valueOf(libras), LocalDate.now().toString()));
            }
            
            cargarInventario();
            cargarLotesSeguimiento();
            txtLibrasFinales.clear();
            mostrarAlerta("Lote Aprobado", "El lote " + loteId + " ha sido movido al inventario.");
            
        } else {
            String sql = "UPDATE tbl_produccion SET estado = 'Rechazado' WHERE lote_id = ?";
            
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, loteId);
                ps.executeUpdate();
            } catch (Exception e) {}
            
            cargarLotesSeguimiento();
            txtLibrasFinales.clear();
            mostrarAlerta("Lote Rechazado", "El lote " + loteId + " ha sido marcado como RECHAZADO.");
        }
    }

    private String obtenerTipoQueso(String loteId) {
        for (LoteProduccion lote : listaLotes) {
            if (lote.getLoteId().equals(loteId)) {
                return lote.getTipo();
            }
        }
        return "Sin especificar";
    }

    @FXML
    private void guardarProveedor() {
        if (txtNombreProv.getText() == null || txtNombreProv.getText().trim().isEmpty()) {
            mostrarAlerta("Campo Vacío", "Debe ingresar el nombre del proveedor.");
            return;
        }

        String sql = "INSERT INTO tbl_suplidores (nombre) VALUES (?)";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, txtNombreProv.getText().trim());
            ps.executeUpdate();
            
            cargarProveedores();
            txtNombreProv.clear();
            mostrarAlerta("Éxito", "Proveedor registrado correctamente.");
            
        } catch (Exception e) {
            listaProveedores.add(0, new Proveedor(listaProveedores.size() + 1, txtNombreProv.getText()));
            cargarProveedores();
            txtNombreProv.clear();
            mostrarAlerta("Éxito", "Proveedor registrado (modo demo).");
        }
    }

    @FXML
    private void eliminarProveedor() {
        Proveedor seleccionado = (Proveedor) tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selección Requerida", "Debe seleccionar un proveedor de la tabla.");
            return;
        }

        String sql = "DELETE FROM tbl_suplidores WHERE id_suplidor = ?";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, seleccionado.getId());
            ps.executeUpdate();
            
            cargarProveedores();
            mostrarAlerta("Eliminado", "Proveedor eliminado.");
            
        } catch (Exception e) {
            listaProveedores.remove(seleccionado);
            cargarProveedores();
            mostrarAlerta("Eliminado", "Proveedor eliminado (modo demo).");
        }
    }

    @FXML
    private void limpiarCamposRecepcion() {
        cbProveedor.getSelectionModel().clearSelection();
        txtLitrosRecibidos.clear();
        idSeleccionado = -1;
    }

    private void resetVistas() {
        VBox[] panes = {paneRecepcion, paneProduccion, paneCalidad, paneInventario, paneProveedores, paneSeguimiento};
        for (VBox p : panes) {
            if (p != null) {
                p.setVisible(false);
                p.setManaged(false);
            }
        }
    }

    @FXML private Button btnNavRecepcion, btnNavProveedores, btnNavProduccion, btnNavSeguimiento, btnNavCalidad, btnNavInventario;

    @FXML private void mostrarRecepcion() { alternarVista(paneRecepcion, btnNavRecepcion); }
    @FXML private void mostrarProveedores() { alternarVista(paneProveedores, btnNavProveedores); }
    @FXML private void mostrarProduccion() { alternarVista(paneProduccion, btnNavProduccion); cargarLotesParaProduccion(); }
    @FXML private void mostrarCalidad() { alternarVista(paneCalidad, btnNavCalidad); cargarLotesParaCalidad(); }
    @FXML private void mostrarSeguimiento() { alternarVista(paneSeguimiento, btnNavSeguimiento); }
    @FXML private void mostrarInventario() { alternarVista(paneInventario, btnNavInventario); }

    private void alternarVista(VBox pane, Button btn) {
        resetVistas();
        if (pane != null) {
            pane.setVisible(true);
            pane.setManaged(true);
        }
        if (btn != null) {
            Button[] btns = {btnNavRecepcion, btnNavProveedores, btnNavProduccion, btnNavSeguimiento, btnNavCalidad, btnNavInventario};
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
    private void actualizarEstadoLote() {
        LoteProduccion seleccionado = tablaSeguimiento.getSelectionModel().getSelectedItem();
        String nuevoEstado = cbCambiarEstado.getValue();

        if (seleccionado == null) {
            mostrarAlerta("Selección Requerida", "Debe seleccionar un lote de la tabla de seguimiento.");
            return;
        }
        if (nuevoEstado == null) {
            mostrarAlerta("Estado Requerido", "Debe seleccionar un nuevo estado.");
            return;
        }

        String sql = "UPDATE tbl_produccion SET estado = ? WHERE lote_id = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setString(2, seleccionado.getLoteId());
            ps.executeUpdate();

            cargarLotesSeguimiento();
            mostrarAlerta("Estado Actualizado", "El lote " + seleccionado.getLoteId() + " ahora está: " + nuevoEstado + ".");

        } catch (Exception e) {
            for (LoteProduccion lote : listaLotes) {
                if (lote.getLoteId().equals(seleccionado.getLoteId())) {
                    lote.setEstado(nuevoEstado);
                    break;
                }
            }
            cargarLotesSeguimiento();
            mostrarAlerta("Estado Actualizado", "El lote " + seleccionado.getLoteId() + " ahora está: " + nuevoEstado + " (modo demo).");
        }
    }

    @FXML
    private void irAMenuPrincipal() {
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

    private double parsearNumero(String texto) {
        try {
            return Double.parseDouble(texto.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static class RecepcionLeche {
        private int id;
        private String proveedor, estado;
        private double litros;

        public RecepcionLeche(int id, String proveedor, double litros, String estado) {
            this.id = id;
            this.proveedor = proveedor;
            this.litros = litros;
            this.estado = estado;
        }

        public int getId() { return id; }
        public String getProveedor() { return proveedor; }
        public double getLitros() { return litros; }
        public String getEstado() { return estado; }
    }

    public static class Proveedor {
        private int id;
        private String nombre;

        public Proveedor(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
    }

    public static class LoteProduccion {
        private String loteId, tipo, estado;

        public LoteProduccion(String loteId, String tipo, String estado) {
            this.loteId = loteId;
            this.tipo = tipo;
            this.estado = estado;
        }

        public String getLoteId() { return loteId; }
        public String getTipo() { return tipo; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }

    public static class Inventario {
        private String loteId, cantidad, fecha;

        public Inventario(String loteId, String cantidad, String fecha) {
            this.loteId = loteId;
            this.cantidad = cantidad;
            this.fecha = fecha;
        }

        public String getLoteId() { return loteId; }
        public String getCantidad() { return cantidad; }
        public String getFecha() { return fecha; }
    }
}