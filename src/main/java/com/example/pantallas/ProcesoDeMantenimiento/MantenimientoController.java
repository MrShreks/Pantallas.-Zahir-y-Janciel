package com.example.pantallas.ProcesoDeMantenimiento;

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
import javafx.util.Callback;
import java.io.IOException;
import java.sql.*;

public class MantenimientoController {

    @FXML private VBox paneEquipos, panePlanificacion, paneHistorial;
    @FXML private TextField txtNombreEquipo, txtModelo;
    @FXML private ComboBox<String> cbEstadoEquipo, cbEquipos;
    @FXML private DatePicker dpFechaManto;
    @FXML private RadioButton rbPreventivo, rbCorrectivo;
    private ToggleGroup groupTipo;

    @FXML private TableView<Maquinaria> tablaMaquinaria;
    @FXML private TableColumn<Maquinaria, Integer> colId;
    @FXML private TableColumn<Maquinaria, String> colNombre, colModelo, colEstado, colUltimoManto;

    @FXML private TableView<MantenimientoTarea> tablaHistorial;
    @FXML private TableColumn<MantenimientoTarea, String> colHistFecha, colHistEquipo, colHistTipo, colHistEstado;
    @FXML private TableColumn<MantenimientoTarea, Void> colHistAccion;

    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;encrypt=true;trustServerCertificate=true;";
    private final String USER = "janciel";
    private final String PASS = "123456789";

    private int idSeleccionado = -1;

