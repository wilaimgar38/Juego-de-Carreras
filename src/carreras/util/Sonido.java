package carreras.util;

import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public final class Sonido {

    public enum Tono {
        CLIC(660.0, 0.08, 0.30, true),
        CHOQUE(120.0, 0.40, 0.60, false),
        RECOLECTAR(920.0, 0.18, 0.35, true),
        ADELANTAR(1180.0, 0.16, 0.32, true),
        CUENTA_ATRAS(560.0, 0.25, 0.40, true),
        VICTORIA(880.0, 0.70, 0.40, true),
        DERROTA(180.0, 0.80, 0.45, false);

        private final double frecuencia;
        private final double duracion;
        private final double volumen;
        private final boolean agudo;

        Tono(double frecuencia, double duracion, double volumen, boolean agudo) {
            this.frecuencia = frecuencia;
            this.duracion = duracion;
            this.volumen = volumen;
            this.agudo = agudo;
        }

        public double getFrecuencia() {
            return frecuencia;
        }

        public double getDuracion() {
            return duracion;
        }

        public double getVolumen() {
            return volumen;
        }

        public boolean isAgudo() {
            return agudo;
        }
    }

    private static final float TAXA = 22050.0f;
    private static boolean disponible = true;

    private Sonido() {
    }

    public static void tocar(Tono tono) {
        if (!disponible || tono == null) {
            return;
        }
        byte[] muestra;
        Clip clip;
        try {
            muestra = generarMuestra(tono);
            AudioFormat formato = new AudioFormat(TAXA, 16, 1, true, false);
            AudioInputStream flujo = new AudioInputStream(new ByteArrayInputStream(muestra), formato, muestra.length);
            clip = AudioSystem.getClip();
            clip.open(flujo);
        } catch (javax.sound.sampled.LineUnavailableException ex) {
            disponible = false;
            return;
        } catch (IllegalArgumentException ex) {
            disponible = false;
            return;
        } catch (SecurityException ex) {
            disponible = false;
            return;
        } catch (IllegalStateException ex) {
            disponible = false;
            return;
        } catch (java.io.IOException ex) {
            disponible = false;
            return;
        }
        reproducir(clip, tono);
    }

    private static void reproducir(final Clip clip, final Tono tono) {
        clip.setFramePosition(0);
        clip.addLineListener(new LineListener() {
            @Override
            public void update(LineEvent event) {
                if (event.getType() == LineEvent.Type.STOP) {
                    cerrar(clip);
                }
            }
        });
        clip.start();
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep((long) (tono.getDuracion() * 1000.0) + 40L);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    try {
                        clip.stop();
                        clip.close();
                    } catch (RuntimeException ex) {
                        disponible = false;
                    }
                }
            }
        }, "hilo-sonido");
        hilo.setDaemon(true);
        hilo.start();
    }

    private static void cerrar(Clip clip) {
        try {
            clip.close();
        } catch (RuntimeException ex) {
            disponible = false;
        }
    }

    public static void pitido() {
        try {
            Toolkit.getDefaultToolkit().beep();
        } catch (RuntimeException ex) {
            disponible = false;
        }
    }

    public static byte[] generarMuestra(Tono tono) {
        int muestras = Math.max(1, (int) (TAXA * tono.getDuracion()));
        byte[] datos = new byte[muestras];
        for (int i = 0; i < muestras; i++) {
            double t = i / (double) TAXA;
            double envolvente = Math.min(1.0, i / 200.0) * Math.min(1.0, (muestras - i) / 900.0);
            double valor;
            if (tono.isAgudo()) {
                valor = Math.sin(2.0 * Math.PI * tono.getFrecuencia() * t)
                        + 0.35 * Math.sin(4.0 * Math.PI * tono.getFrecuencia() * t);
            } else {
                double caida = tono.getFrecuencia() * (1.0 - 0.55 * (i / (double) muestras));
                valor = Math.sin(2.0 * Math.PI * caida * t) * 0.9;
            }
            int valorMuestra = (int) (valor * envolvente * tono.getVolumen() * Short.MAX_VALUE);
            if (valorMuestra > Short.MAX_VALUE) {
                valorMuestra = Short.MAX_VALUE;
            }
            if (valorMuestra < Short.MIN_VALUE) {
                valorMuestra = Short.MIN_VALUE;
            }
            datos[i] = (byte) (valorMuestra >> 8);
        }
        return datos;
    }

    public static boolean isDisponible() {
        return disponible;
    }
}