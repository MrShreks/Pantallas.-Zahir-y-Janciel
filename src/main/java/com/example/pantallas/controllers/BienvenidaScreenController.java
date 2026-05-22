package com.example.pantallas.controllers;

import com.example.pantallas.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import com.example.pantallas.utils.AlertManager;

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
        lblRol.setText("CARGO: " + (rol != null ? rol : "—"));

        progressBar.setStyle("-fx-accent: #e0b878;");

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
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Stage stage = (Stage) progressBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.setTitle("Panel Principal - Fábrica de Queso");
            stage.setResizable(true);
            stage.setMaximized(true);
        } catch (IOException e) {
            com.example.pantallas.utils.LoggerUtil.error("Error al cargar el menú: " + e.getMessage());
            com.example.pantallas.utils.LoggerUtil.error("Excepción detectada", e);
            AlertManager.showError("Error de navegación", "No se pudo cargar el menú principal:\n" + e.getMessage());
        }
    }
}
