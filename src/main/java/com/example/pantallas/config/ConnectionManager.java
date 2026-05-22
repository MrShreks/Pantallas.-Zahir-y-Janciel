package com.example.pantallas.config;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {

    private ConnectionManager() {}

    public static Connection getConnection() throws SQLException {
        if (!DatabaseConfig.isInitialized()) {
            String err = DatabaseConfig.getInitError();
            throw new SQLException("La base de datos no está disponible: " + (err != null ? err : "Error desconocido de inicialización"));
        }
        return DatabaseConfig.getDataSource().getConnection();
    }
}
