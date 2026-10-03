package carreras.vista;

import carreras.modelo.Configuracion;
import carreras.modelo.EventoJuego;
import carreras.modelo.ExcepcionJuego;
import carreras.modelo.Juego;
import carreras.modelo.Marcador;
import carreras.modelo.ModoJuego;
import carreras.modelo.ObservableJuego;
import carreras.util.Preferencias;
import carreras.util.Sonido;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

public class VentanaJuego extends JFrame implements ObservableJuego {

    private static final String TARJETA_MENU = "menu";
    private static final String TARJETA_JUEGO = "juego";
    private static final String TARJETA_RESULTADO = "resultado";

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel raiz = new JPanel(tarjetas);
    private final PanelMenu menu;
    private final PanelResultado resultado;
    private final Marcador marcador;
    private final Preferencias preferencias;
    private PanelJuego panelJuego;
    private Configuracion configuracionActual;
    private boolean sonidoActivo = true;
    private Timer temporizadorResultado;

    public VentanaJuego() {
        super("Gran Rally Turbo - Carreras");
        this.preferencias = new Preferencias(Paths.get("preferencias.dat"));
        this.marcador = new Marcador(Paths.get("puntajes.dat"));
        this.menu = new PanelMenu(this, preferencias);
        this.resultado = new PanelResultado(this);
        this.sonidoActivo = preferencias.get("sonido", "1").equals("1");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        raiz.setBackground(Estilos.FONDO);
        raiz.add(menu, TARJETA_MENU);
        raiz.add(resultado, TARJETA_RESULTADO);
        getContentPane().setBackground(Estilos.FONDO);
        setContentPane(raiz);
        setMinimumSize(new Dimension(Estilos.ANCHO, Estilos.ALTO));
        setPreferredSize(new Dimension(Estilos.ANCHO + 16, Estilos.ALTO + 39));
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                cerrar();
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                menu.setVisible(true);
                menu.repaint();
                instalarAtajosGlobales();
            }
        });
    }

    public Juego getJuegoActual() {
        return panelJuego == null ? null : panelJuego.getJuego();
    }

    public void iniciarPartida(Configuracion configuracion) {
        if (configuracion == null) {
            mostrarError("No hay configuracion de partida");
            return;
        }
        try {
            detenerPartida();
            this.configuracionActual = configuracion;
            this.sonidoActivo = configuracion.isSonido();
            Juego juego = new Juego(configuracion);
            panelJuego = new PanelJuego(juego, this);
            raiz.add(panelJuego, TARJETA_JUEGO);
            tarjetas.show(raiz, TARJETA_JUEGO);
            panelJuego.iniciar();
            panelJuego.requestFocusInWindow();
        } catch (ExcepcionJuego ex) {
            mostrarError(ex.getMessage() == null ? "No se pudo iniciar la carrera" : ex.getMessage());
            mostrarMenu();
        } catch (RuntimeException ex) {
            mostrarError("Fallo inesperado al iniciar la carrera");
            mostrarMenu();
        }
    }

    public void reintentar() {
        if (configuracionActual == null) {
            mostrarMenu();
            return;
        }
        iniciarPartida(configuracionActual);
    }

    public void mostrarMenu() {
        detenerPartida();
        tarjetas.show(raiz, TARJETA_MENU);
        menu.repaint();
    }

    public void mostrarResultado() {
        if (panelJuego == null || panelJuego.getJuego() == null) {
            mostrarMenu();
            return;
        }
        Juego juego = panelJuego.getJuego();
        boolean record = false;
        for (int i = 0; i < juego.getNumeroJugadores(); i++) {
            record |= marcador.registrar(claveRecord(juego, i), juego.getPuntosEnteros(i),
                    juego.getJugador(i).getNombrePiloto());
        }
        resultado.mostrar(juego, record);
        tarjetas.show(raiz, TARJETA_RESULTADO);
        resultado.repaint();
    }

    private String claveRecord(Juego juego, int indiceJugador) {
        return juego.getConfiguracion().getModo().name() + "-"
                + juego.getConfiguracion().getModalidad().name() + "-"
                + juego.getJugador(indiceJugador).getNombrePiloto().toUpperCase();
    }

    public int getRecordDe(Juego juego, int indiceJugador) {
        if (juego == null) {
            return 0;
        }
        return marcador.getMejor(claveRecord(juego, indiceJugador));
    }

    public void borrarRecords() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "Se borraran los records guardados. Continuar?", "Borrar records",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmacion == JOptionPane.YES_OPTION) {
            marcador.reiniciar();
            JOptionPane.showMessageDialog(this, "Records borrados", "Carreras",
                    JOptionPane.INFORMATION_MESSAGE);
            menu.repaint();
        }
    }

    public int[] getRecords() {
        int carrera = 0;
        int infinito = 0;
        for (Map.Entry<String, Integer> entrada : marcador.getMejores().entrySet()) {
            String clave = entrada.getKey();
            int valor = entrada.getValue() == null ? 0 : entrada.getValue().intValue();
            if (clave.startsWith(ModoJuego.CARRERA.name())) {
                carrera = Math.max(carrera, valor);
            } else if (clave.startsWith(ModoJuego.INFINITO.name())) {
                infinito = Math.max(infinito, valor);
            }
        }
        return new int[]{carrera, infinito};
    }

    public void reproducirEvento(EventoJuego evento) {
        if (!sonidoActivo || evento == null) {
            return;
        }
        try {
            switch (evento) {
                case GOLPE:
                    Sonido.tocar(Sonido.Tono.CHOQUE);
                    break;
                case RECOLECTADO:
                    Sonido.tocar(Sonido.Tono.RECOLECTAR);
                    break;
                case ADELANTAMIENTO:
                    Sonido.tocar(Sonido.Tono.ADELANTAR);
                    break;
                case CUENTA_ATRAS:
                    Sonido.tocar(Sonido.Tono.CUENTA_ATRAS);
                    break;
                case VICTORIA:
                    Sonido.tocar(Sonido.Tono.VICTORIA);
                    break;
                case DERROTA:
                    Sonido.tocar(Sonido.Tono.DERROTA);
                    break;
                default:
                    Sonido.tocar(Sonido.Tono.CLIC);
                    break;
            }
        } catch (RuntimeException ex) {
            sonidoActivo = false;
        }
    }

    @Override
    public void alSucceder(EventoJuego evento, double intensidad) {
        reproducirEvento(evento);
        if (evento == EventoJuego.VICTORIA || evento == EventoJuego.DERROTA) {
            programarResultado();
        }
    }

    private void programarResultado() {
        if (temporizadorResultado != null) {
            return;
        }
        temporizadorResultado = new Timer(1600, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evento) {
                temporizadorResultado = null;
                mostrarResultado();
            }
        });
        temporizadorResultado.setRepeats(false);
        temporizadorResultado.start();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Carreras", JOptionPane.WARNING_MESSAGE);
    }

    private void detenerPartida() {
        if (temporizadorResultado != null) {
            temporizadorResultado.stop();
            temporizadorResultado = null;
        }
        final PanelJuego panelAnterior = panelJuego;
        if (panelAnterior != null) {
            panelAnterior.detener();
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (panelAnterior != null) {
                    raiz.remove(panelAnterior);
                }
                revalidate();
                repaint();
            }
        });
    }

    private void cerrar() {
        try {
            if (panelJuego != null) {
                panelJuego.detener();
            }
            marcador.guardar();
            preferencias.guardar();
        } catch (RuntimeException ex) {
            System.err.println("Error al cerrar: " + ex.getMessage());
        }
        dispose();
        System.exit(0);
    }

    public void instalarAtajosGlobales() {
        getRootPane().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evento) {
                if (evento.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (resultado.isVisible()) {
                        reintentar();
                    } else if (menu.isVisible()) {
                        iniciarPartida(configuracionPorDefecto());
                    }
                }
                if (evento.getKeyCode() == KeyEvent.VK_M && !resultado.isVisible()) {
                    mostrarMenu();
                }
            }
        });
    }

    private Configuracion configuracionPorDefecto() {
        try {
            return Configuracion.desdePreferencias(preferencias);
        } catch (ExcepcionJuego ex) {
            mostrarError("Revisa las preferencias guardadas");
            return null;
        }
    }
}