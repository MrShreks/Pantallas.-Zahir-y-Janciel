package com.example.pantallas;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class ItemQueso {
    private final SimpleStringProperty descripcion;
    private final SimpleDoubleProperty libras;
    private final SimpleDoubleProperty precioUnitario;
    private final SimpleDoubleProperty subtotal;

    public ItemQueso(String descripcion, double libras, double precioUnitario) {
        this.descripcion = new SimpleStringProperty(descripcion);
        this.libras = new SimpleDoubleProperty(libras);
        this.precioUnitario = new SimpleDoubleProperty(precioUnitario);
        this.subtotal = new SimpleDoubleProperty(libras * precioUnitario);
    }

    public String getDescripcion() { return descripcion.get(); }
    public double getLibras() { return libras.get(); }
    public double getPrecioUnitario() { return precioUnitario.get(); }
    public double getSubtotal() { return subtotal.get(); }
}