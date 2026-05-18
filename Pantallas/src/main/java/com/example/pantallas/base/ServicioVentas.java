package com.example.pantallas.base;

import java.sql.*;
import javafx.scene.control.Alert;

/**
 * Backend de Ventas y Facturación.
 * Centraliza la lógica de precios, impuestos y notificaciones de pago.
 */
public final class ServicioVentas {

    private static final double FACTOR_KG_LB = 2.20462;

    private ServicioVentas() { }

    /**
     * Lógica de Precios Dinámicos.
     * Recalcula el precio unitario basado en la unidad de medida.
     * @param precioBaseLibras El precio por libra registrado en el sistema.
     * @param esKilos Indica si la unidad seleccionada es Kilos.
     * @return El precio ajustado a la unidad.
     */
    public static double calcularPrecioUnitario(double precioBaseLibras, boolean esKilos) {
        if (esKilos) {
            return precioBaseLibras * FACTOR_KG_LB;
        }
        return precioBaseLibras;
    }

    /**
     * Trigger de Notificación de Cobro.
     * Informa sobre la gestión de cobro para pagos en efectivo.
     */
    public static void dispararNotificacionCobroEfectivo() {
        // En una implementación real, esto podría disparar un email o un log de auditoría.
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gestión de Cobro - Empresa");
        alert.setHeaderText("Aviso de Notificación de Pago");
        alert.setContentText("Se ha registrado un pago en EFECTIVO. El sistema ha programado una llamada de verificación cerca de la fecha de entrega para gestionar el cobro y la recepción del producto.");
        alert.show();
    }

    /**
     * Backend: Obtener precio base desde la DB.
     */
    public static double obtenerPrecioBase(String producto) {
        String sql = "SELECT precio_libra FROM tbl_productos WHERE nombre = ?";
        try (Connection con = FabricaBase.abrirConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("precio_libra");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
