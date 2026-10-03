package carreras.modelo;

public final class Controles {

    public static final Controles NADA = new Controles(false, false, false, false);

    private final boolean acelerar;
    private final boolean frenar;
    private final boolean carrilIzquierda;
    private final boolean carrilDerecha;

    public Controles(boolean acelerar, boolean frenar, boolean carrilIzquierda, boolean carrilDerecha) {
        this.acelerar = acelerar;
        this.frenar = frenar;
        this.carrilIzquierda = carrilIzquierda;
        this.carrilDerecha = carrilDerecha;
    }

    public boolean isAcelerar() {
        return acelerar;
    }

    public boolean isFrenar() {
        return frenar;
    }

    public boolean isCarrilIzquierda() {
        return carrilIzquierda;
    }

    public boolean isCarrilDerecha() {
        return carrilDerecha;
    }

    public boolean estaQuieto() {
        return !acelerar && !frenar && !carrilIzquierda && !carrilDerecha;
    }
}