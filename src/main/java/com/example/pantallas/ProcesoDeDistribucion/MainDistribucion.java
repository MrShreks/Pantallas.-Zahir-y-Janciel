package com.example.pantallas.ProcesoDeDistribucion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class MainDistribucion extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        URL fxmlLocation = getClass().getResource("/com/example/pantallas/ProcesoDistribucion/DistribucionPrincipal.fxml");


        if (fxmlLocation == null) {
            throw new RuntimeException("No se encontró el archivo DistribucionPrincipal.fxml en la ruta especificada.");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

        stage.setTitle("Distribución - Fábrica de Queso");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}