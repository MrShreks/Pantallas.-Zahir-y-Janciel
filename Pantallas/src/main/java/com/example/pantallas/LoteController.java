package com.example.pantallas;

import javafx.fxml.FXML;
import javafx.scene.control.*;
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
        // Aquí cargarías los datos de tus tablas tbl_.empleados y tbl_.productos
        cbEmpleado.getItems().addAll("Juan Pérez (Maestro)", "Ana García (Técnico)");
        cbProducto.getItems().addAll("Cheddar", "Mozzarella", "Parmesano");
        dpFechaInicio.setValue(LocalDate.now());
    }

    @FXML
    private void guardarLote() {
        String codigo = txtCodigoLote.getText();
        LocalDate fecha = dpFechaInicio.getValue();
        String empleado = cbEmpleado.getValue();

        if (codigo.isEmpty() || empleado == null) {
            mostrarAlerta("Error", "Por favor rellene todos los campos obligatorios.");
        } else {

            System.out.println("Guardando Lote: " + codigo);
            mostrarAlerta("Éxito", "Lote registrado correctamente en la base de datos.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}