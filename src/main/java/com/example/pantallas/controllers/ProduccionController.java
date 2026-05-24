package com.example.pantallas.controllers;

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
import java.util.HashMap;
import java.util.Map;

public class ProduccionController {

    @FXML private VBox paneProduccion, paneCalidad, paneInventario, paneSeguimiento;
    @FXML private Node contentArea;
    @FXML private ComboBox<String> cbResultadoCalidad, cbCambiarEstado, cbTipoQueso, cbLotesTerminados;
    @FXML private TextField txtCantidadProyectada, txtLoteId;
    @FXML private TextArea txtObservaciones;
    @FXML private CheckBox chkTextura, chkSabor, chkHumedad, chkColor;
    @FXML private Label lblRecetaDetalle;

    @FXML private TableView<LoteProduccion> tablaSeguimiento;
    @FXML private TableView<Inventario> tablaInventario;

    @FXML private TableColumn<LoteProduccion, String> colSegLote, colSegTipo, colSegEstado;
    @FXML private TableColumn<Inventario, String> colInvLote, colInvCant, colInvFecha;

    private Map<String, Double> recetaActual = new HashMap<>();
    private Map<String, String> recetaUnidades = new HashMap<>();
    private Map<String, Double> lotesCantidades = new HashMap<>(); // Almacenar cantidades proyectadas

    private final ObservableList<LoteProduccion> listaLotes = FXCollections.observableArrayList();
    private final ObservableList<Inventario> listaInventario = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTablas();
        cargarDatosGlobales();
        cbResultadoCalidad.setItems(FXCollections.observableArrayList("Aprobado", "Rechazado"));
        cbCambiarEstado.setItems(FXCollections.observableArrayList("En Producción", "Completado"));
        cargarTiposQueso();

