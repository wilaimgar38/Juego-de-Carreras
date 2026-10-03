package carreras.modelo;

public enum Modalidad {

    DOS_JUGADORES("2 Jugadores", "Dos pilotos en el mismo teclado, sin PC", 2, 0),
    CONTRA_PC("1 vs PC", "Un piloto contra un vehiculo de la PC", 1, 1),
    EQUIPO_VS_PC("2 Jugadores + PC", "Dos pilotos contra un vehiculo de la PC", 2, 1);

    private final String etiqueta;
    private final String descripcion;
    private final int jugadoresHumanos;
    private final int rivales;

    Modalidad(String etiqueta, String descripcion, int jugadoresHumanos, int rivales) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
        this.jugadoresHumanos = jugadoresHumanos;
        this.rivales = rivales;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getJugadoresHumanos() {
        return jugadoresHumanos;
    }

    public int getRivales() {
        return rivales;
    }

    public boolean esDuo() {
        return jugadoresHumanos > 1;
    }

    public boolean tieneRivales() {
        return rivales > 0;
    }

    public String getTeclas() {
        return esDuo()
                ? "P1: flechas/W-A-S-D   P2: I-K-L-J"
                : "Flechas o W-A-S-D   ESC pausa";
    }

    public static Modalidad desdeNombre(String nombre) {
        if (nombre != null) {
            for (Modalidad modalidad : values()) {
                if (modalidad.name().equals(nombre)) {
                    return modalidad;
                }
            }
        }
        return CONTRA_PC;
    }
}