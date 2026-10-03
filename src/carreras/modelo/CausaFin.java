package carreras.modelo;

public enum CausaFin {
    META("Llegaste a la meta"),
    FALLO_MECANICO("El vehiculo quedo inservible"),
    SIN_COMBUSTIBLE("Te quedaste sin combustible"),
    ABANDONO("Partida abandonada"),
    ERROR_INTERNO("Ocurrio un error inesperado");

    private final String descripcion;

    CausaFin(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}