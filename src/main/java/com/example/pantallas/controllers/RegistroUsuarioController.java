package com.example.pantallas.controllers;

import com.example.pantallas.config.ConnectionManager;
import com.example.pantallas.security.PasswordUtil;
import com.example.pantallas.utils.LoggerUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegistroUsuarioController {

    @FXML private TextField txtNombreCompleto, txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private Label lblError;
    @FXML private Button btnCancelar;

    private static final String ROL_ASIGNADO = "VENTAS";

    @FXML
    private void guardar() {
        lblError.setText("");
        String nombre = txtNombreCompleto.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String clave = txtClave.getText().trim();

        if (nombre.isEmpty() || usuario.isEmpty() || clave.isEmpty()) {
            lblError.setText("Todos los campos son obligatorios.");
            return;
        }
        if (clave.length() < 4) {
            lblError.setText("La contraseña debe tener al menos 4 caracteres.");
            return;
        }

        String sql = "INSERT INTO tbl_usuarios (usuario, clave, nombre_completo, rol) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, PasswordUtil.hashPassword(clave));
            ps.setString(3, nombre);
            ps.setString(4, ROL_ASIGNADO);
            ps.executeUpdate();
            new Alert(Alert.AlertType.INFORMATION,
                "Usuario creado correctamente.\nRol asignado: " + ROL_ASIGNADO + "\n\nYa puedes iniciar sesión.").show();
            cerrar();
        } catch (Exception e) {
            LoggerUtil.error("Error al crear usuario", e);
            String msg = e.getMessage();
            if (msg != null && (msg.contains("PK") || msg.contains("duplicate") || msg.contains("UNIQUE"))) {
                lblError.setText("El nombre de usuario ya existe.");
            } else {
                lblError.setText("Error al guardar: " + msg);
            }
        }
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }
}
