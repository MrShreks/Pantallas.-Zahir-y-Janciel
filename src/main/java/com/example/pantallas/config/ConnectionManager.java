package com.example.pantallas.config;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {
<<<<<<< HEAD
    
    private ConnectionManager() { }

    public static Connection getConnection() throws SQLException {
        if (DatabaseConfig.getDataSource() == null) {
            throw new SQLException("El DataSource no está inicializado.");
=======

    private ConnectionManager() {}

    public static Connection getConnection() throws SQLException {
        if (!DatabaseConfig.isInitialized()) {
            String err = DatabaseConfig.getInitError();
            throw new SQLException("La base de datos no está disponible: " + (err != null ? err : "Error desconocido de inicialización"));
>>>>>>> bb658e7 (Primer commit)
        }
        return DatabaseConfig.getDataSource().getConnection();
    }
}
