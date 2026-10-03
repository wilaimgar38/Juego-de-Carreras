package carreras.vista;

import carreras.modelo.Configuracion;
import carreras.modelo.Dificultad;
import carreras.modelo.ExcepcionJuego;
import carreras.modelo.Modalidad;
import carreras.modelo.ModoJuego;
import carreras.modelo.TipoVehiculo;
import carreras.util.Preferencias;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PanelMenu extends JPanel {

    private static final int H_CARDS = 176;
    private static final int H_OPCION = 44;
    private static final int H_MODALIDAD = 52;
    private static final int H_CAMPO = 34;
    private static final int H_BOTON = 46;
    private static final int SEPARACION = 28;

    private final VentanaJuego ventana;
    private final Preferencias preferencias;
    private final TarjetaVehiculo[] tarjetas;
    private final Opcion[] opcionesModalidad;
    private final Opcion[] opcionesVehiculo2;
    private final Opcion[] opcionesModo;
    private final Opcion[] opcionesDificultad;
    private final JTextField campoPiloto;
    private final JTextField campoPiloto2;
    private final JCheckBox casillaSonido;
    private final JLabel etiquetaError = new JLabel(" ");
    private final BotonPersonalizado botonJugar;
    private final BotonPersonalizado botonBorrar;

    private int yTarjetas;
    private int yModalidad;
    private int yVehiculo2;
    private int yModo;
    private int yDificultad;
    private int yNombres;

    public PanelMenu(VentanaJuego ventana, Preferencias preferencias) {
        this.ventana = ventana;
        this.preferencias = preferencias;
        this.tarjetas = new TarjetaVehiculo[TipoVehiculo.values().length];
        this.opcionesModalidad = new Opcion[Modalidad.values().length];
        this.opcionesVehiculo2 = new Opcion[TipoVehiculo.values().length];
        this.opcionesModo = new Opcion[ModoJuego.values().length];
        this.opcionesDificultad = new Opcion[Dificultad.values().length];
        setLayout(null);
        setBackground(Estilos.FONDO);
        setPreferredSize(new Dimension(Estilos.ANCHO, Estilos.ALTO));

        campoPiloto = crearCampoTexto();
        campoPiloto2 = crearCampoTexto();

        casillaSonido = new JCheckBox("Efectos de sonido");
        casillaSonido.setFont(Estilos.FUENTE_SECCION);
        casillaSonido.setForeground(Estilos.TEXTO);
        casillaSonido.setOpaque(false);
        casillaSonido.setFocusPainted(false);
        casillaSonido.setSelected(true);

        crearTarjetasVehiculo();
        crearOpcionesModalidad();
        crearOpcionesVehiculo2();
        crearOpcionesModo();
        crearOpcionesDificultad();

        botonJugar = new BotonPersonalizado("INICIAR CARRERA", Estilos.ACENTO, Estilos.FONDO);
        botonJugar.addMouseListener(new RatónBoton(botonJugar, new Runnable() {
            @Override
            public void run() {
                iniciarPartida();
            }
        }));

        botonBorrar = new BotonPersonalizado("BORRAR RECORD", new Color(0x3A2230), Estilos.PELIGRO);
        botonBorrar.addMouseListener(new RatónBoton(botonBorrar, new Runnable() {
            @Override
            public void run() {
                ventana.borrarRecords();
            }
        }));

        etiquetaError.setForeground(Estilos.PELIGRO);
        etiquetaError.setFont(Estilos.FUENTE_PEQUENA);

        add(etiquetaError);
        add(campoPiloto);
        add(campoPiloto2);
        add(casillaSonido);
        add(botonJugar);
        add(botonBorrar);

        campoPiloto.setText(preferencias.get("piloto", "Piloto"));
        campoPiloto2.setText(preferencias.get("piloto2", "Piloto 2"));
        casillaSonido.setSelected(preferencias.get("sonido", "1").equals("1"));

        tarjetas[0].setSeleccionada(true);
        opcionesVehiculo2[1].setSeleccionada(true);
        opcionesModo[0].setSeleccionada(true);
        opcionesDificultad[1].setSeleccionada(true);
        opcionesModalidad[modalidadGuardada().ordinal()].setSeleccionada(true);
        reorganizar();
    }

    private Modalidad modalidadGuardada() {
        return Modalidad.desdeNombre(preferencias.get("modalidad", Modalidad.CONTRA_PC.name()));
    }

    private JTextField crearCampoTexto() {
        JTextField campo = new JTextField();
        campo.setFont(Estilos.FUENTE_SECCION);
        campo.setForeground(Estilos.TEXTO);
        campo.setBackground(new Color(0x151B25));
        campo.setCaretColor(Estilos.ACENTO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2A3444)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent evento) {
                ocultarError();
            }

            @Override
            public void removeUpdate(DocumentEvent evento) {
                ocultarError();
            }

            @Override
            public void changedUpdate(DocumentEvent evento) {
                ocultarError();
            }
        });
        return campo;
    }

    private void crearTarjetasVehiculo() {
        TipoVehiculo[] tipos = TipoVehiculo.values();
        for (int i = 0; i < tipos.length; i++) {
            final TarjetaVehiculo tarjeta = new TarjetaVehiculo(tipos[i]);
            tarjeta.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent evento) {
                    seleccionarTarjeta(tarjeta);
                }

                @Override
                public void mouseEntered(MouseEvent evento) {
                    tarjeta.setSobre(true);
                }

                @Override
                public void mouseExited(MouseEvent evento) {
                    tarjeta.setSobre(false);
                }
            });
            tarjetas[i] = tarjeta;
            add(tarjeta);
        }
    }

    private void crearOpcionesModalidad() {
        Modalidad[] modalidades = Modalidad.values();
        for (int i = 0; i < modalidades.length; i++) {
            final Modalidad modalidad = modalidades[i];
            Opcion opcion = new Opcion(modalidad.getEtiqueta(), modalidad.getDescripcion(), new Runnable() {
                @Override
                public void run() {
                    seleccionarModalidad(modalidad);
                }
            });
            opcionesModalidad[i] = opcion;
            opcion.addMouseListener(new RatónOpcion(opcion));
            add(opcion);
        }
    }

    private void crearOpcionesVehiculo2() {
        TipoVehiculo[] tipos = TipoVehiculo.values();
        for (int i = 0; i < tipos.length; i++) {
            final TipoVehiculo tipo = tipos[i];
            Opcion opcion = new Opcion(tipo.getEtiqueta(), tipo.getDescripcion(), new Runnable() {
                @Override
                public void run() {
                    seleccionarVehiculo2(tipo);
                }
            });
            opcionesVehiculo2[i] = opcion;
            opcion.addMouseListener(new RatónOpcion(opcion));
            add(opcion);
        }
    }

    private void crearOpcionesModo() {
        ModoJuego[] modos = ModoJuego.values();
        for (int i = 0; i < modos.length; i++) {
            final ModoJuego modo = modos[i];
            Opcion opcion = new Opcion(modo.getEtiqueta(), modo.getDescripcion(), new Runnable() {
                @Override
                public void run() {
                    for (Opcion otra : opcionesModo) {
                        otra.setSeleccionada(otra.getTitulo().equals(modo.getEtiqueta()));
                    }
                    repaint();
                }
            });
            opcionesModo[i] = opcion;
            opcion.addMouseListener(new RatónOpcion(opcion));
            add(opcion);
        }
    }

    private void crearOpcionesDificultad() {
        Dificultad[] dificultades = Dificultad.values();
        for (int i = 0; i < dificultades.length; i++) {
            final Dificultad dificultad = dificultades[i];
            Opcion opcion = new Opcion(dificultad.getEtiqueta(), dificultad.getDescripcion(), new Runnable() {
                @Override
                public void run() {
                    for (Opcion otra : opcionesDificultad) {
                        otra.setSeleccionada(otra.getTitulo().equals(dificultad.getEtiqueta()));
                    }
                    repaint();
                }
            });
            opcionesDificultad[i] = opcion;
            opcion.addMouseListener(new RatónOpcion(opcion));
            add(opcion);
        }
    }

    private void reorganizar() {
        boolean duo = modalidadSeleccionada().esDuo();
        int y = 115; // Mayor espacio tras el título general

        // 1. Tarjetas de Vehículo
        yTarjetas = y;
        int anchoTarjeta = 176;
        int sepTarjetas = 26;
        int totalTarjetas = tarjetas.length * anchoTarjeta + (tarjetas.length - 1) * sepTarjetas;
        int inicioTarjetas = (Estilos.ANCHO - totalTarjetas) / 2;
        for (int i = 0; i < tarjetas.length; i++) {
            tarjetas[i].setBounds(inicioTarjetas + i * (anchoTarjeta + sepTarjetas), y, anchoTarjeta, H_CARDS);
        }
        y += H_CARDS + SEPARACION;

        // 2. Opciones de Modalidad
        yModalidad = y;
        int anchoMod = 280;
        int sepMod = 20;
        int inicioMod = (Estilos.ANCHO - (anchoMod * opcionesModalidad.length + sepMod * (opcionesModalidad.length - 1))) / 2;
        for (int i = 0; i < opcionesModalidad.length; i++) {
            opcionesModalidad[i].setBounds(inicioMod + i * (anchoMod + sepMod), y, anchoMod, H_MODALIDAD);
        }
        y += H_MODALIDAD + SEPARACION;

        // 3. Vehículo Jugador 2 (Si aplica)
        yVehiculo2 = y;
        int anchoV2 = 240;
        int sepV2 = 20;
        int inicioV2 = (Estilos.ANCHO - (anchoV2 * opcionesVehiculo2.length + sepV2 * (opcionesVehiculo2.length - 1))) / 2;
        for (int i = 0; i < opcionesVehiculo2.length; i++) {
            opcionesVehiculo2[i].setBounds(inicioV2 + i * (anchoV2 + sepV2), y, anchoV2, duo ? H_OPCION : 0);
            opcionesVehiculo2[i].setVisible(duo);
        }
        if (duo) {
            y += H_OPCION + SEPARACION;
        }

        // 4. Modo de Juego
        yModo = y;
        int anchoModo = 300;
        int inicioModo = (Estilos.ANCHO - (anchoModo * opcionesModo.length + 24 * (opcionesModo.length - 1))) / 2;
        for (int i = 0; i < opcionesModo.length; i++) {
            opcionesModo[i].setBounds(inicioModo + i * (anchoModo + 24), y, anchoModo, H_OPCION);
        }
        y += H_OPCION + SEPARACION;

        // 5. Dificultad
        yDificultad = y;
        int anchoDif = 190;
        int inicioDif = (Estilos.ANCHO - (anchoDif * opcionesDificultad.length + 20 * (opcionesDificultad.length - 1))) / 2;
        for (int i = 0; i < opcionesDificultad.length; i++) {
            opcionesDificultad[i].setBounds(inicioDif + i * (anchoDif + 20), y, anchoDif, H_OPCION);
        }
        y += H_OPCION + 26;

        // 6. Nombres de Pilotos y Checkbox
        yNombres = y;
        int anchoCampo = duo ? 190 : 240;
        campoPiloto.setBounds(Estilos.ANCHO / 2 - (duo ? 380 : 250), y, anchoCampo, H_CAMPO);
        campoPiloto2.setBounds(Estilos.ANCHO / 2 - 170, y, anchoCampo, H_CAMPO);
        campoPiloto2.setVisible(duo);
        casillaSonido.setBounds(Estilos.ANCHO / 2 + 40, y, 220, H_CAMPO);
        y += H_CAMPO + 14;

        etiquetaError.setBounds(Estilos.ANCHO / 2 - 260, y, 520, 18);
        y += 18 + 8;

        int anchoBoton = 250;
        botonJugar.setBounds(Estilos.ANCHO / 2 - anchoBoton - 5, y, anchoBoton, H_BOTON);
        botonBorrar.setBounds(Estilos.ANCHO / 2 + 5, y, 180, H_BOTON);
    }

    private void seleccionarModalidad(Modalidad modalidad) {
        for (Opcion opcion : opcionesModalidad) {
            opcion.setSeleccionada(opcion.getTitulo().equals(modalidad.getEtiqueta()));
        }
        reorganizar();
        repaint();
    }

    private void seleccionarVehiculo2(TipoVehiculo tipo) {
        for (Opcion opcion : opcionesVehiculo2) {
            opcion.setSeleccionada(opcion.getTitulo().equals(tipo.getEtiqueta()));
        }
        repaint();
    }

    private void seleccionarTarjeta(TarjetaVehiculo elegida) {
        for (TarjetaVehiculo tarjeta : tarjetas) {
            tarjeta.setSeleccionada(tarjeta == elegida);
        }
        repaint();
    }

    Modalidad modalidadSeleccionada() {
        for (Opcion opcion : opcionesModalidad) {
            if (opcion.isSeleccionada()) {
                for (Modalidad modalidad : Modalidad.values()) {
                    if (modalidad.getEtiqueta().equals(opcion.getTitulo())) {
                        return modalidad;
                    }
                }
            }
        }
        return Modalidad.CONTRA_PC;
    }

    TipoVehiculo vehiculoSeleccionado() {
        for (TarjetaVehiculo tarjeta : tarjetas) {
            if (tarjeta.isSeleccionada()) {
                return tarjeta.getTipo();
            }
        }
        return TipoVehiculo.CARRO;
    }

    TipoVehiculo vehiculo2Seleccionado() {
        for (Opcion opcion : opcionesVehiculo2) {
            if (opcion.isSeleccionada()) {
                for (TipoVehiculo tipo : TipoVehiculo.values()) {
                    if (tipo.getEtiqueta().equals(opcion.getTitulo())) {
                        return tipo;
                    }
                }
            }
        }
        return TipoVehiculo.MOTO;
    }

    ModoJuego modoSeleccionado() {
        for (Opcion opcion : opcionesModo) {
            if (opcion.isSeleccionada()) {
                for (ModoJuego modo : ModoJuego.values()) {
                    if (modo.getEtiqueta().equals(opcion.getTitulo())) {
                        return modo;
                    }
                }
            }
        }
        return ModoJuego.CARRERA;
    }

    Dificultad dificultadSeleccionada() {
        for (Opcion opcion : opcionesDificultad) {
            if (opcion.isSeleccionada()) {
                for (Dificultad dificultad : Dificultad.values()) {
                    if (dificultad.getEtiqueta().equals(opcion.getTitulo())) {
                        return dificultad;
                    }
                }
            }
        }
        return Dificultad.NORMAL;
    }

    private void iniciarPartida() {
        try {
            TipoVehiculo tipo = vehiculoSeleccionado();
            TipoVehiculo tipo2 = vehiculo2Seleccionado();
            ModoJuego modo = modoSeleccionado();
            Modalidad modalidad = modalidadSeleccionada();
            Dificultad dificultad = dificultadSeleccionada();
            boolean sonido = casillaSonido.isSelected();
            Configuracion configuracion = new Configuracion(tipo, tipo2, modo, modalidad, dificultad,
                    sonido, campoPiloto.getText(), campoPiloto2.getText());
            preferencias.set("vehiculo", tipo.name());
            preferencias.set("vehiculo2", tipo2.name());
            preferencias.set("modo", modo.name());
            preferencias.set("modalidad", modalidad.name());
            preferencias.set("dificultad", dificultad.name());
            preferencias.set("piloto", configuracion.getNombrePiloto());
            preferencias.set("piloto2", configuracion.getNombrePiloto2());
            preferencias.set("sonido", sonido ? "1" : "0");
            preferencias.guardar();
            ocultarError();
            ventana.iniciarPartida(configuracion);
        } catch (ExcepcionJuego ex) {
            mostrarError(ex.getMessage() == null ? "Datos de partida invalidos" : ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarError("Ocurrio un error al preparar la carrera");
        }
    }

    private void mostrarError(String mensaje) {
        etiquetaError.setText(mensaje);
        repaint();
    }

    private void ocultarError() {
        etiquetaError.setText(" ");
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Estilos.suavizar(g2);
        try {
            g2.setPaint(Estilos.degradadoVertical(0, 0, Estilos.ALTO, new Color(0x101826), Estilos.FONDO));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(Estilos.ACENTO.getRed(), Estilos.ACENTO.getGreen(),
                    Estilos.ACENTO.getBlue(), 34));
            for (int i = 0; i < 5; i++) {
                g2.fillOval(-120 + i * 260, Estilos.ALTO - 170, 420, 420);
            }

            // Título principal y subtítulo centrados con posiciones seguras
            Estilos.textoCentro(g2, "GRAN RALLY TURBO", Estilos.FUENTE_TITULO, Estilos.TEXTO, getWidth() / 2, 45);
            Estilos.textoCentro(g2, "Elige vehiculo, numero de jugadores, duracion y dificultad",
                    Estilos.FUENTE_SUBTITULO, Estilos.TEXTO_TENUE, getWidth() / 2, 70);

            // Etiquetas de sección posicinadas correctamente según sus bloques
            seccion(g2, "1 - VEHICULO DEL JUGADOR 1", yTarjetas - 12);
            seccion(g2, "2 - MODO DE JUGADORES", yModalidad - 12);
            if (modalidadSeleccionada().esDuo()) {
                seccion(g2, "3 - VEHICULO DEL JUGADOR 2", yVehiculo2 - 12);
            }
            seccion(g2, "4 - DURACION DE LA CARRERA", yModo - 12);
            seccion(g2, "5 - DIFICULTAD", yDificultad - 12);
            seccion(g2, "6 - NOMBRES DE LOS PILOTOS", yNombres - 12);

            dibujarMarcador(g2);
            Estilos.textoCentro(g2, "Controles: " + modalidadSeleccionada().getTeclas(),
                    Estilos.FUENTE_PEQUENA, new Color(0xC9D2DC), getWidth() / 2, Estilos.ALTO - 14);
        } catch (RuntimeException ex) {
            g2.setColor(Estilos.PELIGRO);
            g2.drawString("Error en el menu: " + ex.getMessage(), 20, 40);
        } finally {
            g2.dispose();
        }
    }

    private void seccion(Graphics2D g, String titulo, double y) {
        Estilos.textoCentro(g, titulo, Estilos.FUENTE_SECCION, Estilos.ACENTO, getWidth() / 2, y);
    }

    private void dibujarMarcador(Graphics2D g) {
        int[] puntos = ventana == null ? new int[]{0, 0} : ventana.getRecords();
        Estilos.textoDerecha(g, "Record Carrera: " + puntos[0] + " pts", Estilos.FUENTE_PEQUENA,
                Estilos.TEXTO_TENUE, getWidth() - 20, 24);
        Estilos.textoDerecha(g, "Record Infinito: " + puntos[1] + " pts", Estilos.FUENTE_PEQUENA,
                Estilos.TEXTO_TENUE, getWidth() - 20, 42);
    }

    private static final class RatónBoton extends MouseAdapter {

        private final BotonPersonalizado boton;
        private final Runnable accion;

        RatónBoton(BotonPersonalizado boton, Runnable accion) {
            this.boton = boton;
            this.accion = accion;
        }

        @Override
        public void mousePressed(MouseEvent evento) {
            accion.run();
        }

        @Override
        public void mouseEntered(MouseEvent evento) {
            boton.setSobre(true);
        }

        @Override
        public void mouseExited(MouseEvent evento) {
            boton.setSobre(false);
        }
    }

    private static final class RatónOpcion extends MouseAdapter {

        private final Opcion opcion;

        RatónOpcion(Opcion opcion) {
            this.opcion = opcion;
        }

        @Override
        public void mousePressed(MouseEvent evento) {
            opcion.seleccionar();
        }

        @Override
        public void mouseEntered(MouseEvent evento) {
            opcion.setSobre(true);
        }

        @Override
        public void mouseExited(MouseEvent evento) {
            opcion.setSobre(false);
        }
    }

    static final class TarjetaVehiculo extends JComponent {

        private static final String[] ETIQUETAS = {"VEL", "ACC", "RES"};

        private final TipoVehiculo tipo;
        private final double[] estadisticas;
        private boolean seleccionada;
        private boolean sobre;

        TarjetaVehiculo(TipoVehiculo tipo) {
            this.tipo = tipo;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            this.estadisticas = calcularEstadisticas(tipo);
        }

        private static double[] calcularEstadisticas(TipoVehiculo tipo) {
            double[] valores = new double[3];
            try {
                carreras.modelo.Vehiculo vehiculo = tipo.crear();
                valores[0] = vehiculo.velocidadMaxima() / 70.0;
                valores[1] = vehiculo.aceleracion() / 30.0;
                valores[2] = vehiculo.saludMaxima() / 170.0;
            } catch (IllegalStateException ex) {
                valores[0] = 0.35;
                valores[1] = 0.35;
                valores[2] = 0.35;
            }
            for (int i = 0; i < valores.length; i++) {
                valores[i] = Math.max(0.1, Math.min(1.0, valores[i]));
            }
            return valores;
        }

        TipoVehiculo getTipo() {
            return tipo;
        }

        boolean isSeleccionada() {
            return seleccionada;
        }

        void setSeleccionada(boolean valor) {
            this.seleccionada = valor;
            repaint();
        }

        void setSobre(boolean valor) {
            this.sobre = valor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Estilos.suavizar(g2);
            try {
                g2.setColor(seleccionada ? new Color(0x18293A) : new Color(0x111823));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                if (sobre && !seleccionada) {
                    g2.setColor(new Color(0x1B2739));
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 16, 16);
                }
                g2.setColor(seleccionada ? Estilos.ACENTO : new Color(0x2A3444));
                g2.setStroke(new BasicStroke(seleccionada ? 3.5f : 1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
                g2.setStroke(new BasicStroke(1f));

                Dibujante.vistaPrevia(g2, tipo, 0, 4, getWidth(), 100);
                Estilos.textoCentro(g2, tipo.getEtiqueta().toUpperCase(), Estilos.FUENTE_SECCION,
                        seleccionada ? Estilos.ACENTO : Estilos.TEXTO, getWidth() / 2, 124);
                Estilos.textoCentro(g2, tipo.getDescripcion(), Estilos.FUENTE_PEQUENA,
                        Estilos.TEXTO_TENUE, getWidth() / 2, 140);

                Color[] colores = {Estilos.ACENTO, Estilos.ACENTO_ALT, Estilos.COMBUSTIBLE};
                g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
                for (int i = 0; i < estadisticas.length; i++) {
                    int y = 146 + i * 8;
                    int x = 18;
                    int ancho = getWidth() - 36;
                    g2.setColor(Estilos.TEXTO_TENUE);
                    g2.drawString(ETIQUETAS[i], x, y + 5);
                    int xBarra = x + 22;
                    int anchoBarra = ancho - 22;
                    g2.setColor(new Color(0x2A3444));
                    g2.fillRoundRect(xBarra, y, anchoBarra, 5, 4, 4);
                    g2.setColor(colores[i]);
                    g2.fillRoundRect(xBarra, y, (int) (anchoBarra * estadisticas[i]), 5, 4, 4);
                }
            } catch (RuntimeException ex) {
                g2.setColor(Estilos.PELIGRO);
                g2.drawString("?", 10, 20);
            } finally {
                g2.dispose();
            }
        }
    }

    static final class Opcion extends JComponent {

        private final String titulo;
        private final String descripcion;
        private final Runnable accion;
        private boolean seleccionada;
        private boolean sobre;

        Opcion(String titulo, String descripcion, Runnable accion) {
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.accion = accion;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(descripcion);
        }

        void seleccionar() {
            if (accion != null) {
                accion.run();
            } else {
                setSeleccionada(true);
            }
        }

        String getTitulo() {
            return titulo;
        }

        boolean isSeleccionada() {
            return seleccionada;
        }

        void setSeleccionada(boolean valor) {
            this.seleccionada = valor;
            repaint();
        }

        void setSobre(boolean valor) {
            this.sobre = valor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getHeight() <= 0 || getWidth() <= 0) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            Estilos.suavizar(g2);
            try {
                g2.setColor(seleccionada ? new Color(0x16342A) : new Color(0x111823));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                if (sobre && !seleccionada) {
                    g2.setColor(new Color(0x18222F));
                    g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 13, 13);
                }
                g2.setColor(seleccionada ? Estilos.ACENTO : new Color(0x2A3444));
                g2.setStroke(new BasicStroke(seleccionada ? 3f : 1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
                g2.setStroke(new BasicStroke(1f));
                Estilos.textoCentro(g2, titulo.toUpperCase(), Estilos.FUENTE_SECCION,
                        seleccionada ? Estilos.ACENTO : Estilos.TEXTO, getWidth() / 2,
                        getHeight() >= 50 ? 24 : getHeight() / 2 + 5);
                if (getHeight() >= 50) {
                    Estilos.textoCentro(g2, descripcion, Estilos.FUENTE_PEQUENA, Estilos.TEXTO_TENUE,
                            getWidth() / 2, 42);
                }
            } catch (RuntimeException ex) {
                g2.setColor(Estilos.PELIGRO);
                g2.drawString("?", 8, 16);
            } finally {
                g2.dispose();
            }
        }
    }

    static final class BotonPersonalizado extends JComponent {

        private final String titulo;
        private final Color fondo;
        private final Color colorTexto;
        private boolean sobre;

        BotonPersonalizado(String titulo, Color fondo, Color colorTexto) {
            this.titulo = titulo;
            this.fondo = fondo;
            this.colorTexto = colorTexto;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setSobre(boolean valor) {
            this.sobre = valor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Estilos.suavizar(g2);
            try {
                g2.setColor(sobre ? fondo.brighter() : fondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(255, 255, 255, 46));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 16, 16);
                Estilos.textoCentro(g2, titulo, Estilos.FUENTE_BOTON, colorTexto,
                        getWidth() / 2, getHeight() / 2 + 6);
            } catch (RuntimeException ex) {
                g2.setColor(Estilos.PELIGRO);
                g2.drawString("ERROR", 10, 20);
            } finally {
                g2.dispose();
            }
        }
    }
}