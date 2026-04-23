package com.example.pantallas;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Controller {

    private Stage stage;
    private Scene scene;

    @FXML
    public void navegarVentas(ActionEvent event) throws IOException {
        cambiarEscena(event, "venta.fxml");
    }

    @FXML
    public void navegarInventario(ActionEvent event) throws IOException {
        cambiarEscena(event, "inventario.fxml");
    }

    @FXML
    public void navegarClientes(ActionEvent event) throws IOException {
        cambiarEscena(event, "cliente.fxml");
    }

    private void cambiarEscena(ActionEvent event, String fxml) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}