package com.example.pantallas.controllers;

import com.example.pantallas.repositories.UsuarioRepository;
import com.example.pantallas.services.AuthService;
import com.example.pantallas.utils.AlertManager;
import com.example.pantallas.utils.LoggerUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtClave;

    private final AuthService authService = new AuthService();
    private final UsuarioRepository usuarioRepo = new UsuarioRepository();

    @FXML
    public void initialize() {
        usuarioRepo.asegurarTablaUsuarios();
        usuarioRepo.seedUsuariosIniciales();
    }

    @FXML
    private void ingresar(ActionEvent event) {
        try {
            String usuario = txtUsuario.getText().trim();
            String clave = txtClave.getText().trim();

            if (usuario.isEmpty() || clave.isEmpty()) {
                AlertManager.showWarning("Campos Vacíos", "Por favor, completa todos los campos.");
                return;
            }

            if (hacerLogin(usuario, clave)) {
                LoggerUtil.info("Acceso concedido para: " + usuario);
                navegarAMenuPrincipal(event);
            }
        } catch (Exception e) {
            LoggerUtil.error("Error al iniciar sesión", e);
            AlertManager.showError("Error inesperado", "Ocurrió un error al iniciar sesión:\n" + e.getMessage());
        }
    }

    private boolean hacerLogin(String usuario, String clave) {
        String resultado = authService.login(usuario, clave);
        if (resultado.equals("OK")) return true;
        if (resultado.contains("Error")) {
            LoggerUtil.error("Error en login: " + resultado);
        }
        AlertManager.showError("Error de Acceso", resultado);
        return false;
    }

    private void navegarAMenuPrincipal(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/pantallas/BienvenidaScreen.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.setTitle("Bienvenido - Fábrica de Queso");
            stage.setResizable(true);
            stage.setMaximized(true);
        } catch (IOException e) {
            LoggerUtil.error("Error al cargar bienvenida", e);
            AlertManager.showError("Error de navegación", "No se pudo cargar la pantalla de bienvenida:\n" + e.getMessage());
        }
    }

    @FXML
    private void crearCuenta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pantallas/RegistroUsuario.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.setTitle("Registro de Usuario");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            LoggerUtil.error("Error al abrir registro", e);
            AlertManager.showError("Error", "No se pudo abrir el registro de usuario.");
        }
    }

    @FXML
    private void olvideContrasenia(ActionEvent event) {
        LoggerUtil.info("Navegando a Recuperación...");
        AlertManager.showInfo("Recuperación", "Contacta al administrador del sistema para restablecer tu contraseña.");
    }
}
