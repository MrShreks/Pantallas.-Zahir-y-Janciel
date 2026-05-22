package com.example.pantallas;

import java.util.HashMap;
import java.util.Map;

public class EjemploReporte {

    public static void main(String[] args) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("titulo", "Reporte de Ejemplo");
            parametros.put("fecha", new java.util.Date());

            String reporteJrxml = "miReporte.jrxml";
            String salidaPdf = "reporte_salida.pdf";

            ReporteHelper.generarReporteCompleto(reporteJrxml, parametros, salidaPdf);
            com.example.pantallas.utils.LoggerUtil.info("Reporte generado: " + salidaPdf);

        } catch (Exception e) {
            com.example.pantallas.utils.LoggerUtil.error("Excepción detectada", e);
        }
    }
}
