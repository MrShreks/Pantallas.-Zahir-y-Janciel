package com.example.pantallas.dominio;

public class OrdenCompra {
    private int id;
    private String suplidor;
    private String insumo;
    private double cantidad;
    private String estado;
    private double precioUnitario;
    private String unidad;
    private java.time.LocalDate fechaPedido;
    private java.time.LocalDate fechaEntregaEsperada;
    private String notas;
    private double cantidadRecibida;

    public OrdenCompra() { }

    public OrdenCompra(int id, String suplidor, String insumo, double cantidad, String estado, double precioUnitario) {
        this.id = id;
        this.suplidor = suplidor;
        this.insumo = insumo;
        this.cantidad = cantidad;
        this.estado = estado;
        this.precioUnitario = precioUnitario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSuplidor() { return suplidor; }
    public void setSuplidor(String s) { this.suplidor = s; }
    public String getInsumo() { return insumo; }
    public void setInsumo(String s) { this.insumo = s; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double v) { this.cantidad = v; }
    public String getEstado() { return estado; }
    public void setEstado(String s) { this.estado = s; }
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double v) { this.precioUnitario = v; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String s) { this.unidad = s; }
    public java.time.LocalDate getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(java.time.LocalDate v) { this.fechaPedido = v; }
    public java.time.LocalDate getFechaEntregaEsperada() { return fechaEntregaEsperada; }
    public void setFechaEntregaEsperada(java.time.LocalDate v) { this.fechaEntregaEsperada = v; }
    public String getNotas() { return notas; }
    public void setNotas(String s) { this.notas = s; }
    public double getCantidadRecibida() { return cantidadRecibida; }
    public void setCantidadRecibida(double v) { this.cantidadRecibida = v; }
}
