package com.example.pantallas.ProcesoDeCompras;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainCompra extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pantallas/ProcesoCompras/CompraPrincipal.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 700);
        stage.setTitle("Sistema de Compras - Janciel Rojas");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}