    @FXML
    public void initialize() {
        if (cbEstadoEquipo != null) cbEstadoEquipo.getItems().addAll("Operativo", "En Mantenimiento", "Fuera de Servicio");

        groupTipo = new ToggleGroup();
        if (rbPreventivo != null) {
            rbPreventivo.setToggleGroup(groupTipo);
            rbCorrectivo.setToggleGroup(groupTipo);
            rbPreventivo.setSelected(true);
        }

        configurarTablas();
        cargarTodo();

        // Escuchar clics en la tabla para editar
        tablaMaquinaria.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                idSeleccionado = newSelection.getId();
                txtNombreEquipo.setText(newSelection.getNombre());
                txtModelo.setText(newSelection.getModelo());
                cbEstadoEquipo.setValue(newSelection.getEstado());
            }
        });
    }

    private void configurarTablas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colUltimoManto.setCellValueFactory(new PropertyValueFactory<>("ultimoManto"));

        colHistFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHistEquipo.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        colHistTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colHistEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Configuración de la columna de acción con ComboBox
        Callback<TableColumn<MantenimientoTarea, Void>, TableCell<MantenimientoTarea, Void>> cellFactory = param -> {
            return new TableCell<>() {
                private final ComboBox<String> cbCambiarEstado = new ComboBox<>(FXCollections.observableArrayList("Pendiente", "En Proceso", "Completado", "Cancelado"));

                {
                    cbCambiarEstado.setPromptText("Cambiar...");
                    cbCambiarEstado.setOnAction(event -> {
                        MantenimientoTarea tarea = getTableView().getItems().get(getIndex());
                        String nuevoEstado = cbCambiarEstado.getValue();
                        if (nuevoEstado != null) {
                            actualizarEstadoTarea(tarea.getId(), nuevoEstado);
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(cbCambiarEstado);
                    }
                }
            };
        };
        colHistAccion.setCellFactory(cellFactory);
    }

    private void actualizarEstadoTarea(int idTarea, String nuevoEstado) {
        String sql = "UPDATE tbl_mantenimiento_programado SET estado = ? WHERE id_mantenimiento = ?";
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idTarea);
            ps.executeUpdate();
            cargarTodo(); // Recargar para ver los cambios reflejados
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- NAVEGACIÓN ---
    @FXML private void irAEquipos() { alternar(paneEquipos); }
    @FXML private void irAPlanificacion() { alternar(panePlanificacion); }
    @FXML private void irAHistorial() { alternar(paneHistorial); }

    private void alternar(VBox visible) {
        paneEquipos.setVisible(false); paneEquipos.setManaged(false);
        panePlanificacion.setVisible(false); panePlanificacion.setManaged(false);
        paneHistorial.setVisible(false); paneHistorial.setManaged(false);
        visible.setVisible(true); visible.setManaged(true);
    }

    @FXML
    private void volverMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Error al navegar: " + e.getMessage());
        }
    }

    // --- CRUD EQUIPOS ---
    @FXML
    private void guardarEquipo() {
        String sql = "INSERT INTO tbl_maquinaria (nombre, modelo, estado) VALUES (?, ?, ?)";
        ejecutarUpdate(sql, txtNombreEquipo.getText(), txtModelo.getText(), cbEstadoEquipo.getValue());
        cargarTodo();
        limpiarCampos();
    }

    @FXML
    private void actualizarEquipo() {
        if (idSeleccionado == -1) return;
        String sql = "UPDATE tbl_maquinaria SET nombre=?, modelo=?, estado=? WHERE id_maquinaria=?";
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtNombreEquipo.getText());
            ps.setString(2, txtModelo.getText());
            ps.setString(3, cbEstadoEquipo.getValue());
            ps.setInt(4, idSeleccionado);
            ps.executeUpdate();
            cargarTodo();
            limpiarCampos();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void borrarEquipo() {
        if (idSeleccionado == -1) return;
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement("DELETE FROM tbl_maquinaria WHERE id_maquinaria=?")) {
            ps.setInt(1, idSeleccionado);
            ps.executeUpdate();
            cargarTodo();
            limpiarCampos();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void limpiarCampos() {
        txtNombreEquipo.clear();
        txtModelo.clear();
        cbEstadoEquipo.getSelectionModel().clearSelection();
        idSeleccionado = -1;
        tablaMaquinaria.getSelectionModel().clearSelection();
    }

    // --- LÓGICA DE DATOS ---
    @FXML
    private void programarTarea() {
        String sql = "INSERT INTO tbl_mantenimiento_programado (equipo_nombre, fecha_programada, tipo, estado) VALUES (?, ?, ?, 'Pendiente')";
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cbEquipos.getValue());
            ps.setDate(2, Date.valueOf(dpFechaManto.getValue()));
            ps.setString(3, ((RadioButton)groupTipo.getSelectedToggle()).getText());
            ps.executeUpdate();
            cargarTodo();
            irAHistorial();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void ejecutarUpdate(String sql, String... params) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void cargarTodo() {
        ObservableList<Maquinaria> maq = FXCollections.observableArrayList();
        ObservableList<MantenimientoTarea> mant = FXCollections.observableArrayList();
        if (cbEquipos != null) cbEquipos.getItems().clear();

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            ResultSet rs1 = con.createStatement().executeQuery("SELECT * FROM tbl_maquinaria");
            while (rs1.next()) {
                maq.add(new Maquinaria(rs1.getInt("id_maquinaria"), rs1.getString("nombre"),
                        rs1.getString("modelo"), rs1.getString("estado"), rs1.getString("ultimo_mantenimiento")));
                if (cbEquipos != null) cbEquipos.getItems().add(rs1.getString("nombre"));
            }
            tablaMaquinaria.setItems(maq);

            ResultSet rs2 = con.createStatement().executeQuery("SELECT * FROM tbl_mantenimiento_programado");
            while (rs2.next()) {
                mant.add(new MantenimientoTarea(rs2.getInt("id_mantenimiento"), rs2.getString("fecha_programada"),
                        rs2.getString("equipo_nombre"), rs2.getString("tipo"), rs2.getString("estado")));
            }
            tablaHistorial.setItems(mant);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- CLASES MODELO ---
    public static class Maquinaria {
        private int id; private String nombre, modelo, estado, ultimoManto;
        public Maquinaria(int id, String n, String m, String e, String u) {
            this.id = id; this.nombre = n; this.modelo = m; this.estado = e; this.ultimoManto = u;
        }
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getModelo() { return modelo; }
        public String getEstado() { return estado; }
        public String getUltimoManto() { return ultimoManto; }
    }

    public static class MantenimientoTarea {
        private int id; private String fecha, equipo, tipo, estado;
        public MantenimientoTarea(int id, String f, String e, String t, String s) {
            this.id = id; this.fecha = f; this.equipo = e; this.tipo = t; this.estado = s;
        }
        public int getId() { return id; }
        public String getFecha() { return fecha; }
        public String getEquipo() { return equipo; }
        public String getTipo() { return tipo; }
        public String getEstado() { return estado; }
    }
}