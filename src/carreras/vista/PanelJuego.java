package carreras.vista;

import carreras.control.BucleJuego;
import carreras.control.EntradaTeclado;
import carreras.modelo.Efecto;
import carreras.modelo.EstadoJuego;
import carreras.modelo.EventoJuego;
import carreras.modelo.Juego;
import carreras.modelo.Jugador;
import carreras.modelo.ObservableJuego;
import carreras.modelo.Obstaculo;
import carreras.modelo.Recurso;
import carreras.modelo.Rival;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PanelJuego extends JPanel implements ObservableJuego {

    private static final Color[] COLORES_JUGADOR = {new Color(0x35D6C4), new Color(0xF97316)};

    private final Juego juego;
    private final VentanaJuego ventana;
    private final List<EntradaTeclado> teclados = new ArrayList<EntradaTeclado>();
    private BucleJuego bucle;
    private double faseRuedas;

    public PanelJuego(Juego juego, VentanaJuego ventana) {
        this.juego = juego;
        this.ventana = ventana;
        juego.setObservador(this);
        setPreferredSize(new Dimension(Estilos.ANCHO, Estilos.ALTO));
        setBackground(Estilos.FONDO);
        setFocusable(true);
        for (int i = 0; i < Math.max(1, juego.getNumeroJugadores()); i++) {
            EntradaTeclado teclado = i == 0 ? EntradaTeclado.jugadorUno() : EntradaTeclado.jugadorDos();
            teclados.add(teclado);
            addKeyListener(teclado);
        }
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evento) {
                manejarTecla(evento.getKeyCode());
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evento) {
                requestFocusInWindow();
            }
        });
    }

    private void manejarTecla(int codigo) {
        if (codigo == KeyEvent.VK_ESCAPE || codigo == KeyEvent.VK_P) {
            juego.alternarPausa();
            repaint();
            return;
        }
        if (codigo == KeyEvent.VK_ENTER) {
            if (juego.getEstado() == EstadoJuego.PAUSA) {
                juego.reanudar();
                repaint();
            } else if (juego.isFinalizado()) {
                ventana.mostrarResultado();
            }
            return;
        }
        if (codigo == KeyEvent.VK_M) {
            ventana.mostrarMenu();
            return;
        }
        if (codigo == KeyEvent.VK_R && juego.isFinalizado()) {
            ventana.reintentar();
        }
    }

    public void iniciar() {
        if (bucle == null) {
            bucle = new BucleJuego(juego, teclados, new Runnable() {
                @Override
                public void run() {
                    repaint();
                }
            });
            bucle.addAlPintar(new Runnable() {
                @Override
                public void run() {
                    repaint();
                }
            });
        }
        limpiarTeclados();
        juego.iniciar();
        bucle.iniciar();
        requestFocusInWindow();
    }

    public Juego getJuego() {
        return juego;
    }

    public List<EntradaTeclado> getTeclados() {
        return teclados;
    }

    public void detener() {
        if (bucle != null) {
            bucle.detener();
        }
        limpiarTeclados();
    }

    private void limpiarTeclados() {
        for (EntradaTeclado teclado : teclados) {
            teclado.limpiar();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                requestFocusInWindow();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Estilos.suavizar(g2);
            g2.setColor(Estilos.FONDO);
            g2.fillRect(0, 0, getWidth(), getHeight());
            double sacudida = juego.getSacudida();
            if (sacudida > 0.01) {
                double dx = (Math.random() - 0.5) * sacudida * 14.0;
                double dy = (Math.random() - 0.5) * sacudida * 10.0;
                g2.translate(dx, dy);
            }
            dibujarEscena(g2);
            dibujarHud(g2);
            dibujarAvisos(g2);
        } catch (RuntimeException ex) {
            g2.setColor(Estilos.PELIGRO);
            g2.drawString("Error al dibujar: " + ex.getMessage(), 20, 40);
        } finally {
            g2.dispose();
        }
    }

    private void dibujarEscena(Graphics2D g) {
        int ancho = getWidth();
        int alto = getHeight();
        juego.redimensionar(ancho, alto);
        faseRuedas += 0.35;
        double velocidadRelativa = juego.getVehiculo().obtenerVelocidadRelativa();

        Dibujante.paisaje(g, ancho, alto, juego.getDistancia(), velocidadRelativa);
        boolean hayMeta = !juego.getConfiguracion().getModo().esInfinito() && juego.getProgreso() > 0.93;
        Dibujante.carretera(g, juego.getPista(), juego.getDistancia(), hayMeta);

        ArrayList<Dibujable> orden = new ArrayList<Dibujable>();
        for (Recurso recurso : juego.getPista().getRecursos()) {
            if (recurso.estaActivo()) {
                orden.add(new Dibujable(recurso.getPosicionY(), recurso));
            }
        }
        for (Obstaculo obstaculo : juego.getPista().getObstaculos()) {
            if (obstaculo.estaActivo()) {
                orden.add(new Dibujable(obstaculo.getPosicionY(), obstaculo));
            }
        }
        for (Rival rival : juego.getRivales()) {
            double y = rival.obtenerPosicionPantalla(juego.getPista());
            if (y > -260 && y < alto + 260) {
                orden.add(new Dibujable(y, rival));
            }
        }
        for (int i = 0; i < juego.getNumeroJugadores(); i++) {
            Jugador jugador = juego.getJugador(i);
            orden.add(new Dibujable(juego.getPista().getPosicionJugador(i), jugador));
        }
        Collections.sort(orden, new Comparator<Dibujable>() {
            @Override
            public int compare(Dibujable a, Dibujable b) {
                return Double.compare(a.getY(), b.getY());
            }
        });

        for (Dibujable item : orden) {
            try {
                dibujarObjeto(g, item);
            } catch (RuntimeException ex) {
                continue;
            }
        }

        for (Efecto efecto : juego.getEfectos().getEfectos()) {
            Dibujante.efecto(g, efecto);
        }

        if (juego.getVehiculo().obtenerVelocidadRelativa() > 0.8) {
            double fuerza = (juego.getVehiculo().obtenerVelocidadRelativa() - 0.8) / 0.2;
            g.setColor(new Color(255, 255, 255, (int) (26 * fuerza)));
            for (int i = 0; i < 14; i++) {
                int x = (i * 71 + 13) % Math.max(1, ancho);
                g.fillRect(x, alto - 90 - (i * 17 % 80), 3, 60);
            }
        }

        if (juego.getVehiculo().obtenerSaludRelativa() < 0.3) {
            int intensidad = (int) (110 * (0.3 - juego.getVehiculo().obtenerSaludRelativa()) / 0.3);
            g.setColor(new Color(255, 40, 40, intensidad));
            g.fillRect(0, 0, ancho, alto);
        }
    }

    private void dibujarObjeto(Graphics2D g, Dibujable item) {
        Object objeto = item.getObjeto();
        if (objeto instanceof Obstaculo) {
            Dibujante.obstaculo(g, (Obstaculo) objeto, juego.getPista());
        } else if (objeto instanceof Recurso) {
            Dibujante.recurso(g, (Recurso) objeto, juego.getPista());
        } else if (objeto instanceof Rival) {
            Rival rival = (Rival) objeto;
            Dibujante.vehiculo(g, rival, juego.getPista(), item.getY() - rival.obtenerAltoPx(juego.getPista().getAnchoCarril()) / 2.0,
                    faseRuedas, false);
        } else if (objeto instanceof Jugador) {
            Jugador jugador = (Jugador) objeto;
            carreras.modelo.Vehiculo vehiculo = jugador.getVehiculo();
            Dibujante.vehiculo(g, vehiculo, juego.getPista(),
                    item.getY() - vehiculo.obtenerAltoPx(juego.getPista().getAnchoCarril()) / 2.0,
                    faseRuedas, true);
            dibujarEtiquetaJugador(g, jugador, item.getY(),
                    vehiculo.obtenerAltoPx(juego.getPista().getAnchoCarril()));
        } else if (objeto instanceof carreras.modelo.Vehiculo) {
            carreras.modelo.Vehiculo vehiculo = (carreras.modelo.Vehiculo) objeto;
            Dibujante.vehiculo(g, vehiculo, juego.getPista(),
                    item.getY() - vehiculo.obtenerAltoPx(juego.getPista().getAnchoCarril()) / 2.0,
                    faseRuedas, true);
        }
    }

    private void dibujarEtiquetaJugador(Graphics2D g, Jugador jugador, double y, double alto) {
        int indice = jugador.getNumero() - 1;
        Color color = COLORES_JUGADOR[Math.max(0, Math.min(indice, COLORES_JUGADOR.length - 1))];
        String texto = juego.esMultijugador() ? jugador.getEtiqueta() : "";
        if (texto.isEmpty()) {
            return;
        }
        Estilos.panel(g, (int) (jugador.obtenerCentroX(juego.getPista().getIzquierda(),
                juego.getPista().getAnchoCarril()) - 14), (int) (y - alto - 22), 28, 16, 8,
                new Color(0x05080D), 0.7f);
        Estilos.textoCentro(g, texto, Estilos.FUENTE_PEQUENA, color,
                (int) jugador.obtenerCentroX(juego.getPista().getIzquierda(), juego.getPista().getAnchoCarril()),
                y - alto - 10);
    }

    private void dibujarHud(Graphics2D g) {
        int ancho = getWidth();
        int alto = getHeight();
        for (int i = 0; i < juego.getNumeroJugadores(); i++) {
            int x = juego.esMultijugador()
                    ? (i == 0 ? 12 : ancho - 248)
                    : 12;
            dibujarPanelJugador(g, i, x, 12);
        }
        int anchoDerecha = juego.esMultijugador() ? 248 : 214;
        int xDerecha = ancho - anchoDerecha;
        Estilos.panel(g, xDerecha, 12, juego.esMultijugador() ? 236 : 202, 118, 16, new Color(0x0B1018), 0.62f);

        if (juego.esMultijugador()) {
            Estilos.textoCentro(g, "LIDER " + Math.round(juego.getProgresoLeader() * 100) + "%",
                    Estilos.FUENTE_PEQUENA, Estilos.TEXTO, ancho / 2, 40);
            Estilos.textoCentro(g, "J1 vs J2 - " + juego.getConfiguracion().getModalidad().getEtiqueta().toUpperCase(),
                    Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, ancho / 2, 26);
        } else {
            Estilos.textoDerecha(g, "PUNTOS " + juego.getPuntosEnteros(), Estilos.FUENTE_HUD, Estilos.TEXTO, ancho - 28, 46);
            Estilos.textoDerecha(g, "DISTANCIA " + Math.round(juego.getDistancia()) + " m",
                    Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, ancho - 28, 70);
            Estilos.textoDerecha(g, "TIEMPO " + formatearTiempo(juego.getTiempo()),
                    Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, ancho - 28, 90);
            Estilos.textoDerecha(g, "RACHA x" + juego.getJugador().getRacha(), Estilos.FUENTE_PEQUENA,
                    Estilos.ACENTO, ancho - 28, 110);
            Estilos.textoDerecha(g, juego.getConfiguracion().getModo().getEtiqueta().toUpperCase(),
                    Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, ancho - 28, 126);
        }

        if (!juego.getConfiguracion().getModo().esInfinito()) {
            int x = juego.esMultijugador() ? ancho / 2 - 150 : 268;
            int anchoBarra = juego.esMultijugador() ? 300 : Math.max(120, ancho - 300);
            Estilos.panel(g, x - 10, juego.esMultijugador() ? 50 : 14, anchoBarra + 20,
                    juego.esMultijugador() ? 30 : 30, 14, new Color(0x0B1018), 0.5f);
            Estilos.barra(g, x, juego.esMultijugador() ? 56 : 20, anchoBarra, 18,
                    juego.esMultijugador() ? juego.getProgresoLeader() : juego.getProgreso(), Estilos.ACENTO_ALT);
            Estilos.textoCentro(g, "PROGRESO", Estilos.FUENTE_PEQUENA, Estilos.TEXTO,
                    x + anchoBarra / 2, juego.esMultijugador() ? 69 : 33);
        } else {
            Estilos.textoCentro(g, "MODO INFINITO - dificultad x"
                            + String.format(java.util.Locale.US, "%.2f", juego.getGenerador().getDificultadDinamica()),
                    Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, ancho / 2, 30);
        }

        Estilos.textoCentro(g, juego.getConfiguracion().getModalidad().getTeclas()
                        + "   ESC pausa",
                Estilos.FUENTE_PEQUENA, new Color(0xC9D2DC), ancho / 2, alto - 14);
    }

    private void dibujarPanelJugador(Graphics2D g, int indice, int x, int y) {
        Jugador jugador = juego.getJugador(indice);
        Color color = COLORES_JUGADOR[Math.max(0, Math.min(indice, COLORES_JUGADOR.length - 1))];
        int anchoPanel = juego.esMultijugador() ? 236 : 236;
        Estilos.panel(g, x, y, anchoPanel, 118, 16, new Color(0x0B1018), 0.62f);

        Estilos.texto(g, Math.round(jugador.getVelocidadKmh()) + " km/h",
                Estilos.FUENTE_CIFRA, Estilos.TEXTO, x + 14, y + 40);
        Estilos.texto(g, juego.esMultijugador()
                        ? (jugador.getEtiqueta() + " " + recortar(jugador.getNombrePiloto()) + " - " + jugador.nombreVehiculo())
                        : "VELOCIDAD",
                Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, x + 14, y + 56);
        Estilos.barra(g, x + 14, y + 66, anchoPanel - 28, 12, jugador.obtenerVelocidadRelativa(), color);
        Estilos.texto(g, "COMBUSTIBLE", Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, x + 14, y + 92);
        Estilos.barra(g, x + 106, y + 82, anchoPanel - 120, 12, jugador.getCombustible() / 100.0, Estilos.COMBUSTIBLE);
        Estilos.texto(g, "INTEGRIDAD", Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, x + 14, y + 112);
        Estilos.barra(g, x + 106, y + 102, anchoPanel - 120, 12, jugador.getVehiculo().obtenerSaludRelativa(),
                jugador.getVehiculo().obtenerSaludRelativa() < 0.35 ? Estilos.PELIGRO : Estilos.ACENTO_ALT);

        if (juego.esMultijugador()) {
            Estilos.textoDerecha(g, Math.round(juego.getDistancia(indice)) + " m",
                    Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, x + anchoPanel - 12, y + 24);
        }
    }

    private String recortar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= 10 ? texto : texto.substring(0, 10);
    }

    private void dibujarAvisos(Graphics2D g) {
        int ancho = getWidth();
        int alto = getHeight();
        if (juego.getEstado() == EstadoJuego.CUENTA_ATRAS) {
            Estilos.panel(g, 0, 0, ancho, alto, 0, new Color(0x05080D), 0.35f);
            int numero = (int) Math.ceil(juego.getCuentaAtras());
            Estilos.textoCentro(g, String.valueOf(Math.max(1, numero)), new java.awt.Font("SansSerif",
                    java.awt.Font.BOLD, 96), Estilos.TEXTO, ancho / 2, alto / 2 + 20);
            Estilos.textoCentro(g, nombresPilotos(), Estilos.FUENTE_SECCION, Estilos.ACENTO,
                    ancho / 2, alto / 2 + 60);
            Estilos.textoCentro(g, juego.getConfiguracion().getModalidad().getTeclas(),
                    Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, ancho / 2, alto / 2 + 84);
        } else if (juego.getEstado() == EstadoJuego.PAUSA) {
            Estilos.panel(g, 0, 0, ancho, alto, 0, new Color(0x05080D), 0.68f);
            Estilos.panel(g, ancho / 2 - 200, alto / 2 - 110, 400, 220, 22, new Color(0x11161F), 0.95f);
            Estilos.textoCentro(g, "PAUSA", Estilos.FUENTE_TITULO, Estilos.TEXTO, ancho / 2, alto / 2 - 50);
            Estilos.textoCentro(g, "ESC o ENTER para continuar", Estilos.FUENTE_SECCION,
                    Estilos.TEXTO_TENUE, ancho / 2, alto / 2 - 10);
            Estilos.textoCentro(g, "M vuelve al menu", Estilos.FUENTE_PEQUENA,
                    Estilos.TEXTO_TENUE, ancho / 2, alto / 2 + 18);
            Estilos.textoCentro(g, juego.getConfiguracion().resumen(), Estilos.FUENTE_PEQUENA,
                    Estilos.ACENTO, ancho / 2, alto / 2 + 52);
            Estilos.textoCentro(g, nombresPilotos(), Estilos.FUENTE_PEQUENA,
                    Estilos.TEXTO_TENUE, ancho / 2, alto / 2 + 76);
        } else if (juego.isFinalizado()) {
            boolean victoria = juego.getEstado() == EstadoJuego.VICTORIA;
            Estilos.panel(g, 0, 0, ancho, alto, 0, new Color(0x05080D), 0.55f);
            Estilos.panel(g, ancho / 2 - 250, alto / 2 - 130, 500, 260, 24, new Color(0x11161F), 0.95f);
            Estilos.textoCentro(g, victoria ? "VICTORIA" : "FIN DE LA PARTIDA", Estilos.FUENTE_TITULO,
                    victoria ? Estilos.ACENTO : Estilos.PELIGRO, ancho / 2, alto / 2 - 74);
            Estilos.textoCentro(g, juego.getCausaFin().getDescripcion(), Estilos.FUENTE_SECCION,
                    Estilos.TEXTO, ancho / 2, alto / 2 - 40);
            for (int i = 0; i < juego.getNumeroJugadores(); i++) {
                Color color = COLORES_JUGADOR[Math.max(0, Math.min(i, COLORES_JUGADOR.length - 1))];
                Estilos.textoCentro(g, juego.getJugador(i).getNombrePiloto() + "  " + juego.getPuntosEnteros(i)
                                + " pts   " + Math.round(juego.getDistancia(i)) + " m   "
                                + juego.getCausaFin(i).getDescripcion(),
                        Estilos.FUENTE_PEQUENA, color, ancho / 2, alto / 2 + 4 + i * 24);
            }
            Estilos.textoCentro(g, "ENTER resultado   R repetir   M menu", Estilos.FUENTE_PEQUENA,
                    Estilos.ACENTO, ancho / 2, alto / 2 + 100);
        }
    }

    private String nombresPilotos() {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < juego.getNumeroJugadores(); i++) {
            if (i > 0) {
                texto.append("   VS   ");
            }
            texto.append(juego.getJugador(i).getNombrePiloto());
        }
        return texto.toString();
    }

    private String formatearTiempo(double segundos) {
        int total = (int) Math.floor(segundos);
        int minutos = total / 60;
        int resto = total % 60;
        return String.format(java.util.Locale.US, "%02d:%02d", minutos, resto);
    }

@Override
public void alSucceder(EventoJuego evento, double intensidad) {
        if (ventana == null) {
            return;
        }
        ventana.alSucceder(evento, intensidad);
    }

    private static final class Dibujable {

        private final double y;
        private final Object objeto;

        Dibujable(double y, Object objeto) {
            this.y = y;
            this.objeto = objeto;
        }

        double getY() {
            return y;
        }

        Object getObjeto() {
            return objeto;
        }
    }
}