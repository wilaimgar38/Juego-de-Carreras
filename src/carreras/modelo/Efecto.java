package carreras.modelo;

import java.awt.Color;

public class Efecto {

    private final double x;
    private final double y;
    private final double vx;
    private final double vy;
    private double vida;
    private final double vidaMaxima;
    private final double tamano;
    private final Color color;
    private final boolean gravedad;

    public Efecto(double x, double y, double vx, double vy, double vida, double tamano, Color color, boolean gravedad) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.vida = vida;
        this.vidaMaxima = vida;
        this.tamano = tamano;
        this.color = color;
        this.gravedad = gravedad;
    }

    public void avanzar(double dt) {
        vida -= dt;
    }

    public boolean estaVivo() {
        return vida > 0.0;
    }

    public double getAlfa() {
        if (vidaMaxima <= 0.0) {
            return 0.0;
        }
        double proporcion = vida / vidaMaxima;
        return proporcion < 0.0 ? 0.0 : proporcion;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getTamano() {
        return tamano;
    }

    public Color getColor() {
        return color;
    }

    public boolean isGravedad() {
        return gravedad;
    }
}