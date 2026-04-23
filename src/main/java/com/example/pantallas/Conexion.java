package com.example.pantallas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/fabricadequeso";
        String user = "janciel";
        String password = "123456789";
        return DriverManager.getConnection(url, user, password);
    }

    public static Connection getConexion() {
            return null;
    }
}