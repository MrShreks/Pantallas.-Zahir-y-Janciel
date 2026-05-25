package com.example.pantallas.repositories;

import com.example.pantallas.models.Usuario;
import com.example.pantallas.config.ConnectionManager;
import com.example.pantallas.security.PasswordUtil;
import com.example.pantallas.utils.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {

    public void asegurarTablaUsuarios() {
        String sqlCreate = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_usuarios' AND xtype='U') "
                + "CREATE TABLE tbl_usuarios ("
                + "id_usuario INT IDENTITY PRIMARY KEY, "
                + "usuario VARCHAR(100) NOT NULL UNIQUE, "
                + "clave VARCHAR(255) NOT NULL, "
                + "nombre_completo VARCHAR(200), "
                + "rol VARCHAR(50))";
        try (Connection con = ConnectionManager.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sqlCreate);
        } catch (SQLException e) {
            LoggerUtil.error("Error creando tbl_usuarios", e);
        }
        String[] columnas = {
            "IF NOT EXISTS (SELECT * FROM syscolumns WHERE id=OBJECT_ID('tbl_usuarios') AND name='nombre_completo') ALTER TABLE tbl_usuarios ADD nombre_completo VARCHAR(200)",
            "IF NOT EXISTS (SELECT * FROM syscolumns WHERE id=OBJECT_ID('tbl_usuarios') AND name='rol') ALTER TABLE tbl_usuarios ADD rol VARCHAR(50)"
        };
        try (Connection con = ConnectionManager.getConnection();
             Statement st = con.createStatement()) {
            for (String col : columnas) {
                try { st.execute(col); } catch (SQLException ignored) {}
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error asegurando columnas tbl_usuarios", e);
        }
    }

    public void seedUsuariosIniciales() {
        String[][] iniciales = {
            {"admin", "1234", "Administrador", "ADMINISTRADOR"},
            {"emil", "1234", "Emil Gomez Gomez", "VENTAS"},
            {"repartidor1", "1234", "Carlos Reyes", "REPARTIDOR"},
            {"repartidor2", "1234", "Luis Jimenez", "REPARTIDOR"},
            {"bodega", "1234", "Ana Polanco", "INVENTARIO"},
            {"compras", "1234", "Pedro Suarez", "COMPRAS"},
            {"produccion", "1234", "Maria Diaz", "PRODUCCION"},
            {"mantenimiento", "1234", "Jose Rodriguez", "MANTENIMIENTO"},
            {"distribucion", "1234", "Distribucion General", "DISTRIBUCION"}
        };
        String sqlCheck = "SELECT COUNT(*) FROM tbl_usuarios WHERE usuario = ?";
        String sqlInsert = "INSERT INTO tbl_usuarios (usuario, clave, nombre_completo, rol) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement psCheck = con.prepareStatement(sqlCheck);
             PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
            for (String[] u : iniciales) {
                psCheck.setString(1, u[0]);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        psInsert.setString(1, u[0]);
                        psInsert.setString(2, PasswordUtil.hashPassword(u[1]));
                        psInsert.setString(3, u[2]);
                        psInsert.setString(4, u[3]);
                        psInsert.executeUpdate();
                        LoggerUtil.info("Usuario creado: " + u[0] + " (" + u[3] + ")");
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("No se pudieron crear usuarios iniciales", e);
        }
    }

    public Optional<Usuario> findByUsername(String username) {
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
                        return Optional.of(u);
                    }
                }
            } catch (SQLException e) {
                LoggerUtil.warning("Fallo consulta: " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    public List<Usuario> findAll() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id_usuario, usuario, clave, nombre_completo, rol FROM tbl_usuarios ORDER BY id_usuario";
        try (Connection con = ConnectionManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Usuario u = new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("usuario"),
                    rs.getString("clave"),
                    rs.getString("rol")
                );
                lista.add(u);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error listando usuarios", e);
        }
        return lista;
    }

    public String testConnection() {
        try {
            ConnectionManager.getConnection().close();
            return "OK";
        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage();
        }
    }
}
