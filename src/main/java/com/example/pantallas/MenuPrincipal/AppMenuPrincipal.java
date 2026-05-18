package com.example.pantallas.MenuPrincipal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class AppMenuPrincipal extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));

        if (fxmlLoader.getLocation() == null) {
            throw new RuntimeException("No se encontró el archivo FXML. Revisa la ruta en resources.");
        }

        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);



        stage.setTitle("Sistema de Gestión Integral - Fábrica de Queso 🧀");
        stage.setScene(scene);
        stage.show();
    }
}