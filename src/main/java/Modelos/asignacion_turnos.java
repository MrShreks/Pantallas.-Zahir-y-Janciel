package Modelos;

import java.util.Date;

public class asignacion_turnos {

private int asignacion_id;
private int empleado_id;
private int turno_id;
private Date fecha;

    public int getAsignacion_id() {
        return asignacion_id;
    }

    public void setAsignacion_id(int asignacion_id) {
        this.asignacion_id = asignacion_id;
    }

    public int getEmpleado_id() {
        return empleado_id;
    }

    public void setEmpleado_id(int empleado_id) {
        this.empleado_id = empleado_id;
    }

    public int getTurno_id() {
        return turno_id;
    }

    public void setTurno_id(int turno_id) {
        this.turno_id = turno_id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public asignacion_turnos(int asignacion_id, int empleado_id, int turno_id, Date fecha) {
        this.asignacion_id = asignacion_id;
        this.empleado_id = empleado_id;
        this.turno_id = turno_id;
        this.fecha = fecha;
    }
}
