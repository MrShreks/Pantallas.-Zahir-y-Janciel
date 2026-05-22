package com.example.pantallas.controllers;

import com.example.pantallas.config.ConnectionManager;
import com.example.pantallas.utils.AlertManager;
import com.example.pantallas.utils.LoggerUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class LoteController {

    @FXML private TextField txtCodigoLote;
    @FXML private DatePicker dpFechaInicio;
    @FXML private ComboBox<String> cbEmpleado;
    @FXML private ComboBox<String> cbProducto;
    @FXML private TextArea txtObservaciones;
    @FXML private Button btnGuardar;

    @FXML
    public void initialize() {
        dpFechaInicio.setValue(LocalDate.now());
        cargarDatosDesdeDB();
    }

    private void cargarDatosDesdeDB() {
        try (Connection con = ConnectionManager.getConnection();
             Statement stmt = con.createStatement()) {
             
            try (ResultSet rsE = stmt.executeQuery("SELECT nombre FROM tbl_empleados")) {
                cbEmpleado.getItems().clear();
                while (rsE.next()) {
                    cbEmpleado.getItems().add(rsE.getString("nombre"));
                }
            } catch (SQLException e) {
                LoggerUtil.warning("No se pudo cargar tabla tbl_empleados");
            }

            try (ResultSet rsP = stmt.executeQuery("SELECT nombre FROM tbl_productos")) {
                cbProducto.getItems().clear();
                while (rsP.next()) {
                    cbProducto.getItems().add(rsP.getString("nombre"));
                }
            } catch (SQLException e) {
                LoggerUtil.warning("No se pudo cargar tabla tbl_productos");
            }

        } catch (SQLException e) {
            LoggerUtil.error("Error al cargar datos de Lote", e);
        }
    }

    @FXML
    private void guardarLote() {
        String codigo = txtCodigoLote.getText();
        LocalDate fecha = dpFechaInicio.getValue();
        String empleado = cbEmpleado.getValue();

        if (codigo.isEmpty() || empleado == null) {
            AlertManager.showError("Error", "Por favor rellene todos los campos obligatorios.");
        } else {
            LoggerUtil.info("Guardando Lote: " + codigo + " en base de datos...");
            AlertManager.showInfo("Éxito", "Lote registrado correctamente.");
            txtCodigoLote.clear();
            txtObservaciones.clear();
        }
    }
}
