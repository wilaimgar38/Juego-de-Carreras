package carreras.modelo;

import java.util.ArrayList;
import java.util.Iterator;

public class Pista {

    public static final double ESCALA = 6.0;

    private final ArrayList<Obstaculo> obstaculos = new ArrayList<Obstaculo>();
    private final ArrayList<Recurso> recursos = new ArrayList<Recurso>();
    private double desplazamiento;
    private double anchoVista;
    private double altoVista;
    private double anchoCarril;
    private double anchura;
    private double izquierda;
    private long obstaculosGenerados;
    private long recursosGenerados;

    public Pista(double anchoVista, double altoVista) {
        redimensionar(anchoVista, altoVista);
        this.desplazamiento = 0.0;
    }

    public final void redimensionar(double anchoVista, double altoVista) {
        this.anchoVista = Math.max(320.0, anchoVista);
        this.altoVista = Math.max(240.0, altoVista);
        this.anchoCarril = Math.max(64.0, Math.min(128.0, this.anchoVista * 0.135));
        this.anchura = this.anchoCarril * Vehiculo.CARRILES;
        this.izquierda = (this.anchoVista - this.anchura) / 2.0;
    }

    public void actualizar(double dt, double velocidadJugador) {
        desplazamiento += velocidadJugador * ESCALA * dt;
        for (Iterator<Obstaculo> it = obstaculos.iterator(); it.hasNext();) {
            Obstaculo obstaculo = it.next();
            obstaculo.avanzar(dt, velocidadJugador);
            if (obstaculo.haSalido(altoVista)) {
                it.remove();
            }
        }
        for (Iterator<Recurso> it = recursos.iterator(); it.hasNext();) {
            Recurso recurso = it.next();
            recurso.avanzar(dt, velocidadJugador);
            if (recurso.haSalido(altoVista)) {
                it.remove();
            }
        }
    }

    public void agregarObstaculo(Obstaculo obstaculo) {
        if (obstaculo != null) {
            obstaculos.add(obstaculo);
            obstaculosGenerados++;
        }
    }

    public void agregarRecurso(Recurso recurso) {
        if (recurso != null) {
            recursos.add(recurso);
            recursosGenerados++;
        }
    }

    public void limpiar() {
        obstaculos.clear();
        recursos.clear();
    }

    public boolean carrilOcupado(int carril, double desde, double hasta) {
        for (Obstaculo obstaculo : obstaculos) {
            if (obstaculo.getCarril() == carril && obstaculo.getPosicionY() > desde && obstaculo.getPosicionY() < hasta) {
                return true;
            }
        }
        return false;
    }

    public int carrilesLibres(double desde, double hasta) {
        int libres = 0;
        for (int carril = 0; carril < Vehiculo.CARRILES; carril++) {
            if (!carrilOcupado(carril, desde, hasta)) {
                libres++;
            }
        }
        return libres;
    }

    public double getPosicionJugador() {
        return getPosicionJugador(0);
    }

    public double getPosicionJugador(int indice) {
        double base = altoVista - Math.max(90.0, altoVista * 0.18);
        if (indice <= 0) {
            return base;
        }
        return base - indice * Math.max(70.0, altoVista * 0.13);
    }

    public ArrayList<Obstaculo> getObstaculos() {
        return obstaculos;
    }

    public ArrayList<Recurso> getRecursos() {
        return recursos;
    }

    public double getDesplazamiento() {
        return desplazamiento;
    }

    public double getAnchoCarril() {
        return anchoCarril;
    }

    public double getAnchura() {
        return anchura;
    }

    public double getIzquierda() {
        return izquierda;
    }

    public double getAnchoVista() {
        return anchoVista;
    }

    public double getAltoVista() {
        return altoVista;
    }

    public long getObstaculosGenerados() {
        return obstaculosGenerados;
    }

    public long getRecursosGenerados() {
        return recursosGenerados;
    }
}