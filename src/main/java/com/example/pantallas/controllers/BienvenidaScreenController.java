package com.example.pantallas.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class BienvenidaScreenController {

    @FXML private ProgressBar progressBar;
    @FXML private Label lblLoading;
    @FXML private Label lblUsuario;
    @FXML private Label lblRol;

    @FXML
    public void initialize() {
        String usuario = SessionManager.getUsuario();
        String rol = SessionManager.getRol();

        lblUsuario.setText(usuario != null ? usuario : "Usuario");
        lblRol.setText("Rol: " + (rol != null ? rol : "—"));

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(1.0), e -> lblLoading.setText("Cargando módulos disponibles...")),
            new KeyFrame(Duration.seconds(2.0), e -> lblLoading.setText("Personalizando tu experiencia...")),
            new KeyFrame(Duration.seconds(2.8), new KeyValue(progressBar.progressProperty(), 1))
        );
        timeline.setOnFinished(e -> irAlMenu());
        timeline.play();
    }

    private void irAlMenu() {
        try {
            Stage stage = (Stage) progressBar.getScene().getWindow();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            stage.setTitle("Panel Principal - Fábrica de Queso");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            com.example.pantallas.utils.LoggerUtil.error("Error al cargar el menú: " + e.getMessage());
            com.example.pantallas.utils.LoggerUtil.error("Excepci�n detectada", e);
        }
    }
}