        cbTipoQueso.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) cargarReceta(newVal);
        });

        if (txtCantidadProyectada != null) {
            txtCantidadProyectada.textProperty().addListener((obs, oldVal, newVal) -> {
                if (cbTipoQueso.getValue() != null) cargarReceta(cbTipoQueso.getValue());
            });
        }
    }

    private void configurarTablas() {
        colSegLote.setCellValueFactory(new PropertyValueFactory<>("loteId"));
        colSegTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colSegEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colInvLote.setCellValueFactory(new PropertyValueFactory<>("loteId"));
        colInvCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colInvFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
    }

    private void cargarDatosGlobales() {
        cargarLotesSeguimiento();
        cargarInventario();
    }

    private void cargarTiposQueso() {
        ObservableList<String> tipos = FXCollections.observableArrayList();
        String sql = "SELECT nombre_producto FROM Productos WHERE categoria = 'Quesos' AND activo = 1 ORDER BY nombre_producto";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String nombre = rs.getString(1);
                if (nombre.startsWith("Queso ")) {
                    nombre = nombre.substring(6);
                }
                if (!tipos.contains(nombre)) {
                    tipos.add(nombre);
                }
            }
        } catch (Exception e) {
            // Continuar con los valores por defecto si falla
        }
        
        String[] todasLasVariedades = {
            "Hoja", "Freir", "Crema", "Mozzarella", "Cheddar", 
            "Blanco", "Amarillo", "Parmesano", "Gouda", "Camembert", "de Bola", "Ricotta"
        };
        for (String v : todasLasVariedades) {
            if (!tipos.contains(v)) {
                tipos.add(v);
            }
        }
        FXCollections.sort(tipos);
        cbTipoQueso.setItems(tipos);
    }

    private void cargarReceta(String tipoQueso) {
        recetaActual.clear();
        recetaUnidades.clear();
        String sql = "SELECT ingrediente, cantidad, unidad FROM tbl_recetas WHERE producto_destino = ? AND activo = 1 ORDER BY id_receta";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipoQueso);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                recetaActual.put(rs.getString("ingrediente"), rs.getDouble("cantidad"));
                recetaUnidades.put(rs.getString("ingrediente"), rs.getString("unidad"));
            }
        } catch (Exception e) { }

        if (recetaActual.isEmpty()) {
            recetaActual.put("Leche Cruda", 10.0);
            recetaUnidades.put("Leche Cruda", "Litros (L)");
            recetaActual.put("Sal Industrial", 0.8);
            recetaUnidades.put("Sal Industrial", "Kilos (Kg)");
            recetaActual.put("Cuajo Líquido", 0.3);
            recetaUnidades.put("Cuajo Líquido", "Litros (L)");
        }

        double multiplicador = parsearNumero(txtCantidadProyectada != null && !txtCantidadProyectada.getText().isEmpty() ? txtCantidadProyectada.getText() : "1");
        if (multiplicador <= 0) multiplicador = 1;

        if (lblRecetaDetalle != null) {
            StringBuilder sb = new StringBuilder("📋 Receta total para " + multiplicador + " de ");
            sb.append(tipoQueso).append(":\n");
            int i = 0;
            for (Map.Entry<String, Double> e : recetaActual.entrySet()) {
                if (i++ > 0) sb.append(" | ");
                double cantidadTotal = e.getValue() * multiplicador;
                sb.append(e.getKey()).append(": ").append(String.format("%.2f", cantidadTotal)).append(" ").append(recetaUnidades.get(e.getKey()));
            }
            lblRecetaDetalle.setText(sb.toString());
        }
    }

    private void cargarLotesSeguimiento() {
        listaLotes.clear();
        if(cbLotesTerminados != null) cbLotesTerminados.getItems().clear();

        String sql = "SELECT lote_id, tipo_queso, estado FROM tbl_produccion ORDER BY lote_id DESC";

        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String lid = rs.getString("lote_id");
                String tq = rs.getString("tipo_queso");
                String est = rs.getString("estado");
                listaLotes.add(new LoteProduccion(lid, tq, est));

                if ("Completado".equalsIgnoreCase(est) && cbLotesTerminados != null) {
                    cbLotesTerminados.getItems().add(lid + " - " + tq);
                }
            }
            tablaSeguimiento.setItems(listaLotes);

        } catch (Exception e) {
            listaLotes.add(new LoteProduccion("Lote-A-2026-001", "Crema", "En Producción"));
            listaLotes.add(new LoteProduccion("Lote-A-2026-002", "Freir", "Completado"));
            if(cbLotesTerminados != null) cbLotesTerminados.getItems().add("Lote-A-2026-002 - Freir");
            tablaSeguimiento.setItems(listaLotes);
        }
    }

    private void cargarInventario() {
        listaInventario.clear();
        String sql = "SELECT lote_id, cantidad_disponible, fecha_entrada FROM tbl_inventario_productos";

        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
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
        if(cbLotesTerminados != null) cbLotesTerminados.getItems().clear();
        String sql = "SELECT lote_id, tipo_queso FROM tbl_produccion WHERE estado = 'Completado'";

        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                if(cbLotesTerminados != null) cbLotesTerminados.getItems().add(rs.getString("lote_id") + " - " + rs.getString("tipo_queso"));
            }
        } catch (Exception e) {
            if(cbLotesTerminados != null) cbLotesTerminados.getItems().addAll("Lote-A-2026-001 - Crema", "Lote-A-2026-002 - Freir");
        }
    }

    @FXML
    private void iniciarProduccion() {
        if (txtCantidadProyectada.getText() == null || txtCantidadProyectada.getText().isEmpty() || cbTipoQueso.getValue() == null) {
            mostrarAlerta("Campos Vacíos", "Debe ingresar la cantidad proyectada y seleccionar el tipo de queso a producir.");
            return;
        }
        
        double cantidadProyectada = parsearNumero(txtCantidadProyectada.getText());
        if (cantidadProyectada <= 0) {
            mostrarAlerta("Cantidad Inválida", "Debe ingresar una cantidad mayor a 0.");
            return;
        }

        if (recetaActual.isEmpty()) {
            mostrarAlerta("Receta Vacía", "No se encontró receta para este tipo de queso.");
            return;
        }

        String tipoQueso = cbTipoQueso.getValue();
        // Generar un ID de lote automático
        String loteId = "LOTE-" + tipoQueso.toUpperCase().replaceAll("\\s+","").substring(0, Math.min(3, tipoQueso.length())) + "-" + System.currentTimeMillis() % 10000;

        for (Map.Entry<String, Double> ing : recetaActual.entrySet()) {
            double cantidadRequerida = ing.getValue() * cantidadProyectada;
            if (!verificarStockSuficiente(ing.getKey(), cantidadRequerida)) {
                mostrarAlerta("Stock Insuficiente",
                    "No hay suficiente " + ing.getKey() + " (" + cantidadRequerida + " " + recetaUnidades.get(ing.getKey()) + ") en inventario.");
                return;
            }
        }

        String sqlInsert = "INSERT INTO tbl_produccion (lote_id, tipo_queso, estado, fecha_inicio) VALUES (?, ?, 'En Producción', GETDATE())";

        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlInsert)) {

            ps.setString(1, loteId);
            ps.setString(2, tipoQueso);
            ps.executeUpdate();

            descontarMaterialesStock(tipoQueso, cantidadProyectada);
            lotesCantidades.put(loteId, cantidadProyectada);
            cargarLotesSeguimiento();
            mostrarAlerta("Producción Iniciada", "Orden de producción iniciada. Lote generado: " + loteId + ". Materiales descontados.");

        } catch (Exception e) {
            listaLotes.add(0, new LoteProduccion(loteId, tipoQueso, "En Producción"));
            lotesCantidades.put(loteId, cantidadProyectada);
            cargarLotesSeguimiento();
            descontarMaterialesStockDemo();
            mostrarAlerta("Producción Iniciada", "Orden de producción iniciada (modo demo). Lote: " + loteId);
        }
    }

    private boolean verificarStockSuficiente(String producto, double cantidadRequerida) {
        String sql = "SELECT stock_actual FROM Productos WHERE nombre_producto = ?";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("stock_actual") >= cantidadRequerida;
        } catch (Exception e) { }

        String sql2 = "SELECT cantidad_stock FROM tbl_inventario_productos WHERE nombre_producto = ?";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql2)) {
            ps.setString(1, producto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("cantidad_stock") >= cantidadRequerida;
        } catch (Exception e) { }

        return true;
    }

    private void descontarMaterialesStock(String tipoQueso, double cantidadProyectada) {
        for (Map.Entry<String, Double> ing : recetaActual.entrySet()) {
            String ingrediente = ing.getKey();
            double cantidad = ing.getValue() * cantidadProyectada;
            String unidad = recetaUnidades.get(ingrediente);

            String sqlMov = "INSERT INTO tbl_movimientos_inventario (producto, tipo, cantidad, unidad, fecha_movimiento, justificacion) VALUES (?, 'SALIDA', ?, ?, GETDATE(), ?)";
            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sqlMov)) {
                ps.setString(1, ingrediente);
                ps.setDouble(2, cantidad);
                ps.setString(3, unidad);
                ps.setString(4, "Producción de " + tipoQueso);
                ps.executeUpdate();
            } catch (Exception e) { }

            actualizarStockIngrediente(ingrediente, cantidad);
        }
    }

    private void actualizarStockIngrediente(String ingrediente, double cantidad) {
        String[][] updates = {
            {"Productos", "stock_actual", "nombre_producto"},
            {"tbl_inventario_productos", "cantidad_stock", "nombre_producto"}
        };
        for (String[] u : updates) {
            String sql = "UPDATE " + u[0] + " SET " + u[1] + " = " + u[1] + " - ? WHERE " + u[2] + " = ?";
            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, cantidad);
                ps.setString(2, ingrediente);
                int rows = ps.executeUpdate();
                if (rows > 0) break;
            } catch (Exception e) { }
        }
    }

    private void descontarMaterialesStockDemo() {
    }

    @FXML
    private void aprobarLote() {
        if (cbLotesTerminados.getValue() == null) {
            mostrarAlerta("Selección Requerida", "Debe seleccionar un lote para aprobar.");
            return;
        }

        if (cbResultadoCalidad.getValue() == null) {
            mostrarAlerta("Campos Vacíos", "Debe seleccionar el resultado de calidad.");
            return;
        }

        // Extraer lote_id del texto del ComboBox ("LOTE-XXX - Cheddar")
        String seleccion = cbLotesTerminados.getValue();
        String loteId = seleccion.contains(" - ") ? seleccion.split(" - ")[0] : seleccion;
        String resultado = cbResultadoCalidad.getValue();
        String observaciones = txtObservaciones != null ? txtObservaciones.getText() : "";
        
        System.out.println("Criterios evaluados:");
        if (chkTextura != null && chkTextura.isSelected()) System.out.println("- Textura Correcta");
        if (chkSabor != null && chkSabor.isSelected()) System.out.println("- Sabor/Aroma");
        if (chkHumedad != null && chkHumedad.isSelected()) System.out.println("- Humedad");
        if (chkColor != null && chkColor.isSelected()) System.out.println("- Color");
        System.out.println("Observaciones: " + observaciones);

        if (resultado.equals("Aprobado")) {
            String tipoQueso = obtenerTipoQueso(loteId);
            double libras = lotesCantidades.getOrDefault(loteId, 100.0); // Valor por defecto si no se guardó

            String sqlInv = "INSERT INTO tbl_inventario_productos (lote_id, cantidad_disponible, fecha_entrada, tipo_queso) VALUES (?, ?, GETDATE(), ?)";

            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sqlInv)) {

                ps.setString(1, loteId);
                ps.setDouble(2, libras);
                ps.setString(3, tipoQueso);
                ps.executeUpdate();

                String sqlProd = "UPDATE tbl_produccion SET estado = 'Finalizado' WHERE lote_id = ?";
                try (PreparedStatement ps2 = con.prepareStatement(sqlProd)) {
                    ps2.setString(1, loteId);
                    ps2.executeUpdate();
                }

                if (tipoQueso != null && !tipoQueso.isEmpty() && !tipoQueso.equals("Sin especificar")) {
                    actualizarStockQueso(tipoQueso, libras);
                }

            } catch (Exception e) {
                listaInventario.add(0, new Inventario(loteId, String.valueOf(libras), LocalDate.now().toString()));
            }

            cargarInventario();
            cargarLotesSeguimiento();
            if (txtObservaciones != null) txtObservaciones.clear();
            mostrarAlerta("Lote Aprobado", "El lote " + loteId + " ha sido movido al inventario con cantidad de " + libras + ".");

        } else {
            String sql = "UPDATE tbl_produccion SET estado = 'Rechazado' WHERE lote_id = ?";

            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, loteId);
                ps.executeUpdate();
            } catch (Exception e) {}

            cargarLotesSeguimiento();
            if (txtObservaciones != null) txtObservaciones.clear();
            mostrarAlerta("Lote Rechazado", "El lote " + loteId + " ha sido marcado como RECHAZADO.");
        }
    }

    private void actualizarStockQueso(String tipoQueso, double cantidad) {
        String[][] updates = {
            {"Productos", "stock_actual", "nombre_producto"},
            {"tbl_inventario_productos", "cantidad_stock", "nombre_producto"},
            {"tbl_inventario_productos", "cantidad_disponible", "tipo_queso"}
        };
        for (String[] u : updates) {
            String sql = "UPDATE " + u[0] + " SET " + u[1] + " = COALESCE(" + u[1] + ",0) + ? WHERE " + u[2] + " = ?";
            try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, cantidad);
                ps.setString(2, tipoQueso);
                int rows = ps.executeUpdate();
                if (rows > 0) break;
            } catch (Exception e) { }
        }

        String sqlMov = "INSERT INTO tbl_movimientos_inventario (producto, tipo, cantidad, unidad, fecha_movimiento, justificacion) VALUES (?, 'ENTRADA', ?, ?, GETDATE(), ?)";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlMov)) {
            ps.setString(1, tipoQueso);
            ps.setDouble(2, cantidad);
            ps.setString(3, "Libras (Lbs)");
            ps.setString(4, "Producción finalizada - Lote " + cbLotesTerminados.getValue());
            ps.executeUpdate();
        } catch (Exception e) { }
    }

    private String obtenerTipoQueso(String loteId) {
        for (LoteProduccion lote : listaLotes) {
            if (lote.getLoteId().equals(loteId)) {
                return lote.getTipo();
            }
        }
        String sql = "SELECT tipo_queso FROM tbl_produccion WHERE lote_id = ?";
        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loteId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("tipo_queso");
        } catch (Exception e) { }
        return "Sin especificar";
    }

    private void resetVistas() {
        VBox[] panes = {paneProduccion, paneCalidad, paneInventario, paneSeguimiento};
        for (VBox p : panes) {
            if (p != null) {
                p.setVisible(false);
                p.setManaged(false);
            }
        }
    }

    @FXML private Button btnNavProduccion, btnNavSeguimiento, btnNavCalidad, btnNavInventario;

    @FXML private void mostrarProduccion() { alternarVista(paneProduccion, btnNavProduccion); }
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
            Button[] btns = {btnNavProduccion, btnNavSeguimiento, btnNavCalidad, btnNavInventario};
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

        try (Connection con = com.example.pantallas.config.ConnectionManager.getConnection();
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
            com.example.pantallas.utils.LoggerUtil.error("Excepción detectada", e);
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
