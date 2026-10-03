package carreras.vista;

import carreras.modelo.Juego;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Locale;
import javax.swing.JPanel;

public class PanelResultado extends JPanel {

    private static final Color[] COLORES_JUGADOR = {new Color(0x35D6C4), new Color(0xF97316)};

    private final VentanaJuego ventana;
    private Juego juego;
    private boolean nuevoRecord;

    public PanelResultado(VentanaJuego ventana) {
        this.ventana = ventana;
        setLayout(null);
        setBackground(Estilos.FONDO);
        setPreferredSize(new Dimension(Estilos.ANCHO, Estilos.ALTO));
        PanelMenu.BotonPersonalizado reintentar = new PanelMenu.BotonPersonalizado("CORRER DE NUEVO",
                Estilos.ACENTO, Estilos.FONDO);
        reintentar.addMouseListener(new Raton(reintentar, new Runnable() {
            @Override
            public void run() {
                ventana.reintentar();
            }
        }));
        reintentar.setBounds(235, 620, 230, 52);

        PanelMenu.BotonPersonalizado menu = new PanelMenu.BotonPersonalizado("VOLVER AL MENU",
                new Color(0x1C2634), Estilos.TEXTO);
        menu.addMouseListener(new Raton(menu, new Runnable() {
            @Override
            public void run() {
                ventana.mostrarMenu();
            }
        }));
        menu.setBounds(475, 620, 230, 52);
        add(reintentar);
        add(menu);
    }

    public void mostrar(Juego juego, boolean nuevoRecord) {
        this.juego = juego;
        this.nuevoRecord = nuevoRecord;
        repaint();
    }

    public Juego getJuego() {
        return juego;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Estilos.suavizar(g2);
        try {
            g2.setPaint(Estilos.degradadoVertical(0, 0, Estilos.ALTO, new Color(0x121A28), Estilos.FONDO));
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (juego == null) {
                Estilos.textoCentro(g2, "SIN DATOS DE PARTIDA", Estilos.FUENTE_TITULO, Estilos.TEXTO,
                        getWidth() / 2, getHeight() / 2);
                return;
            }
            boolean victoria = juego.getEstado() == carreras.modelo.EstadoJuego.VICTORIA;
            Estilos.textoCentro(g2, victoria ? "VICTORIA" : "FIN DE LA PARTIDA", Estilos.FUENTE_TITULO,
                    victoria ? Estilos.ACENTO : Estilos.PELIGRO, getWidth() / 2, 96);
            Estilos.textoCentro(g2, juego.getCausaFin().getDescripcion(), Estilos.FUENTE_SECCION,
                    Estilos.TEXTO_TENUE, getWidth() / 2, 128);
            Estilos.textoCentro(g2, juego.getConfiguracion().resumen(), Estilos.FUENTE_PEQUENA,
                    Estilos.ACENTO, getWidth() / 2, 152);

            int jugadores = juego.getNumeroJugadores();
            int panelAncho = jugadores > 1 ? 640 : 600;
            int panelX = (getWidth() - panelAncho) / 2;
            Estilos.panel(g2, panelX, 176, panelAncho, 300, 22, new Color(0x0E1520), 0.85f);

            if (jugadores > 1) {
                int x = panelX + 44;
                for (int i = 0; i < jugadores; i++) {
                    dibujarJugador(g2, x + i * 300, 226, i, recordDe(i));
                }
                Estilos.textoCentro(g2, "JUGADOR 1", Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE,
                        panelX + 190, 210);
                Estilos.textoCentro(g2, "JUGADOR 2", Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE,
                        panelX + 490, 210);
                dibujarPc(g2);
                Estilos.textoCentro(g2, "ENTER repetir   M menu", Estilos.FUENTE_PEQUENA,
                        new Color(0x7C8896), getWidth() / 2, 700);
                return;
            }

            int x = panelX + 50;
            int y = 226;
            fila(g2, x, y, "Piloto", juego.getJugador(0).getNombrePiloto());
            fila(g2, x, y + 36, "Vehiculo", juego.getVehiculo(0).nombre());
            fila(g2, x, y + 72, "Modo", juego.getConfiguracion().getModo().getEtiqueta()
                    + " (" + juego.getConfiguracion().getDificultad().getEtiqueta() + ")");
            fila(g2, x, y + 108, "Distancia", String.format(Locale.US, "%.0f m", juego.getDistancia(0)));
            fila(g2, x, y + 144, "Tiempo", formatearTiempo(juego.getTiempo()));
            fila(g2, x, y + 180, "Golpes recibidos", String.valueOf(juego.getJugador(0).getGolpesRecibidos()));
            fila(g2, x, y + 216, "Adelantamientos", String.valueOf(juego.getJugador(0).getAdelantamientos()));
            fila(g2, x, y + 252, "Mejor racha", "x" + juego.getJugador(0).getMejorRacha());

            Estilos.textoCentro(g2, "PUNTUACION FINAL", Estilos.FUENTE_SECCION, Estilos.TEXTO_TENUE,
                    getWidth() / 2 + 170, 252);
            Estilos.textoCentro(g2, String.valueOf(juego.getPuntosEnteros(0)),
                    new java.awt.Font("SansSerif", java.awt.Font.BOLD, 52), Estilos.ACENTO,
                    getWidth() / 2 + 170, 312);
            Estilos.textoCentro(g2, "Record del modo: " + recordDe(0) + " pts",
                    Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, getWidth() / 2 + 170, 342);
            if (nuevoRecord) {
                Estilos.textoCentro(g2, "NUEVO RECORD", Estilos.FUENTE_HUD, Estilos.COMBUSTIBLE,
                        getWidth() / 2 + 170, 372);
            }
            dibujarPc(g2);
            Estilos.textoCentro(g2, "ENTER repetir   M menu", Estilos.FUENTE_PEQUENA,
                    new Color(0x7C8896), getWidth() / 2, 700);
        } catch (RuntimeException ex) {
            g2.setColor(Estilos.PELIGRO);
            g2.drawString("Error al mostrar el resultado: " + ex.getMessage(), 20, 40);
        } finally {
            g2.dispose();
        }
    }

