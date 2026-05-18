package Modelos;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Almacenes {
    private final StringProperty almacen_id = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final DoubleProperty ubicacion = new SimpleDoubleProperty();

    public Almacenes(String almacen_id, double ubicacion, String nombre) {
        this.almacen_id.set(almacen_id);
        this.ubicacion.set(ubicacion);
        this.nombre.set(nombre);
    }

    public String getAlmacen_id() { return almacen_id.get(); }
    public double getUbicacion() { return ubicacion.get(); }
    public String getNombre() { return nombre.get(); }
}