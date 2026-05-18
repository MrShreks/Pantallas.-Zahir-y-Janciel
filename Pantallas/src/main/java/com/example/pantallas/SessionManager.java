package com.example.pantallas;

public class SessionManager {

    private static String usuario;
    private static String rol;

    public static void iniciarSesion(String usuario, String rol) {
        SessionManager.usuario = usuario;
        SessionManager.rol = (rol != null) ? rol.toUpperCase() : "USUARIO";
    }

    public static String getUsuario() {
        return usuario;
    }

    public static String getRol() {
        return rol;
    }

    public static void cerrarSesion() {
        usuario = null;
        rol = null;
    }

    public static String getUsuarioActual() {
        return usuario;
    }
}
