package com.example.pantallas;

import com.example.pantallas.security.SecurityUtil;

public class SessionManager {

    private SessionManager() {}

    public static void iniciarSesion(String user, String rolUsuario) {
        SecurityUtil.login(0, user, rolUsuario);
    }

    public static String getUsuario() {
        return SecurityUtil.getCurrentUser();
    }

    public static String getRol() {
        return SecurityUtil.getCurrentRole();
    }

    public static void cerrarSesion() {
        SecurityUtil.logout();
    }
}
