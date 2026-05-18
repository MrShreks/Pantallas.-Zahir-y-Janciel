package com.example.pantallas;

import com.example.pantallas.base.FabricaBase;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Clase de conexión legacy — delega a FabricaBase para consistencia.
 * Todas las conexiones ahora usan SQL Server (no MySQL).
 */
public class Conexion {
    public static Connection getConnection() throws SQLException {
        return FabricaBase.abrirConexion();
    }

    public static Connection getConexion() {
        try {
            return FabricaBase.abrirConexion();
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return null;
        }
    }
}