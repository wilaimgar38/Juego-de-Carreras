package carreras.modelo;

public class Carro extends Vehiculo {

    private static final int COLOR_BASE = 0xE8413C;
    private static final int COLOR_DETALLE = 0x1C2430;

    public Carro() {
        super(1);
    }

    public Carro(int carril) {
        super(carril);
    }

    @Override
    public String nombre() {
        return "Carro";
    }

    @Override
    public TipoVehiculo tipo() {
        return TipoVehiculo.CARRO;
    }

    @Override
    public double anchoRelativo() {
        return 0.72;
    }

    @Override
    public double factorAspecto() {
        return 1.85;
    }

    @Override
    public double velocidadMaxima() {
        return 62.0;
    }

    @Override
    public double aceleracion() {
        return 24.0;
    }

    @Override
    public double frenado() {
        return 36.0;
    }

    @Override
    public double consumo() {
        return 1.7;
    }

    @Override
    public double agilidadCarril() {
        return 3.4;
    }

    @Override
    public double saludMaxima() {
        return 100.0;
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