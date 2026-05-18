package com.example.pantallas.models;

import javafx.beans.property.*;

public class Producto {
    private final IntegerProperty idProducto;
    private final StringProperty codigoProducto;
    private final StringProperty nombreProducto;
    private final StringProperty descripcion;
    private final IntegerProperty idCategoria;
    private final StringProperty unidadMedida;
    private final DoubleProperty stockActual;
    private final DoubleProperty precioVentaBase;

    public Producto() {
        this.idProducto = new SimpleIntegerProperty();
        this.codigoProducto = new SimpleStringProperty();
        this.nombreProducto = new SimpleStringProperty();
        this.descripcion = new SimpleStringProperty();
        this.idCategoria = new SimpleIntegerProperty();
        this.unidadMedida = new SimpleStringProperty();
        this.stockActual = new SimpleDoubleProperty();
        this.precioVentaBase = new SimpleDoubleProperty();
    }

    public Producto(int id, String codigo, String nombre, String desc, int idCat, String unidad, double stock, double precio) {
        this.idProducto = new SimpleIntegerProperty(id);
        this.codigoProducto = new SimpleStringProperty(codigo);
        this.nombreProducto = new SimpleStringProperty(nombre);
        this.descripcion = new SimpleStringProperty(desc);
        this.idCategoria = new SimpleIntegerProperty(idCat);
        this.unidadMedida = new SimpleStringProperty(unidad);
        this.stockActual = new SimpleDoubleProperty(stock);
        this.precioVentaBase = new SimpleDoubleProperty(precio);
    }

    public int getIdProducto() { return idProducto.get(); }
    public void setIdProducto(int value) { idProducto.set(value); }
    public IntegerProperty idProductoProperty() { return idProducto; }

    public String getCodigoProducto() { return codigoProducto.get(); }
    public void setCodigoProducto(String value) { codigoProducto.set(value); }
    public StringProperty codigoProductoProperty() { return codigoProducto; }

    public String getNombreProducto() { return nombreProducto.get(); }
    public void setNombreProducto(String value) { nombreProducto.set(value); }
    public StringProperty nombreProductoProperty() { return nombreProducto; }

    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String value) { descripcion.set(value); }
    public StringProperty descripcionProperty() { return descripcion; }

    public int getIdCategoria() { return idCategoria.get(); }
    public void setIdCategoria(int value) { idCategoria.set(value); }
    public IntegerProperty idCategoriaProperty() { return idCategoria; }

    public String getUnidadMedida() { return unidadMedida.get(); }
    public void setUnidadMedida(String value) { unidadMedida.set(value); }
    public StringProperty unidadMedidaProperty() { return unidadMedida; }

    public double getStockActual() { return stockActual.get(); }
    public void setStockActual(double value) { stockActual.set(value); }
    public DoubleProperty stockActualProperty() { return stockActual; }

    public double getPrecioVentaBase() { return precioVentaBase.get(); }
    public void setPrecioVentaBase(double value) { precioVentaBase.set(value); }
    public DoubleProperty precioVentaBaseProperty() { return precioVentaBase; }

    @Override
    public String toString() {
        return nombreProducto.get() + " (" + codigoProducto.get() + ")";
    }
}

