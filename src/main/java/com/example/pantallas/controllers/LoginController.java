package com.example.pantallas.controllers;

import com.example.pantallas.services.AuthService;
import com.example.pantallas.utils.AlertManager;
import com.example.pantallas.utils.LoggerUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
<<<<<<< HEAD
=======
import javafx.scene.Parent;
>>>>>>> bb658e7 (Primer commit)
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtClave;

    private final AuthService authService = new AuthService();

    @FXML
    private void ingresar(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String clave = txtClave.getText().trim();

        if (usuario.isEmpty() || clave.isEmpty()) {
            AlertManager.showWarning("Campos Vacíos", "Por favor, completa todos los campos.");
            return;
        }

<<<<<<< HEAD
        if (authService.login(usuario, clave)) {
            LoggerUtil.info("Acceso concedido para: " + usuario);
            navegarAMenuPrincipal(event);
        } else {
            AlertManager.showError("Error de Acceso", "Usuario o contraseña incorrectos.");
=======
        String resultado = authService.login(usuario, clave);
        if (resultado.equals("OK")) {
            LoggerUtil.info("Acceso concedido para: " + usuario);
            navegarAMenuPrincipal(event);
        } else {
            if (resultado.contains("Error")) {
                LoggerUtil.error("Error en login: " + resultado);
            }
            AlertManager.showError("Error de Acceso", resultado);
>>>>>>> bb658e7 (Primer commit)
        }
    }

    private void navegarAMenuPrincipal(ActionEvent event) {
        try {
<<<<<<< HEAD
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/pantallas/BienvenidaScreen.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("Bienvenido - Fábrica de Queso");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(wasMaximized);
            if (!wasMaximized) stage.centerOnScreen();
            stage.show();
=======
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pantallas/BienvenidaScreen.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.setTitle("Bienvenido - Fábrica de Queso");
            stage.setResizable(true);
            stage.setMaximized(true);
>>>>>>> bb658e7 (Primer commit)
        } catch (IOException e) {
            LoggerUtil.error("Error al cargar bienvenida", e);
        }
    }

    @FXML
    private void crearCuenta(ActionEvent event) {
        LoggerUtil.info("Navegando a Registro...");
    }

    @FXML
    private void olvideContrasenia(ActionEvent event) {
        LoggerUtil.info("Navegando a Recuperación...");
    }
}
