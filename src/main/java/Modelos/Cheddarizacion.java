package Modelos;

public class Cheddarizacion {

    private int id_cheddarizacion;
    private int id_lote;
    private int tiempo;
    private int temperatura;

    public int getId_cheddarizacion() {
        return id_cheddarizacion;
    }

    public void setId_cheddarizacion(int id_cheddarizacion) {
        this.id_cheddarizacion = id_cheddarizacion;
    }

    public int getId_lote() {
        return id_lote;
    }

    public void setId_lote(int id_lote) {
        this.id_lote = id_lote;
    }

    public int getTiempo() {
        return tiempo;
    }

    public void setTiempo(int tiempo) {
        this.tiempo = tiempo;
    }

    public int getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(int temperatura) {
        this.temperatura = temperatura;
    }

    public Cheddarizacion(int id_cheddarizacion, int id_lote, int tiempo, int temperatura) {
        this.id_cheddarizacion = id_cheddarizacion;
        this.id_lote = id_lote;
        this.tiempo = tiempo;
        this.temperatura = temperatura;
    }
}
