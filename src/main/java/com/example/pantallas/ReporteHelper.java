package com.example.pantallas;

import com.example.pantallas.services.FabricaBase;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

public class ReporteHelper {

    public static JasperPrint generarReporte(String archivoJrxml, Map<String, Object> parametros) throws Exception {
        try (Connection conn = FabricaBase.abrirConexion()) {
            InputStream reporteStream = ReporteHelper.class.getResourceAsStream("/reportes/" + archivoJrxml);
            JasperReport reporte = JasperCompileManager.compileReport(reporteStream);
            return JasperFillManager.fillReport(reporte, parametros, conn);
        }
    }

    public static void exportarAPdf(JasperPrint print, String rutaSalida) throws Exception {
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(rutaSalida));
        exporter.exportReport();
    }

    public static void generarReporteCompleto(String archivoJrxml, Map<String, Object> parametros, String rutaSalida) throws Exception {
        JasperPrint print = generarReporte(archivoJrxml, parametros);
        exportarAPdf(print, rutaSalida);
    }

    public static void generarReporteConDatos(String archivoJrxml, Map<String, Object> parametros, Collection<?> datos, String rutaSalida) throws Exception {
        InputStream reporteStream = ReporteHelper.class.getResourceAsStream("/reportes/" + archivoJrxml);
        JasperReport reporte = JasperCompileManager.compileReport(reporteStream);
        JRDataSource dataSource = new JRBeanCollectionDataSource(datos);
        JasperPrint print = JasperFillManager.fillReport(reporte, parametros, dataSource);
        exportarAPdf(print, rutaSalida);
    }
}
