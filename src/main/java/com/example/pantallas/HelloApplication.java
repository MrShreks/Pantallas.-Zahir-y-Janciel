package com.example.pantallas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("Venta.fxml"));
        stage.setScene(new Scene(loader.load(), 1000, 700));
        stage.setTitle("Fábrica de Queso");
        stage.show();
    }

    public static void main(String[] args) { launch(); }
}
