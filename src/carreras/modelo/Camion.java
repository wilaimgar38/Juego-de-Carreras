package carreras.modelo;

public class Camion extends Vehiculo {

    private static final int COLOR_BASE = 0xF2A03D;
    private static final int COLOR_DETALLE = 0x2A2118;

    public Camion() {
        super(1);
    }

    public Camion(int carril) {
        super(carril);
    }

    @Override
    public String nombre() {
        return "Camion";
    }

    @Override
    public TipoVehiculo tipo() {
        return TipoVehiculo.CAMION;
    }

    @Override
    public double anchoRelativo() {
        return 0.92;
    }

    @Override
    public double factorAspecto() {
        return 2.9;
    }

    @Override
    public double velocidadMaxima() {
        return 52.0;
    }

    @Override
    public double aceleracion() {
        return 14.0;
    }

    @Override
    public double frenado() {
        return 22.0;
    }

    @Override
    public double consumo() {
        return 2.5;
    }

    @Override
    public double agilidadCarril() {
        return 2.0;
    }

    @Override
    public double saludMaxima() {
        return 160.0;
    }

    @Override
    public int colorBase() {
        return colorBasePersonalizada >= 0 ? colorBasePersonalizada : COLOR_BASE;
    }

    @Override
    public int colorDetalle() {
        return colorDetallePersonalizado >= 0 ? colorDetallePersonalizado : COLOR_DETALLE;
    }

    @Override
    public void recibirImpacto(double dano) {
        super.recibirImpacto(dano * 0.7);
    }

    @Override
    public void recibirRoce(double dano) {
        super.recibirRoce(dano * 0.7);
    }
}