package com.example.pantallas.MenuPrincipal;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class MenuPrincipalController {

    private void navegarA(Object eventSource, String fxmlPath, String titulo) {
        try {
            Stage stage = (Stage) ((Node) eventSource).getScene().getWindow();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));

            if (fxmlLoader.getLocation() == null) {
                System.err.println("NO SE ENCONTRÓ EL ARCHIVO: " + fxmlPath);
                return;
            }

            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Error cargando la pantalla: " + titulo);
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirCompras(ActionEvent event) {
        navegarA(event.getSource(), "/com/example/pantallas/ProcesoCompras/CompraPrincipal.fxml", "Gestión de Compras");
    }

    @FXML
    private void abrirVentas(ActionEvent event) {
        navegarA(event.getSource(), "/com/example/pantallas/ProcesoDeVenta/MainVentas.fxml", "Módulo de Ventas");
    }

    @FXML
    private void abrirInventario(ActionEvent event) {
        // Ajusta el nombre si tu FXML de inventario se llama distinto
        navegarA(event.getSource(), "/com/example/pantallas/ProcesoInventario/InventarioPrincipal.fxml", "Control de Inventario");
    }

    @FXML
    private void abrirProduccion(ActionEvent event) {
        navegarA(event.getSource(), "/com/example/pantallas/ProcesoProduccion/ProduccionPrincipal.fxml", "Gestión de Producción");
    }

    @FXML
    private void abrirDistribucion(ActionEvent event) {
        navegarA(event.getSource(), "/com/example/pantallas/ProcesoDistribucion/DistribucionPrincipal.fxml", "Sistema de Distribución");
    }

    @FXML
    private void abrirMantenimiento(ActionEvent event) {
        navegarA(event.getSource(), "/com/example/pantallas/ProcesoMantenimiento/MantenimientoPrincipal.fxml", "Mantenimiento y Equipos");
    }

    @FXML
    private void salir() {
        System.exit(0);
    }
}