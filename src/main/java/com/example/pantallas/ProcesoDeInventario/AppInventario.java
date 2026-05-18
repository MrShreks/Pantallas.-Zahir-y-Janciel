package com.example.pantallas.ProcesoDeInventario;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class AppInventario extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/pantallas/ProcesoInventario/InventarioPrincipal.fxml"));


        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

        stage.setTitle("Sistema de Gestión - Fábrica de Queso");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}