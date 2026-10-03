package carreras.modelo;

import java.util.concurrent.ThreadLocalRandom;

public class Rival extends Vehiculo {

    public static final double ESCALA_IA = 1.7;

    private final int colorBase;
    private final int colorDetalle;
    private final double velocidadObjetivo;
    private final String nombreRival;
    private double ventaja;
    private boolean adelantado;
    private double temporizadorDecision;

    public Rival(int carril, double velocidadObjetivo, int colorBase, int colorDetalle, String nombreRival) {
        super(carril);
        this.velocidadObjetivo = velocidadObjetivo;
        this.colorBase = colorBase;
        this.colorDetalle = colorDetalle;
        this.nombreRival = nombreRival;
        this.ventaja = 0.0;
        this.adelantado = false;
        this.temporizadorDecision = 0.0;
        setVelocidad(velocidadObjetivo * 0.7);
    }

    public void actualizarIA(double dt, Pista pista) {
        if (pista == null || dt <= 0) {
            return;
        }
        double y = obtenerPosicionPantalla(pista);
        double anchoCarril = pista.getAnchoCarril();

        for (Obstaculo obstaculo : pista.getObstaculos()) {
            if (!obstaculo.estaActivo() || obstaculo.getCarril() != getCarril()) {
                continue;
            }
            double altoObstaculo = obstaculo.obtenerAltoPx(anchoCarril);
            double altoRival = obtenerAltoPx(anchoCarril);
            if (y < obstaculo.getPosicionY() + altoObstaculo && y + altoRival > obstaculo.getPosicionY()) {
                setVelocidad(getVelocidad() * 0.35);
                break;
            }
        }

        if (detienePeligro(pista, y)) {
            int destino = buscarCarrilLibre(pista, y);
            if (destino != getCarril()) {
                irACarril(destino);
            }
        } else {
            temporizadorDecision -= dt;
            if (temporizadorDecision <= 0) {
                temporizadorDecision = 2.5 + ThreadLocalRandom.current().nextDouble() * 3.5;
                if (ThreadLocalRandom.current().nextDouble() < 0.35) {
                    irACarril(getCarrilObjetivo() + (ThreadLocalRandom.current().nextBoolean() ? 1 : -1));
                }
            }
        }

        actualizarFisica(dt, false, false, false, false);

        if (getVelocidad() < velocidadObjetivo) {
            setVelocidad(getVelocidad() + aceleracion() * dt);
        } else {
            setVelocidad(getVelocidad() - frenado() * 0.5 * dt);
        }
    }

    public void setVentajaInicial(double metros) {
        this.ventaja = metros;
    }

public void avanzar(double dt, double velocidadJugador) {
        double diferencia = getVelocidad() - velocidadJugador;
        ventaja += diferencia * dt;
        if (!adelantado && ventaja < 0.0) {
            adelantado = true;
        }
    }

    public double obtenerPosicionPantalla(Pista pista) {
        return pista.getPosicionJugador() - ventaja * ESCALA_IA;
    }

    private boolean detienePeligro(Pista pista, double y) {
        double limite = y - 240.0;
        for (Obstaculo obstaculo : pista.getObstaculos()) {
            if (!obstaculo.estaActivo() || obstaculo.getCarril() != getCarril()) {
                continue;
            }
            if (obstaculo.getPosicionY() < limite) {
                continue;
            }
            if (obstaculo.getPosicionY() > y + 60.0) {
                continue;
            }
            return true;
        }
        return false;
    }

    private int buscarCarrilLibre(Pista pista, double y) {
        int[] orden = {getCarril() - 1, getCarril() + 1, getCarril() - 2, getCarril() + 2};
        for (int candidato : orden) {
            if (candidato < 0 || candidato > Vehiculo.CARRILES - 1) {
                continue;
            }
            boolean ocupado = false;
            for (Obstaculo obstaculo : pista.getObstaculos()) {
                if (!obstaculo.estaActivo() || obstaculo.getCarril() != candidato) {
                    continue;
                }
                if (obstaculo.getPosicionY() < y - 300.0) {
                    continue;
                }
                if (obstaculo.getPosicionY() > y + 80.0) {
                    continue;
                }
                ocupado = true;
                break;
            }
            if (!ocupado) {
                return candidato;
            }
        }
        return getCarril();
    }

    @Override
    public String nombre() {
        return nombreRival;
    }

    @Override
    public TipoVehiculo tipo() {
        return TipoVehiculo.CARRO;
    }

    @Override
    public double anchoRelativo() {
        return 0.7;
    }

    @Override
    public double factorAspecto() {
        return 1.85;
    }

    @Override
    public double velocidadMaxima() {
        return velocidadObjetivo * 1.25;
    }

    @Override
    public double aceleracion() {
        return 16.0;
    }

    @Override
    public double frenado() {
        return 22.0;
    }

    @Override
    public double consumo() {
        return 0.0;
    }

    @Override
    public double agilidadCarril() {
        return 2.4;
    }

    @Override
    public double saludMaxima() {
        return 100.0;
    }

    @Override
    public int colorBase() {
        return colorBase;
    }

    @Override
    public int colorDetalle() {
        return colorDetalle;
    }

    public double getVentaja() {
        return ventaja;
    }

    public boolean isAdelantado() {
        return adelantado;
    }

    public double getVelocidadObjetivo() {
        return velocidadObjetivo;
    }
}