package carreras.modelo;

public class ExcepcionJuego extends Exception {

    private static final long serialVersionUID = 1L;

    public ExcepcionJuego(String mensaje) {
        super(mensaje);
    }

    public ExcepcionJuego(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}