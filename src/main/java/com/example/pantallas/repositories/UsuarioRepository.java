package com.example.pantallas.repositories;

import com.example.pantallas.models.Usuario;
import com.example.pantallas.config.ConnectionManager;
import com.example.pantallas.utils.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UsuarioRepository {

    public Optional<Usuario> findByUsername(String username) {
<<<<<<< HEAD
        String query = "SELECT id_usuario, usuario, clave, rol FROM tbl_usuarios WHERE usuario = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("usuario"),
                        rs.getString("clave"),
                        rs.getString("rol")
                    );
                    return Optional.of(u);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al buscar usuario por nombre", e);
        }
        return Optional.empty();
    }
=======
        String[] queries = {
            "SELECT id_usuario, usuario, clave, rol FROM tbl_usuarios WHERE usuario = ?",
            "SELECT id_usuario, nombre_usuario AS usuario, contrasena AS clave, '' AS rol FROM Usuarios WHERE nombre_usuario = ?"
        };
        for (String query : queries) {
            try (Connection conn = ConnectionManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Usuario u = new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("usuario"),
                            rs.getString("clave"),
                            rs.getString("rol")
                        );
                        LoggerUtil.info("Usuario encontrado en tabla: " + query.substring(query.indexOf("FROM") + 5, query.indexOf("WHERE")).trim());
                        return Optional.of(u);
                    }
                }
            } catch (SQLException e) {
                LoggerUtil.warning("Fallo consulta (" + query.substring(0, 60) + "...): " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    public String testConnection() {
        try {
            ConnectionManager.getConnection().close();
            return "OK";
        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }
>>>>>>> bb658e7 (Primer commit)
}
