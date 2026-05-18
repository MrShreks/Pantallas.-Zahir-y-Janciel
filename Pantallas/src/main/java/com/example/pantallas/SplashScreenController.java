package com.example.pantallas;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class SplashScreenController {

    @FXML private ProgressBar progressBar;
    @FXML private Label lblLoading;
    @FXML private Button btnComenzar;
    @FXML private Label lblLogo;

    @FXML
    public void initialize() {
        btnComenzar.setVisible(false);
        btnComenzar.setOpacity(0);

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.8), lblLogo);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.15);
        pulse.setToY(1.15);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.play();

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(1.2), e -> lblLoading.setText("Cargando módulos...")),
            new KeyFrame(Duration.seconds(2.4), e -> lblLoading.setText("Preparando todo...")),
            new KeyFrame(Duration.seconds(3.2), new KeyValue(progressBar.progressProperty(), 1))
        );
        timeline.setOnFinished(e -> {
            lblLoading.setText("¡Listo!");
            lblLoading.setStyle("-fx-text-fill: #d4af37; -fx-font-size: 12px; -fx-font-weight: bold;");

            FadeTransition ft = new FadeTransition(Duration.seconds(1), btnComenzar);
            ft.setFromValue(0);
            ft.setToValue(1);
            btnComenzar.setVisible(true);
            ft.play();
        });
        timeline.play();
    }

    @FXML
    private void comenzar(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/pantallas/Login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Inicio de Sesión - Fábrica de Queso");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar Login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
