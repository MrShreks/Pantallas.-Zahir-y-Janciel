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
}
