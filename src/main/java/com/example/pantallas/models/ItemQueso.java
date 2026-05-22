package com.example.pantallas.models;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class ItemQueso {
    private final SimpleStringProperty descripcion;
    private final SimpleDoubleProperty cantidad;
    private final SimpleDoubleProperty precioUnitario;
    private final SimpleDoubleProperty subtotal;
    private final SimpleStringProperty unidadMedida;

    public ItemQueso(String descripcion, double cantidad, double precioUnitario, String unidadMedida) {
        this.descripcion = new SimpleStringProperty(descripcion);
        this.cantidad = new SimpleDoubleProperty(cantidad);
        this.precioUnitario = new SimpleDoubleProperty(precioUnitario);
        this.subtotal = new SimpleDoubleProperty(cantidad * precioUnitario);
        this.unidadMedida = new SimpleStringProperty(unidadMedida);
    }

    public String getDescripcion() { return descripcion.get(); }
    public double getCantidad() { return cantidad.get(); }
    public double getPrecioUnitario() { return precioUnitario.get(); }
    public double getSubtotal() { return subtotal.get(); }
    public String getUnidadMedida() { return unidadMedida.get(); }
    public void setCantidad(double c) { cantidad.set(c); recalcular(); }
    public void setPrecioUnitario(double p) { precioUnitario.set(p); recalcular(); }
    private void recalcular() { subtotal.set(cantidad.get() * precioUnitario.get()); }

    public double getLibras() { return cantidad.get(); }
    public void setLibras(double l) { setCantidad(l); }
}
