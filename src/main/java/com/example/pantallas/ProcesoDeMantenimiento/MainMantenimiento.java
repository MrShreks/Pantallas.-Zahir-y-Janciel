package com.example.pantallas.ProcesoDeMantenimiento;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class MainMantenimiento extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Ruta absoluta para evitar errores de "Location is not set"
        URL fxmlLocation = getClass().getResource("/com/example/pantallas/ProcesoMantenimiento/MantenimientoPrincipal.fxml");

        if (fxmlLocation == null) {
            System.err.println(" ERROR: No se encontró el archivo FXML.");
            System.err.println("Verifica que el archivo esté en: src/main/resources/com/example/pantallas/ProcesoMantenimiento/MantenimientoPrincipal.fxml");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);

        // Ajustamos a 1200x700 para que el sidebar y las tablas tengan espacio
        Scene scene = new Scene(loader.load(), 1200, 700);

        stage.setTitle("Gestión de Mantenimiento - Fábrica de Queso");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}