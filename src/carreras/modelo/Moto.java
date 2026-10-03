package carreras.modelo;

public class Moto extends Vehiculo {

    private static final int COLOR_BASE = 0x2F7BE8;
    private static final int COLOR_DETALLE = 0x14181F;

    public Moto() {
        super(1);
    }

    public Moto(int carril) {
        super(carril);
    }

    @Override
    public String nombre() {
        return "Moto";
    }

    @Override
    public TipoVehiculo tipo() {
        return TipoVehiculo.MOTO;
    }

    @Override
    public double anchoRelativo() {
        return 0.42;
    }

    @Override
    public double factorAspecto() {
        return 1.7;
    }

    @Override
    public double velocidadMaxima() {
        return 68.0;
    }

    @Override
    public double aceleracion() {
        return 28.0;
    }

    @Override
    public double frenado() {
        return 30.0;
    }

    @Override
    public double consumo() {
        return 1.0;
    }

    @Override
    public double agilidadCarril() {
        return 4.8;
    }

    @Override
    public double saludMaxima() {
        return 75.0;
    }

    @Override
    public int colorBase() {
        return colorBasePersonalizada >= 0 ? colorBasePersonalizada : COLOR_BASE;
    }

    @Override
    public int colorDetalle() {
        return colorDetallePersonalizado >= 0 ? colorDetallePersonalizado : COLOR_DETALLE;
    }
}