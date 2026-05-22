package com.example.pantallas.services;

import com.example.pantallas.config.ConnectionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class FabricaBase {

    private FabricaBase() {}

    public static Connection abrirConexion() throws SQLException {
        return ConnectionManager.getConnection();
    }

    public static void asegurarEsquemaProveedores() {
        String sqlCreate = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_proveedores' AND xtype='U') "
                + "CREATE TABLE tbl_proveedores ("
                + "provider_id INT IDENTITY PRIMARY KEY, "
                + "nombre VARCHAR(200) NOT NULL, "
                + "rnc VARCHAR(20), "
                + "telefono VARCHAR(50), "
                + "direccion VARCHAR(500), "
                + "activo BIT DEFAULT 1)";
        try (Connection con = ConnectionManager.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sqlCreate);
        } catch (SQLException e) {
            com.example.pantallas.utils.LoggerUtil.error("Error creando esquema proveedores", e);
        }
        String[] columnas = {
            "IF NOT EXISTS (SELECT * FROM syscolumns WHERE id=OBJECT_ID('tbl_proveedores') AND name='rnc') ALTER TABLE tbl_proveedores ADD rnc VARCHAR(20)",
            "IF NOT EXISTS (SELECT * FROM syscolumns WHERE id=OBJECT_ID('tbl_proveedores') AND name='telefono') ALTER TABLE tbl_proveedores ADD telefono VARCHAR(50)",
            "IF NOT EXISTS (SELECT * FROM syscolumns WHERE id=OBJECT_ID('tbl_proveedores') AND name='direccion') ALTER TABLE tbl_proveedores ADD direccion VARCHAR(500)",
            "IF NOT EXISTS (SELECT * FROM syscolumns WHERE id=OBJECT_ID('tbl_proveedores') AND name='activo') ALTER TABLE tbl_proveedores ADD activo BIT DEFAULT 1"
        };
        try (Connection con = ConnectionManager.getConnection();
             Statement st = con.createStatement()) {
            for (String col : columnas) {
                try { st.execute(col); } catch (SQLException ignored) {}
            }
        } catch (SQLException e) {
            com.example.pantallas.utils.LoggerUtil.error("Error asegurando columnas proveedores", e);
        }
    }

    public static void seedFactoresConversion() {
        String sql = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tbl_factores_conversion' AND xtype='U') "
                + "CREATE TABLE tbl_factores_conversion ("
                + "id INT IDENTITY PRIMARY KEY, "
                + "id_proveedor INT, "
                + "producto VARCHAR(200), "
                + "precio_unitario DECIMAL(10,2), "
                + "unidad VARCHAR(50))";
        try (Connection con = ConnectionManager.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            com.example.pantallas.utils.LoggerUtil.error("Error creando factores_conversion", e);
        }
    }
}
