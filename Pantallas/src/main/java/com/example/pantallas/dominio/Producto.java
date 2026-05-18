package com.example.pantallas.dominio;

import javafx.beans.property.*;

public class Producto {
    private final IntegerProperty id;
    private final StringProperty nombre;
    private final StringProperty categoria;
    private final DoubleProperty cantidadStock;
    private final DoubleProperty cantidadMinima;
    private final StringProperty unidadMedida;
    private final DoubleProperty ultimoCosto;
    private final StringProperty proveedor;
    private final SimpleObjectProperty<java.time.LocalDateTime> fechaActualizacion;

    public Producto() {
        this.id = new SimpleIntegerProperty();
        this.nombre = new SimpleStringProperty();
        this.categoria = new SimpleStringProperty("Materia Prima");
        this.cantidadStock = new SimpleDoubleProperty(0.0);
        this.cantidadMinima = new SimpleDoubleProperty(10.0);
        this.unidadMedida = new SimpleStringProperty("Und");
        this.ultimoCosto = new SimpleDoubleProperty(0.0);
        this.proveedor = new SimpleStringProperty();
        this.fechaActualizacion = new SimpleObjectProperty<>();
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getNombre() { return nombre.get(); }
    public StringProperty nombreProperty() { return nombre; }
    public void setNombre(String nombre) { this.nombre.set(nombre); }

    public String getCategoria() { return categoria.get(); }
    public void setCategoria(String categoria) { this.categoria.set(categoria); }

    public double getCantidadStock() { return cantidadStock.get(); }
    public DoubleProperty cantidadStockProperty() { return cantidadStock; }
    public void setCantidadStock(double v) { this.cantidadStock.set(v); }

    public double getCantidadMinima() { return cantidadMinima.get(); }
    public void setCantidadMinima(double v) { this.cantidadMinima.set(v); }

    public String getUnidadMedida() { return unidadMedida.get(); }
    public void setUnidadMedida(String v) { this.unidadMedida.set(v); }

    public double getUltimoCosto() { return ultimoCosto.get(); }
    public void setUltimoCosto(double v) { this.ultimoCosto.set(v); }

    public String getProveedor() { return proveedor.get(); }
    public void setProveedor(String v) { this.proveedor.set(v); }

    public java.time.LocalDateTime getFechaActualizacion() { return fechaActualizacion.get(); }
    public void setFechaActualizacion(java.time.LocalDateTime v) { this.fechaActualizacion.set(v); }
}
