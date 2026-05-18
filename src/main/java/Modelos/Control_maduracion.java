package Modelos;

import java.util.Date;

public class Control_maduracion {

    private int control_id;
    private int lote_maduracion_id;
    private String temperatura;
    private String humedad;
    private Date fecha;

    public int getControl_id() {
        return control_id;
    }

    public void setControl_id(int control_id) {
        this.control_id = control_id;
    }

    public int getLote_maduracion_id() {
        return lote_maduracion_id;
    }

    public void setLote_maduracion_id(int lote_maduracion_id) {
        this.lote_maduracion_id = lote_maduracion_id;
    }

    public String getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }

    public String getHumedad() {
        return humedad;
    }

    public void setHumedad(String humedad) {
        this.humedad = humedad;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Control_maduracion(int control_id, int lote_maduracion_id, String temperatura, String humedad, Date fecha) {
        this.control_id = control_id;
        this.lote_maduracion_id = lote_maduracion_id;
        this.temperatura = temperatura;
        this.humedad = humedad;
        this.fecha = fecha;
    }
}
