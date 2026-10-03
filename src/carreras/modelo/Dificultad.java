package carreras.modelo;

public enum Dificultad {

    FACIL("Facil", "Menos conos y rivales mas lentos", 0.65, 0.82, 0.75),
    NORMAL("Normal", "El equilibrio recomendado", 1.0, 1.0, 1.0),
    DIFICIL("Dificil", "Conos densos, rival rapido, puntos x1.4", 1.45, 1.22, 1.4);

    private final String etiqueta;
    private final String descripcion;
    private final double factorDensidad;
    private final double factorVelocidad;
    private final double factorPuntuacion;

    Dificultad(String etiqueta, String descripcion, double factorDensidad, double factorVelocidad, double factorPuntuacion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
        this.factorDensidad = factorDensidad;
        this.factorVelocidad = factorVelocidad;
        this.factorPuntuacion = factorPuntuacion;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getFactorDensidad() {
        return factorDensidad;
    }

    public double getFactorVelocidad() {
        return factorVelocidad;
    }

    public double getFactorPuntuacion() {
        return factorPuntuacion;
    }

    public static Dificultad desdeNombre(String nombre) {
        if (nombre == null) {
            return NORMAL;
        }
        for (Dificultad dificultad : values()) {
            if (dificultad.name().equalsIgnoreCase(nombre.trim())) {
                return dificultad;
            }
        }
        return NORMAL;
    }
}