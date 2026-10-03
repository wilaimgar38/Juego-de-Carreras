package carreras.modelo;

import java.util.concurrent.ThreadLocalRandom;

public class Generador {

    private static final double APARICION_CIMA = -220.0;

    private final Pista pista;
    private final Dificultad dificultad;
    private final ModoJuego modo;
    private double temporizadorObstaculo;
    private double temporizadorRecurso;
    private double dificultadDinamica;

    public Generador(Pista pista, Dificultad dificultad, ModoJuego modo) {
        if (pista == null || dificultad == null || modo == null) {
            throw new IllegalArgumentException("El generador necesita pista, dificultad y modo validos");
        }
        this.pista = pista;
        this.dificultad = dificultad;
        this.modo = modo;
        this.temporizadorObstaculo = 1.2;
        this.temporizadorRecurso = 6.0;
        this.dificultadDinamica = dificultad.getFactorDensidad();
    }

    public void actualizar(double dt, double velocidadJugador, double distancia) {
        if (dt <= 0 || pista == null) {
            return;
        }
        try {
            dificultadDinamica = modo.calcularDificultad(distancia) * dificultad.getFactorDensidad();
            double factorVelocidad = Math.max(0.35, velocidadJugador / 55.0);
            temporizadorObstaculo -= dt * factorVelocidad;
            if (temporizadorObstaculo <= 0) {
                generarObstaculo();
                temporizadorObstaculo = calcularIntervalo();
            }
            temporizadorRecurso -= dt;
            if (temporizadorRecurso <= 0) {
                generarRecurso();
                temporizadorRecurso = 8.0 + ThreadLocalRandom.current().nextDouble() * 7.0;
            }
        } catch (IllegalArgumentException ex) {
            pista.limpiar();
            temporizadorObstaculo = 2.0;
            temporizadorRecurso = 6.0;
        } catch (IllegalStateException ex) {
            pista.limpiar();
            temporizadorObstaculo = 2.0;
        }
    }

    private double calcularIntervalo() {
        double base = 1.55 - 0.085 * dificultadDinamica;
        double variacion = ThreadLocalRandom.current().nextDouble() * 0.75;
        return Math.max(0.55, base + variacion);
    }

    private void generarObstaculo() {
        int carril = elegirCarrilLibre();
        if (carril < 0) {
            return;
        }
        pista.agregarObstaculo(Obstaculo.crearCono(carril, APARICION_CIMA - 120.0));
        if (dificultadDinamica > 2.6 && ThreadLocalRandom.current().nextDouble() < 0.3) {
            int segundoCarril = elegirCarrilLibre(carril);
            if (segundoCarril >= 0 && pista.carrilesLibres(APARICION_CIMA, 0.0) > 2) {
                pista.agregarObstaculo(Obstaculo.crearCono(segundoCarril, APARICION_CIMA - 320.0));
            }
        }
    }

    private void generarRecurso() {
        int carril = elegirCarrilLibre();
        if (carril >= 0) {
            pista.agregarRecurso(new Recurso(carril, APARICION_CIMA - 60.0));
        }
    }

    private int elegirCarrilLibre() {
        return elegirCarrilLibre(-1);
    }

    private int elegirCarrilLibre(int excepto) {
        int[] libres = new int[Vehiculo.CARRILES];
        int cantidad = 0;
        for (int carril = 0; carril < Vehiculo.CARRILES; carril++) {
            if (carril == excepto) {
                continue;
            }
            if (!pista.carrilOcupado(carril, APARICION_CIMA - 200.0, 40.0)) {
                libres[cantidad] = carril;
                cantidad++;
            }
        }
        if (cantidad == 0) {
            return -1;
        }
        return libres[ThreadLocalRandom.current().nextInt(cantidad)];
    }

    public double getDificultadDinamica() {
        return dificultadDinamica;
    }
}