package carreras.modelo;

public enum ModoJuego {

    CARRERA("Carrera", "Llega a la meta antes de quedarte sin combustible", 3500.0, 0.0, true),
    INFINITO("Infinito", "Sobrevive todo lo que puedas, la pista se vuelve mas dura", Double.MAX_VALUE, 0.00055, false);

    private final String etiqueta;
    private final String descripcion;
    private final double distanciaMeta;
    private final double crecimiento;
    private final boolean tieneRivales;

    ModoJuego(String etiqueta, String descripcion, double distanciaMeta, double crecimiento, boolean tieneRivales) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
        this.distanciaMeta = distanciaMeta;
        this.crecimiento = crecimiento;
        this.tieneRivales = tieneRivales;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getDistanciaMeta() {
        return distanciaMeta;
    }

    public boolean esInfinito() {
        return this == INFINITO;
    }

    public boolean tieneRivales() {
        return tieneRivales;
    }

    public double calcularDificultad(double distancia) {
        if (!esInfinito()) {
            return 1.0;
        }
        double dificultad = 1.0 + crecimiento * distancia;
        return dificultad > 6.0 ? 6.0 : dificultad;
    }

    public static ModoJuego desdeNombre(String nombre) {
        if (nombre == null) {
            return CARRERA;
        }
        for (ModoJuego modo : values()) {
            if (modo.name().equalsIgnoreCase(nombre.trim())) {
                return modo;
            }
        }
        return CARRERA;
    }
}