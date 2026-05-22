package com.example.pantallas.models;

import javafx.beans.property.*;

public class Usuario {
    private final IntegerProperty id;
    private final StringProperty nombreUsuario;
    private final StringProperty contrasena;
    private final StringProperty rol;

    public Usuario() {
        this.id = new SimpleIntegerProperty();
        this.nombreUsuario = new SimpleStringProperty();
        this.contrasena = new SimpleStringProperty();
        this.rol = new SimpleStringProperty();
    }

    public Usuario(int id, String nombreUsuario, String contrasena, String rol) {
        this.id = new SimpleIntegerProperty(id);
        this.nombreUsuario = new SimpleStringProperty(nombreUsuario);
        this.contrasena = new SimpleStringProperty(contrasena);
        this.rol = new SimpleStringProperty(rol);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getNombreUsuario() { return nombreUsuario.get(); }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario.set(nombreUsuario); }
    public StringProperty nombreUsuarioProperty() { return nombreUsuario; }

    public String getContrasena() { return contrasena.get(); }
    public void setContrasena(String contrasena) { this.contrasena.set(contrasena); }
    public StringProperty contrasenaProperty() { return contrasena; }

    public String getRol() { return rol.get(); }
    public void setRol(String rol) { this.rol.set(rol); }
    public StringProperty rolProperty() { return rol; }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id.get() +
                ", nombreUsuario='" + nombreUsuario.get() + '\'' +
                ", rol='" + rol.get() + '\'' +
                '}';
    }
}

