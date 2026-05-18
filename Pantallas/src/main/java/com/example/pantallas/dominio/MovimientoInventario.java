package com.example.pantallas.dominio;

public class MovimientoInventario {
    private int id;
    private String producto;
    private String tipo;
    private double cantidad;
    private String unidad;
    private java.time.LocalDateTime fecha;
    private String usuario;

    public MovimientoInventario() { }

    public MovimientoInventario(int id, String producto, String tipo, double cantidad, String unidad, java.time.LocalDateTime fecha, String usuario) {
        this.id = id;
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.fecha = fecha;
        this.usuario = usuario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public java.time.LocalDateTime getFecha() { return fecha; }
    public void setFecha(java.time.LocalDateTime fecha) { this.fecha = fecha; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
