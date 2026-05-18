package com.example.pantallas;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtClave;

    // Configuración corregida con tu base de datos y parámetros de seguridad
    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;trustServerCertificate=true;encrypt=false;";
    private final String USER_DB = "sa";
    private final String PASS_DB = "123456"; // Pon aquí la clave de tu instancia SQL

    @FXML
    private void ingresar(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String clave = txtClave.getText().trim();

        if (usuario.isEmpty() || clave.isEmpty()) {
            mostrarAlerta("Campos Vacíos", "Por favor, completa todos los campos.");
        } else {
            if (validarLogin(usuario, clave)) {
                System.out.println("Acceso concedido para: " + usuario);
                navegarAMenuPrincipal(event);
            } else {
                mostrarAlerta("Error de Acceso", "Usuario o contraseña incorrectos.");
            }
        }
    }

    private boolean validarLogin(String usuario, String clave) {
        String sql = "SELECT * FROM Usuarios WHERE nombre_usuario = ? AND contrasena = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER_DB, PASS_DB);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);
            pstmt.setString(2, clave);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Retorna true si los datos coinciden
            }

        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return false;
        }
    }

    private void navegarAMenuPrincipal(ActionEvent event) {
        try {
            // Ruta de tu FXML de menú
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/pantallas/MenuPrincipal/MenuPrincipal.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle("Panel Principal - Fábrica de Queso");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar menú: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void crearCuenta(ActionEvent event) {
        System.out.println("Navegando a Registro...");
    }

    @FXML
    private void olvideContrasenia(ActionEvent event) {
        System.out.println("Navegando a Recuperación...");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}