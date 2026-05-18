package com.example.pantallas.config;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {
    
    private ConnectionManager() { }

    public static Connection getConnection() throws SQLException {
        if (DatabaseConfig.getDataSource() == null) {
            throw new SQLException("El DataSource no está inicializado.");
        }
        return DatabaseConfig.getDataSource().getConnection();
    }
}
