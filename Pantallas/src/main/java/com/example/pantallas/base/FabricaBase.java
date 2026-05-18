package com.example.pantallas.base;

import java.sql.*;

public final class FabricaBase {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso;trustServerCertificate=true;encrypt=false;";
    private static final String USER = "sa";
    private static final String PASS = "123456";

    static { cargarDriver(); }

    private FabricaBase() { }

    private static void cargarDriver() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("SQL Server JDBC Driver no encontrado: " + e.getMessage());
        }
    }

    public static Connection abrirConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void cerrarSilenciosamente(AutoCloseable... recursos) {
        for (AutoCloseable r : recursos) {
            if (r != null) {
                try { r.close(); } catch (Exception e) { /* ignore */ }
            }
        }
    }

    public static int ejecutarUpdate(String sql, Object... params) {
        try (Connection con = abrirConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            return -1;
        }
    }

    public static <T> T ejecutarQuery(java.util.function.Function<ResultSet, T> extractor, String sql, Object... params) {
        try (Connection con = abrirConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ResultSet rs = ps.executeQuery();
            return extractor.apply(rs);
        } catch (SQLException e) {
            System.err.println("SQL Query Error: " + e.getMessage());
            return null;
        }
    }

    public static boolean tablaExiste(String nombreTabla) {
        String sql = "SELECT 1 FROM sys.tables WHERE name = ?";
        try (Connection con = abrirConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreTabla);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Asegura que tbl_suplidor_productos exista para precios normalizados por factor de conversión.
     * Usa las tablas existentes: tbl_suplidores (proveedores) y tbl_suplidor_productos (precios por unidad).
     */
    public static void asegurarEsquemaProveedores() {
        String sql = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_suplidores' AND xtype='U') "
                + "CREATE TABLE tbl_suplidores ("
                + "id INT IDENTITY PRIMARY KEY, "
                + "nombre VARCHAR(200) NOT NULL, "
                + "contacto VARCHAR(200), "
                + "telefono VARCHAR(50))";
        try (Connection con = abrirConexion(); Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error asegurando tbl_suplidores: " + e.getMessage());
        }
    }

    public static void seedFactoresConversion() {
        String sql = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_factores_conversion' AND xtype='U') "
                + "CREATE TABLE tbl_factores_conversion ("
                + "id INT IDENTITY PRIMARY KEY, "
                + "producto_origen VARCHAR(200), "
                + "unidad_origen VARCHAR(50), "
                + "producto_destino VARCHAR(200), "
                + "unidad_destino VARCHAR(50), "
                + "factor DECIMAL(10,4))";
        try (Connection con = abrirConexion(); Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error asegurando tbl_factores_conversion: " + e.getMessage());
        }
    }

    public static void asegurarTablaProductos() {
        String sqlCreate = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_suplidor_productos' AND xtype='U') "
                + "CREATE TABLE tbl_suplidor_productos ("
                + "id INT IDENTITY PRIMARY KEY, "
                + "id_suplidor INT, "
                + "producto VARCHAR(200) NOT NULL, "
                + "precio_unitario DECIMAL(10,2), "
                + "unidad VARCHAR(50))";
        try (Connection con = abrirConexion(); Statement st = con.createStatement()) {
            st.execute(sqlCreate);
        } catch (SQLException e) {
            System.err.println("Error asegurando tbl_suplidor_productos: " + e.getMessage());
        }
    }
}
