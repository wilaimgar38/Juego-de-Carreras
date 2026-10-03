package carreras.control;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public class EntradaTeclado implements KeyListener {

    private final int[] teclasAcelerar;
    private final int[] teclasFrenar;
    private final int[] teclasCarrilIzquierda;
    private final int[] teclasCarrilDerecha;
    private final String etiqueta;
    private final Set<Integer> teclas = new HashSet<Integer>();
    private final Set<Integer> pulsacionesNuevas = new HashSet<Integer>();

    public EntradaTeclado() {
        this(new int[]{KeyEvent.VK_UP, KeyEvent.VK_W},
                new int[]{KeyEvent.VK_DOWN, KeyEvent.VK_S},
                new int[]{KeyEvent.VK_LEFT, KeyEvent.VK_A},
                new int[]{KeyEvent.VK_RIGHT, KeyEvent.VK_D},
                "P1");
    }

    public EntradaTeclado(int[] teclasAcelerar, int[] teclasFrenar,
            int[] teclasCarrilIzquierda, int[] teclasCarrilDerecha, String etiqueta) {
        if (teclasAcelerar == null || teclasAcelerar.length == 0
                || teclasFrenar == null || teclasFrenar.length == 0
                || teclasCarrilIzquierda == null || teclasCarrilIzquierda.length == 0
                || teclasCarrilDerecha == null || teclasCarrilDerecha.length == 0) {
            throw new IllegalArgumentException("Cada jugador necesita sus cuatro teclas");
        }
        this.teclasAcelerar = teclasAcelerar.clone();
        this.teclasFrenar = teclasFrenar.clone();
        this.teclasCarrilIzquierda = teclasCarrilIzquierda.clone();
        this.teclasCarrilDerecha = teclasCarrilDerecha.clone();
        this.etiqueta = etiqueta == null ? "P" : etiqueta;
    }

    public static EntradaTeclado jugadorUno() {
        return new EntradaTeclado();
    }

    public static EntradaTeclado jugadorDos() {
        return new EntradaTeclado(new int[]{KeyEvent.VK_I},
                new int[]{KeyEvent.VK_K},
                new int[]{KeyEvent.VK_J},
                new int[]{KeyEvent.VK_L},
                "P2");
    }

    @Override
    public void keyPressed(KeyEvent evento) {
        teclas.add(Integer.valueOf(evento.getKeyCode()));
        pulsacionesNuevas.add(Integer.valueOf(evento.getKeyCode()));
        evento.consume();
    }

    @Override
    public void keyReleased(KeyEvent evento) {
        teclas.remove(Integer.valueOf(evento.getKeyCode()));
        evento.consume();
    }

    @Override
    public void keyTyped(KeyEvent evento) {
        evento.consume();
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean acelerar() {
        return algunaTeclaPulsada(teclasAcelerar);
    }

    public boolean frenar() {
        return algunaTeclaPulsada(teclasFrenar);
    }

    public boolean izquierda() {
        return algunaTeclaPulsada(teclasCarrilIzquierda);
    }

    public boolean derecha() {
        return algunaTeclaPulsada(teclasCarrilDerecha);
    }

    public boolean tomarCarrilIzquierda() {
        return tomarPulsacion(teclasCarrilIzquierda);
    }

    public boolean tomarCarrilDerecha() {
        return tomarPulsacion(teclasCarrilDerecha);
    }

    private boolean algunaTeclaPulsada(int[] codigos) {
        for (int codigo : codigos) {
            if (pulsada(codigo)) {
                return true;
            }
        }
        return false;
    }

    private boolean tomarPulsacion(int[] codigos) {
        boolean nueva = false;
        for (int codigo : codigos) {
            if (pulsacionesNuevas.remove(Integer.valueOf(codigo))) {
                nueva = true;
            }
        }
        return nueva;
    }

    public boolean pausa() {
        return pulsada(KeyEvent.VK_ESCAPE) || pulsada(KeyEvent.VK_P);
    }

    public boolean salir() {
        return pulsada(KeyEvent.VK_Q);
    }

    public boolean reiniciar() {
        return pulsada(KeyEvent.VK_R);
    }

    private boolean pulsada(int codigo) {
        return teclas.contains(Integer.valueOf(codigo));
    }

    public void soltar(int codigo) {
        Integer tecla = Integer.valueOf(codigo);
        teclas.remove(tecla);
        pulsacionesNuevas.remove(tecla);
    }

    public void limpiar() {
        teclas.clear();
        pulsacionesNuevas.clear();
    }

    public int getTeclasPulsadas() {
        return teclas.size();
    }
}