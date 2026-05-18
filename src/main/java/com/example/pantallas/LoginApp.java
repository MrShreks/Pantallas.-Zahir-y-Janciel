package com.example.pantallas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import com.example.pantallas.utils.LoggerUtil;

public class LoginApp extends Application {
    @Override
    public void start(Stage stage) {
        try {
            String ruta = "/com/example/pantallas/Login.fxml";
            URL fxmlLocation = getClass().getResource(ruta);

            if (fxmlLocation == null) {
                LoggerUtil.error("No se encontró el archivo FXML del Login.");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load(), 850, 550);

            stage.setTitle("Inicio de Sesión - Fábrica de Queso");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();
            stage.setMaximized(true);
        } catch (Exception e) {
            LoggerUtil.error("Error al iniciar LoginApp", e);
        }
    }

    public static void main(String[] args) { launch(); }
}