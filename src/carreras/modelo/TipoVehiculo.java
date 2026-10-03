package carreras.modelo;

public enum TipoVehiculo {

    CARRO("Carro", "Rapido y equilibrado", 1.0),
    MOTO("Moto", "Muy agil y economica, poca resistencia", 1.15),
    CAMION("Camion", "Lento pero muy resistente", 0.9);

    private final String etiqueta;
    private final String descripcion;
    private final double factorPuntuacion;

    TipoVehiculo(String etiqueta, String descripcion, double factorPuntuacion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
        this.factorPuntuacion = factorPuntuacion;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getFactorPuntuacion() {
        return factorPuntuacion;
    }

    public Vehiculo crear() {
        switch (this) {
            case CARRO:
                return new Carro();
            case MOTO:
                return new Moto();
            case CAMION:
                return new Camion();
            default:
                throw new IllegalStateException("Tipo de vehiculo no soportado: " + this);
        }
    }

    public static TipoVehiculo desdeNombre(String nombre) {
        if (nombre == null) {
            return CARRO;
        }
        for (TipoVehiculo tipo : values()) {
            if (tipo.name().equalsIgnoreCase(nombre.trim())) {
                return tipo;
            }
        }
        return CARRO;
    }
}