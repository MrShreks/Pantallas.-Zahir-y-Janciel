package com.example.pantallas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class AppPrincipal extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(AppPrincipal.class.getResource("MainVentas.fxml"));


        Scene scene = new Scene(fxmlLoader.load(), 1200, 750);

        stage.setTitle("Sistema de Gestión - Fábrica de Queso");
        stage.setScene(scene);

        // Centrar en pantalla y mostrar
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}