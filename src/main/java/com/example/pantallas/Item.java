package com.example.pantallas;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Item {
    private final StringProperty descripcion = new SimpleStringProperty();
    private final DoubleProperty subtotal = new SimpleDoubleProperty();

    public Item(String descripcion, double subtotal) {
        this.descripcion.set(descripcion);
        this.subtotal.set(subtotal);
    }

    public String getDescripcion() { return descripcion.get(); }
    public double getSubtotal() { return subtotal.get(); }
}