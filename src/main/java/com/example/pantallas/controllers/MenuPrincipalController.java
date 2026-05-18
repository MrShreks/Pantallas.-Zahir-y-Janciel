package com.example.pantallas.controllers;

import com.example.pantallas.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuPrincipalController {

    @FXML private Label lblUsuario;
    @FXML private Label lblRol;
    @FXML private Button btnCompras;
    @FXML private Button btnVentas;
    @FXML private Button btnInventario;
    @FXML private Button btnProduccion;
    @FXML private Button btnDistribucion;
    @FXML private Button btnMantenimiento;

    private static final Map<String, List<String>> ROL_MODULOS = new HashMap<>();

    static {
        ROL_MODULOS.put("ADMINISTRADOR", Arrays.asList("COMPRAS", "VENTAS", "INVENTARIO", "PRODUCCION", "DISTRIBUCION", "MANTENIMIENTO"));
        ROL_MODULOS.put("ADMIN", Arrays.asList("COMPRAS", "VENTAS", "INVENTARIO", "PRODUCCION", "DISTRIBUCION", "MANTENIMIENTO"));
        ROL_MODULOS.put("GERENTE", Arrays.asList("COMPRAS", "VENTAS", "INVENTARIO", "PRODUCCION", "DISTRIBUCION", "MANTENIMIENTO"));
        ROL_MODULOS.put("PRODUCCION", Arrays.asList("PRODUCCION", "INVENTARIO"));
        ROL_MODULOS.put("VENTAS", Arrays.asList("VENTAS", "DISTRIBUCION"));
        ROL_MODULOS.put("COMPRAS", Arrays.asList("COMPRAS"));
        ROL_MODULOS.put("INVENTARIO", Arrays.asList("INVENTARIO"));
        ROL_MODULOS.put("MANTENIMIENTO", Arrays.asList("MANTENIMIENTO"));
        ROL_MODULOS.put("DISTRIBUCION", Arrays.asList("DISTRIBUCION", "VENTAS"));
        ROL_MODULOS.put("USUARIO", Arrays.asList("VENTAS", "INVENTARIO"));
    }

    private static final List<String> TODOS_MODULOS = Arrays.asList("COMPRAS", "VENTAS", "INVENTARIO", "PRODUCCION", "DISTRIBUCION", "MANTENIMIENTO");

    @FXML
    public void initialize() {
        String usuario = SessionManager.getUsuario();
        String rol = SessionManager.getRol();

        if (usuario != null) {
            lblUsuario.setText(usuario);
        }
        if (rol != null) {
            lblRol.setText("Rol: " + rol);
        }

        List<String> modulosPermitidos = ROL_MODULOS.get(rol);
        if (modulosPermitidos == null) {
            modulosPermitidos = TODOS_MODULOS;
        }

        Map<String, Button> botones = new HashMap<>();
        botones.put("COMPRAS", btnCompras);
        botones.put("VENTAS", btnVentas);
        botones.put("INVENTARIO", btnInventario);
        botones.put("PRODUCCION", btnProduccion);
        botones.put("DISTRIBUCION", btnDistribucion);
        botones.put("MANTENIMIENTO", btnMantenimiento);

        for (Map.Entry<String, Button> entry : botones.entrySet()) {
            boolean permitido = modulosPermitidos.contains(entry.getKey());
            entry.getValue().setVisible(permitido);
            entry.getValue().setManaged(permitido);
        }
    }

    private void navegarA(Object eventSource, String fxmlPath, String titulo) {
        try {
            Stage stage = (Stage) ((Node) eventSource).getScene().getWindow();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));

            if (fxmlLoader.getLocation() == null) {
                com.example.pantallas.utils.LoggerUtil.error("NO SE ENCONTR脫 EL ARCHIVO: " + fxmlPath);
                return;
            }

            boolean wasMaximized = stage.isMaximized();
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(wasMaximized);
            if (!wasMaximized) stage.centerOnScreen();
        } catch (IOException e) {
            com.example.pantallas.utils.LoggerUtil.error("Error cargando la pantalla: " + titulo);
            com.example.pantallas.utils.LoggerUtil.error("Excepci髇 detectada", e);
        }
    }

    @FXML
    private void abrirCompras(ActionEvent event) {
        navegarA(event.getSource(), "/com/example/pantallas/ProcesoCompras/CompraPrincipal.fxml", "Gesti贸n de Compras");
    }

    @FXML
    private void abrirVentas(ActionEvent event) {
        navegarA(event.getSource(), "/com/example/pantallas/ProcesoDeVenta/MainVentas.fxml", "M贸dulo de Ventas");
    }

    @FXML
    private void abrirInventario(ActionEvent event) {
        navegarA(event.getSource(), "/com/example/pantallas/ProcesoInventario/InventarioPrincipal.fxml", "Control de Inventario");
    }

    @FXML
    private void abrirProduccion(ActionEvent event) {
        navegarA(event.getSource(), "/com/example/pantallas/ProcesoProduccion/ProduccionPrincipal.fxml", "Gesti贸n de Producci贸n");
    }

    @FXML
    private void abrirDistribucion(ActionEvent event) {
        navegarA(event.getSource(), "/com/example/pantallas/ProcesoDistribucion/DistribucionPrincipal.fxml", "Sistema de Distribuci贸n");
    }

    @FXML
    private void abrirMantenimiento(ActionEvent event) {
        navegarA(event.getSource(), "/com/example/pantallas/ProcesoMantenimiento/MantenimientoPrincipal.fxml", "Mantenimiento y Equipos");
    }

    @FXML
    private void abrirDashboard(ActionEvent event) {
        navegarA(event.getSource(), "/com/example/pantallas/Dashboard/DashboardPrincipal.fxml", "Panel de Control General");
    }

    @FXML
    private void salir(ActionEvent event) {
        SessionManager.cerrarSesion();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/pantallas/Login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 420, 520);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Inicio de Sesi贸n - F谩brica de Queso");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            com.example.pantallas.utils.LoggerUtil.error("Error al volver al login: " + e.getMessage());
        }
    }
}



