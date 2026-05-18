package com.example.pantallas;

import java.time.LocalDate;

public class OrdenProduccion {
    private int id;
    private String producto;
    private int cantidadPlanificada;
    private LocalDate fechaEntrega;
    private String estado; // Pendiente, En Proceso, Finalizado

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public int getCantidadPlanificada() {
        return cantidadPlanificada;
    }

    public void setCantidadPlanificada(int cantidadPlanificada) {
        this.cantidadPlanificada = cantidadPlanificada;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public OrdenProduccion(int id, String producto, int cantidadPlanificada, LocalDate fechaEntrega, String estado) {
        this.id = id;
        this.producto = producto;
        this.cantidadPlanificada = cantidadPlanificada;
        this.fechaEntrega = fechaEntrega;
        this.estado = estado;
    }
}

