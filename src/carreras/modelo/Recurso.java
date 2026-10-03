package carreras.modelo;

public class Recurso {

    private final int carril;
    private double posicionY;
    private boolean activo;
    private double animacion;

    public Recurso(int carril, double posicionY) {
        this.carril = Vehiculo.limitarCarrilPublico(carril);
        this.posicionY = posicionY;
        this.activo = true;
        this.animacion = 0.0;
    }

    public void avanzar(double dt, double velocidadJugador) {
        animacion += dt * 6.0;
        posicionY += velocidadJugador * Pista.ESCALA * dt;
    }

    public boolean haSalido(double limiteInferior) {
        return posicionY > limiteInferior + 200.0;
    }

    public void recolectar() {
        activo = false;
    }

    public double obtenerCentroX(double izquierda, double anchoCarril) {
        return izquierda + (carril + 0.5) * anchoCarril;
    }

    public int getCarril() {
        return carril;
    }

    public double getPosicionY() {
        return posicionY;
    }

    public double getAnimacion() {
        return animacion;
    }

    public boolean estaActivo() {
        return activo;
    }
}