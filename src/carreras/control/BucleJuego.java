package carreras.control;

import carreras.modelo.Controles;
import carreras.modelo.Juego;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BucleJuego implements Runnable {

    public static final int FOTOGRAMAS = 60;
    public static final double PASO = 1.0 / FOTOGRAMAS;

    private final Juego juego;
    private final List<EntradaTeclado> teclados;
    private final Runnable alTerminar;
    private final List<Runnable> alPintar = new ArrayList<Runnable>();
    private volatile boolean activo;
    private volatile boolean terminado;
    private Thread hilo;

    public BucleJuego(Juego juego, EntradaTeclado teclado, Runnable alTerminar) {
        this(juego, Collections.singletonList(teclado), alTerminar);
    }

    public BucleJuego(Juego juego, List<EntradaTeclado> teclados, Runnable alTerminar) {
        if (juego == null || teclados == null || teclados.isEmpty()) {
            throw new IllegalArgumentException("El bucle necesita el juego y el teclado");
        }
        this.juego = juego;
        this.teclados = new ArrayList<EntradaTeclado>(teclados);
        this.alTerminar = alTerminar;
    }

    public void addAlPintar(Runnable accion) {
        if (accion != null) {
            alPintar.add(accion);
        }
    }

    public synchronized void iniciar() {
        if (activo) {
            return;
        }
        activo = true;
        terminado = false;
        hilo = new Thread(this, "bucle-juego");
        hilo.setDaemon(true);
        hilo.start();
    }

    public synchronized void detener() {
        activo = false;
        if (hilo != null) {
            hilo.interrupt();
            hilo = null;
        }
    }

    public boolean isActivo() {
        return activo;
    }

    public int getNumeroTeclados() {
        return teclados.size();
    }

    private Controles[] leerControles() {
        int cuantos = Math.max(juego.getNumeroJugadores(), teclados.size());
        Controles[] controles = new Controles[cuantos];
        for (int i = 0; i < cuantos; i++) {
            if (i >= teclados.size()) {
                controles[i] = Controles.NADA;
                continue;
            }
            EntradaTeclado teclado = teclados.get(i);
            controles[i] = new Controles(teclado.acelerar(), teclado.frenar(),
                    teclado.tomarCarrilIzquierda(), teclado.tomarCarrilDerecha());
        }
        return controles;
    }

    public boolean isTerminado() {
        return terminado;
    }

    @Override
    public void run() {
        long anterior = System.nanoTime();
        double acumulador = 0.0;
        try {
            while (activo) {
                long ahora = System.nanoTime();
                double dt = (ahora - anterior) / 1000000000.0;
                anterior = ahora;
                if (dt > 0.25) {
                    dt = 0.25;
                }
                acumulador += dt;
                int pasos = 0;
while (acumulador >= PASO && pasos < 6) {
                    juego.actualizar(PASO, leerControles());
                    acumulador -= PASO;
                    pasos++;
                }
                if (pasos >= 6) {
                    acumulador = 0.0;
                }
                pintar();
                if (juego.isFinalizado()) {
                    terminado = true;
                    break;
                }
                Thread.sleep(8L);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException ex) {
            terminado = true;
        } finally {
            activo = false;
            if (alTerminar != null) {
                try {
                    alTerminar.run();
                } catch (RuntimeException ex) {
                    System.err.println("Error al cerrar el bucle de juego: " + ex.getMessage());
                }
            }
        }
    }

    private void pintar() {
        for (Runnable accion : alPintar) {
            try {
                accion.run();
            } catch (RuntimeException ex) {
                System.err.println("Error al repintar: " + ex.getMessage());
            }
        }
    }
}