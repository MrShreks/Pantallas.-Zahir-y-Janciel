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

    exports com.example.pantallas;
    exports com.example.pantallas.controllers;
    exports com.example.pantallas.models;
    exports com.example.pantallas.utils;
    exports com.example.pantallas.security;
    exports com.example.pantallas.config;
    exports com.example.pantallas.services;
    exports com.example.pantallas.repositories;
}