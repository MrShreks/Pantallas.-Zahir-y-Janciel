package com.example.pantallas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class LoginApp extends Application {
    @Override
    public void start(Stage stage) {
        try {
            // Asegúrate de que el FXML esté en esta carpeta de resources
            String ruta = "/com/example/pantallas/Login.fxml";
            URL fxmlLocation = getClass().getResource(ruta);

            if (fxmlLocation == null) {
                System.err.println("❌ ERROR: No se encontró el archivo FXML del Login.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load(), 400, 500);

            stage.setTitle("Inicio de Sesión - Fábrica de Queso");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) { launch(); }
}