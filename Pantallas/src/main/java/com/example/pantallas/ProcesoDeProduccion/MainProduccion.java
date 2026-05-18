package com.example.pantallas.ProcesoDeProduccion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class MainProduccion extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        URL fxmlUrl = getClass().getResource("/com/example/pantallas/ProcesoProduccion/ProduccionPrincipal.fxml");

        if (fxmlUrl == null) {
            System.err.println(" ERROR: No se encontró el FXML. Verifica que esté en src/main/resources/com/example/pantallas/ProcesoProduccion/");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load(), 1200, 700);
        stage.setTitle("Producción Fábrica de Queso - Janciel Rojas");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}