    private void dibujarPc(Graphics2D g) {
        int cuantos = juego.getNumeroRivales();
        if (cuantos <= 0) {
            return;
        }
        int ancho = 640;
        int x = (getWidth() - ancho) / 2;
        int y = 500;
        Estilos.panel(g, x, y, ancho, 92 + Math.max(0, cuantos - 1) * 26, 18,
                new Color(0x0E1520), 0.85f);
        Estilos.texto(g, "VEHICULOS DE LA PC", Estilos.FUENTE_PEQUENA, new Color(0x7C8896), x + 28, y + 26);

        int columnaVehiculo = x + 28;
        int columnaPuesto = x + 250;
        int columnaDistancia = x + 360;
        int columnaEstado = x + 480;
        Estilos.texto(g, "PILOTO", Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, columnaVehiculo, y + 48);
        Estilos.texto(g, "PUESTO", Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, columnaPuesto, y + 48);
        Estilos.texto(g, "DISTANCIA", Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, columnaDistancia, y + 48);
        Estilos.texto(g, "ESTADO", Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, columnaEstado, y + 48);

        for (int i = 0; i < cuantos; i++) {
            int fila = y + 72 + i * 26;
            boolean termino = juego.rivalTermino(i);
            Estilos.texto(g, juego.getNombreRival(i), Estilos.FUENTE_SECCION, Estilos.TEXTO, columnaVehiculo, fila);
            Estilos.texto(g, juego.getPuestoRival(i) + " de " + juego.getTotalCompetidores(),
                    Estilos.FUENTE_SECCION, Estilos.ACENTO_ALT, columnaPuesto, fila);
            Estilos.texto(g, String.format(Locale.US, "%.0f m", juego.getDistanciaRival(i)),
                    Estilos.FUENTE_SECCION, Estilos.TEXTO, columnaDistancia, fila);
            Estilos.texto(g, termino ? "Llego a la meta" : "En pista",
                    Estilos.FUENTE_SECCION, termino ? Estilos.COMBUSTIBLE : Estilos.TEXTO_TENUE,
                    columnaEstado, fila);
        }
    }

    private int recordDe(int indiceJugador) {
        return ventana == null || juego == null ? 0 : ventana.getRecordDe(juego, indiceJugador);
    }

    private void dibujarJugador(Graphics2D g, int x, int y, int indice, int record) {
        Color color = COLORES_JUGADOR[Math.max(0, Math.min(indice, COLORES_JUGADOR.length - 1))];
        Estilos.texto(g, juego.getJugador(indice).getEtiqueta(), Estilos.FUENTE_SECCION, color, x, y - 22);
        fila(g, x, y, "Piloto", juego.getJugador(indice).getNombrePiloto());
        fila(g, x, y + 52, "Vehiculo", juego.getVehiculo(indice).nombre());
        fila(g, x, y + 104, "Puesto", juego.getPuesto(indice) + " de " + juego.getTotalCompetidores());
        fila(g, x, y + 156, "Distancia", String.format(Locale.US, "%.0f m", juego.getDistancia(indice)));
        fila(g, x, y + 208, "Golpes", String.valueOf(juego.getJugador(indice).getGolpesRecibidos()));
        Estilos.textoCentro(g, "PUNTOS " + juego.getPuntosEnteros(indice), Estilos.FUENTE_SECCION,
                color, x + 110, y + 250);
        Estilos.textoCentro(g, "Record " + record, Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE,
                x + 110, y + 272);
    }

    private void fila(Graphics2D g, int x, int y, String etiqueta, String valor) {
        Estilos.texto(g, etiqueta, Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE, x, y);
        Estilos.texto(g, valor, Estilos.FUENTE_SECCION, Estilos.TEXTO, x, y + 20);
    }

    private String formatearTiempo(double segundos) {
        int total = (int) Math.floor(segundos);
        return String.format(Locale.US, "%02d:%02d", total / 60, total % 60);
    }

    private static final class Raton extends java.awt.event.MouseAdapter {

        private final PanelMenu.BotonPersonalizado boton;
        private final Runnable accion;

        Raton(PanelMenu.BotonPersonalizado boton, Runnable accion) {
            this.boton = boton;
            this.accion = accion;
        }

        @Override
        public void mousePressed(java.awt.event.MouseEvent evento) {
            accion.run();
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent evento) {
            boton.setSobre(true);
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent evento) {
            boton.setSobre(false);
        }
    }
}