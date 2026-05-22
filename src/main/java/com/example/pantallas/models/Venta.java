package com.example.pantallas.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Venta {
    private final IntegerProperty idVenta;
    private final ObjectProperty<LocalDate> fecha;
    private final StringProperty cliente;
    private final StringProperty producto;
    private final IntegerProperty cantidad;
    private final DoubleProperty precioUnitario;
    private final StringProperty vendedor;

    public Venta() {
        this.idVenta = new SimpleIntegerProperty();
        this.fecha = new SimpleObjectProperty<>();
        this.cliente = new SimpleStringProperty();
        this.producto = new SimpleStringProperty();
        this.cantidad = new SimpleIntegerProperty();
        this.precioUnitario = new SimpleDoubleProperty();
        this.vendedor = new SimpleStringProperty();
    }

    public Venta(int idVenta, LocalDate fecha, String cliente, String producto, int cantidad, double precioUnitario, String vendedor) {
        this();
        this.idVenta.set(idVenta);
        this.fecha.set(fecha);
        this.cliente.set(cliente);
        this.producto.set(producto);
        this.cantidad.set(cantidad);
        this.precioUnitario.set(precioUnitario);
        this.vendedor.set(vendedor);
    }

    public int getIdVenta() { return idVenta.get(); }
    public void setIdVenta(int value) { idVenta.set(value); }
    public IntegerProperty idVentaProperty() { return idVenta; }

    public LocalDate getFecha() { return fecha.get(); }
    public void setFecha(LocalDate value) { fecha.set(value); }
    public ObjectProperty<LocalDate> fechaProperty() { return fecha; }

    public String getCliente() { return cliente.get(); }
    public void setCliente(String value) { cliente.set(value); }
    public StringProperty clienteProperty() { return cliente; }

    public String getProducto() { return producto.get(); }
    public void setProducto(String value) { producto.set(value); }
    public StringProperty productoProperty() { return producto; }

    public int getCantidad() { return cantidad.get(); }
    public void setCantidad(int value) { cantidad.set(value); }
    public IntegerProperty cantidadProperty() { return cantidad; }

    public double getPrecioUnitario() { return precioUnitario.get(); }
    public void setPrecioUnitario(double value) { precioUnitario.set(value); }
    public DoubleProperty precioUnitarioProperty() { return precioUnitario; }

    public String getVendedor() { return vendedor.get(); }
    public void setVendedor(String value) { vendedor.set(value); }
    public StringProperty vendedorProperty() { return vendedor; }

    public double getTotal() {
        return getCantidad() * getPrecioUnitario();
    }
}

