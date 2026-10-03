package carreras.modelo;

import java.awt.Color;

public class Obstaculo {

    private static final double VELOCIDAD_PROPIA = 0.0;
    private static final double ANCHO_RELATIVO = 0.34;
    private static final double FACTOR_ASPECTO = 1.5;

    private final int carril;
    private final int colorBase;
    private final int colorDetalle;
    private final double anchoRelativo;
    private final double factorAspecto;
    private double posicionY;
    private boolean activo;

    private Obstaculo(int carril, int colorBase, int colorDetalle, double posicionY) {
        this.carril = carril;
        this.colorBase = colorBase;
        this.colorDetalle = colorDetalle;
        this.anchoRelativo = ANCHO_RELATIVO;
        this.factorAspecto = FACTOR_ASPECTO;
        this.posicionY = posicionY;
        this.activo = true;
    }

    public static Obstaculo crearCono(int carril, double posicionY) {
        return new Obstaculo(carril, 0xF76B1C, 0xFFFFFF, posicionY);
    }

    public void avanzar(double dt, double velocidadJugador) {
        posicionY += (velocidadJugador - VELOCIDAD_PROPIA) * Pista.ESCALA * dt;
    }

    public boolean haSalido(double limiteInferior) {
        return posicionY > limiteInferior + 260.0;
    }

    public void golpear() {
        activo = false;
    }

    public double obtenerCentroX(double izquierda, double anchoCarril) {
        return izquierda + (carril + 0.5) * anchoCarril;
    }

    public double obtenerAnchoPx(double anchoCarril) {
        return anchoRelativo * anchoCarril * 0.9;
    }

    public double obtenerAltoPx(double anchoCarril) {
        return obtenerAnchoPx(anchoCarril) * factorAspecto;
    }

    public int getCarril() {
        return carril;
    }

    public double getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(double posicionY) {
        this.posicionY = posicionY;
    }

    public boolean estaActivo() {
        return activo;
    }

    public Color obtenerColorBase() {
        return new Color(colorBase);
    }

    public Color obtenerColorDetalle() {
        return new Color(colorDetalle);
    }

    public double getAnchoRelativo() {
        return anchoRelativo;
    }

    public double getFactorAspecto() {
        return factorAspecto;
    }
}