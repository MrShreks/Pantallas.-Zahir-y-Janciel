module com.example.pantallas {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;
    requires jasperreports;
    requires java.desktop;
    requires java.xml;
    requires com.zaxxer.hikari;
    requires org.slf4j;
    requires jbcrypt;
    
    opens com.example.pantallas to javafx.fxml;
    opens com.example.pantallas.controllers to javafx.fxml;
    opens com.example.pantallas.models to javafx.base;
    opens com.example.pantallas.ProcesoDeProduccion to javafx.fxml;
    opens com.example.pantallas.ProcesoDeCompras to javafx.fxml;
    opens com.example.pantallas.ProcesoDeInventario to javafx.fxml;
    opens com.example.pantallas.ProcesoDeDistribucion to javafx.fxml;
    opens com.example.pantallas.ProcesoDeMantenimiento to javafx.fxml;
    opens com.example.pantallas.ProcesoDeVenta to javafx.fxml;
    opens com.example.pantallas.MenuPrincipal to javafx.fxml;
    opens com.example.pantallas.Dashboard to javafx.fxml;

    exports com.example.pantallas;
    exports com.example.pantallas.controllers;
    exports com.example.pantallas.models;
    exports com.example.pantallas.utils;
    exports com.example.pantallas.security;
    exports com.example.pantallas.config;
    exports com.example.pantallas.services;
    exports com.example.pantallas.repositories;
    
    exports com.example.pantallas.ProcesoDeProduccion;
    exports com.example.pantallas.ProcesoDeCompras;
    exports com.example.pantallas.ProcesoDeInventario;
    exports com.example.pantallas.ProcesoDeDistribucion;
    exports com.example.pantallas.ProcesoDeMantenimiento;
    exports com.example.pantallas.ProcesoDeVenta;
    exports com.example.pantallas.MenuPrincipal;
    exports com.example.pantallas.Dashboard;
}