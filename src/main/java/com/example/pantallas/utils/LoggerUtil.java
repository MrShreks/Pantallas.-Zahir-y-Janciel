package com.example.pantallas.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtil {
    private static final Logger LOGGER = Logger.getLogger("SistemaEmpresarial");

    private LoggerUtil() {}

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warning(String message) {
        LOGGER.warning(message);
    }

    public static void error(String message) {
        LOGGER.severe(message);
    }

    public static void error(String message, Throwable t) {
        LOGGER.log(Level.SEVERE, message, t);
    }
}
