package carreras.modelo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public class GestorEfectos {

    private static final int MAXIMO = 260;

    private final ArrayList<Efecto> efectos = new ArrayList<Efecto>();

    public void crearChispas(double x, double y, int cantidad, Color color) {
        for (int i = 0; i < cantidad; i++) {
            double angulo = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0;
            double fuerza = 90.0 + ThreadLocalRandom.current().nextDouble() * 220.0;
            efectos.add(new Efecto(x, y, Math.cos(angulo) * fuerza, Math.sin(angulo) * fuerza - 60.0,
                    0.35 + ThreadLocalRandom.current().nextDouble() * 0.4,
                    2.0 + ThreadLocalRandom.current().nextDouble() * 3.0, color, true));
        }
        recortar();
    }

    public void crearHumo(double x, double y, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            efectos.add(new Efecto(x + ThreadLocalRandom.current().nextDouble() * 24.0 - 12.0, y,
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 40.0,
                    -40.0 - ThreadLocalRandom.current().nextDouble() * 70.0,
                    0.5 + ThreadLocalRandom.current().nextDouble() * 0.6,
                    6.0 + ThreadLocalRandom.current().nextDouble() * 8.0,
                    new Color(200, 205, 215), false));
        }
        recortar();
    }

    public void crearResplandor(double x, double y, Color color, double intensidad) {
        efectos.add(new Efecto(x, y, 0.0, 0.0, 0.45, intensidad, color, false));
        recortar();
    }

    public void actualizar(double dt) {
        for (Iterator<Efecto> it = efectos.iterator(); it.hasNext();) {
            Efecto efecto = it.next();
            efecto.avanzar(dt);
            if (!efecto.estaVivo()) {
                it.remove();
            }
        }
    }

    private void recortar() {
        while (efectos.size() > MAXIMO) {
            efectos.remove(0);
        }
    }

    public void limpiar() {
        efectos.clear();
    }

    public ArrayList<Efecto> getEfectos() {
        return efectos;
    }
}