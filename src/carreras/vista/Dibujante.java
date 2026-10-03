package carreras.vista;

import carreras.modelo.Efecto;
import carreras.modelo.Obstaculo;
import carreras.modelo.Pista;
import carreras.modelo.Recurso;
import carreras.modelo.TipoVehiculo;
import carreras.modelo.Vehiculo;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public final class Dibujante {

    private Dibujante() {
    }

    public static void paisaje(Graphics2D g, int ancho, int alto, double distancia, double velocidadRelativa) {
        g.setPaint(Estilos.degradadoVertical(0, 0, alto * 0.55f, Estilos.CIELO_ALTO, Estilos.CIELO_MEDIO));
        g.fillRect(0, 0, ancho, (int) (alto * 0.58));
        g.setPaint(Estilos.degradadoVertical(0, alto * 0.35f, alto * 0.25f, Estilos.CIELO_MEDIO, Estilos.CIELO_BAJO));
        g.fillRect(0, (int) (alto * 0.34), ancho, (int) (alto * 0.26));

        double desplazamientoCielo = distancia * 0.02;
        dibujarSol(g, ancho * 0.78, alto * 0.16);
        dibujarNubes(g, ancho, alto, desplazamientoCielo);
        dibujarMontanas(g, ancho, alto, distancia * 0.05, Estilos.MONTAÑA_LEJANA, 0.34, 5);
        dibujarMontanas(g, ancho, alto, distancia * 0.12, Estilos.MONTAÑA_CERCA, 0.52, 8);

        g.setColor(new Color(0, 0, 0, 0));
        if (velocidadRelativa > 0.75) {
            double fuerza = (velocidadRelativa - 0.75) / 0.25;
            g.setColor(new Color(255, 255, 255, (int) (18 * fuerza)));
            for (int i = 0; i < 10; i++) {
                double x = (i * 97 + distancia * 3.0) % (ancho + 80.0) - 40.0;
                g.drawLine((int) x, (int) (alto * 0.55), (int) (x + 40), alto);
            }
        }
    }

    private static void dibujarSol(Graphics2D g, double x, double y) {
        for (int radio = 90; radio > 20; radio -= 14) {
            g.setColor(new Color(Estilos.SOL.getRed(), Estilos.SOL.getGreen(), Estilos.SOL.getBlue(),
                    radio > 50 ? 12 : 24));
            g.fill(new Ellipse2D.Double(x - radio, y - radio, radio * 2.0, radio * 2.0));
        }
        g.setColor(Estilos.SOL);
        g.fill(new Ellipse2D.Double(x - 20, y - 20, 40, 40));
    }

    private static void dibujarNubes(Graphics2D g, int ancho, int alto, double desplazamiento) {
        g.setColor(new Color(255, 255, 255, 40));
        for (int i = 0; i < 7; i++) {
            double baseX = i * 190.0 + desplazamiento;
            double x = ((baseX % (ancho + 260.0)) + (ancho + 260.0)) % (ancho + 260.0) - 130.0;
            double y = alto * 0.06 + (i % 3) * 26.0;
            double escala = 0.7 + (i % 4) * 0.16;
            g.fill(new Ellipse2D.Double(x, y, 110 * escala, 34 * escala));
            g.fill(new Ellipse2D.Double(x + 34 * escala, y - 14 * escala, 74 * escala, 40 * escala));
        }
    }

    private static void dibujarMontanas(Graphics2D g, int ancho, int alto, double desplazamiento,
            Color color, double alturaBase, int picos) {
        g.setColor(color);
        double paso = (double) ancho / picos;
        double desfase = ((desplazamiento % paso) + paso) % paso;
        int[] alturas = new int[picos + 2];
        for (int i = 0; i < alturas.length; i++) {
            alturas[i] = (int) (alto * alturaBase * (0.55 + ((i * 37) % 10) / 14.0));
        }
        java.awt.geom.Path2D.Double camino = new java.awt.geom.Path2D.Double();
        camino.moveTo(-paso, alto);
        for (int i = 0; i < alturas.length; i++) {
            double x = i * paso - desfase;
            camino.lineTo(x, alto - alturas[i]);
            camino.lineTo(x + paso / 2.0, alto - alturas[i] * 0.35);
        }
        camino.lineTo(ancho + paso, alto);
        camino.closePath();
        g.fill(camino);
    }

    public static void carretera(Graphics2D g, Pista pista, double distancia, boolean hayMeta) {
        int ancho = (int) pista.getAnchoVista();
        int alto = (int) pista.getAltoVista();
        double izquierda = pista.getIzquierda();
        double anchura = pista.getAnchura();
        double anchoCarril = pista.getAnchoCarril();
        double desplazamiento = pista.getDesplazamiento();

        g.setPaint(Estilos.degradadoVertical(0, alto * 0.5f, (int) (alto * 0.5),
                Estilos.CESPED, new Color(0x14361F)));
        g.fillRect(0, (int) (alto * 0.52), ancho, (int) (alto * 0.48));

        double franja = 90.0;
        double desfase = ((desplazamiento % franja) + franja) % franja;
        for (double y = alto * 0.52 - franja + desfase; y < alto; y += franja) {
            g.setColor(((int) (y / franja) % 2 == 0) ? Estilos.CESPED_ALT : Estilos.CESPED);
            g.fill(new RoundRectangle2D.Double(0, y, izquierda, franja, 0, franja));
            g.fill(new RoundRectangle2D.Double(izquierda + anchura, y, ancho - izquierda - anchura, franja, 0, franja));
        }

        g.setPaint(Estilos.degradadoVertical(0, (int) (alto * 0.5f), (int) (alto * 0.5),
                Estilos.ASFALTO_ALT, Estilos.ASFALTO));
        g.fill(new RoundRectangle2D.Double(izquierda, alto * 0.52, anchura, alto * 0.48 + 2, 18, 18));

        double bordeIzq = izquierda + 8;
        double bordeDer = izquierda + anchura - 8;
        g.setColor(new Color(0x9AA4B2));
        g.fill(new RoundRectangle2D.Double(bordeIzq - 5, alto * 0.52, 5, alto * 0.48 + 2, 4, 4));
        g.fill(new RoundRectangle2D.Double(bordeDer, alto * 0.52, 5, alto * 0.48 + 2, 4, 4));

        double trazo = 46.0;
        double hueco = 40.0;
        double ciclo = trazo + hueco;
        double desfaseLinea = ((desplazamiento % ciclo) + ciclo) % ciclo;
        g.setColor(Estilos.LINEA);
        for (int carril = 1; carril < 4; carril++) {
            double x = izquierda + carril * anchoCarril - 4;
            for (double y = alto * 0.52 - ciclo + desfaseLinea; y < alto; y += ciclo) {
                g.fill(new RoundRectangle2D.Double(x, y, 8, trazo, 6, 6));
            }
        }

        dibujarBordeVerde(g, pista, alto, desplazamiento);
        dibujarDecoracion(g, pista, alto, desplazamiento, distancia);
        if (hayMeta) {
            dibujarMeta(g, pista, alto);
        }
    }

    private static void dibujarBordeVerde(Graphics2D g, Pista pista, int alto, double desplazamiento) {
        g.setColor(new Color(255, 255, 255, 26));
        double paso = 120.0;
        double desfase = ((desplazamiento % paso) + paso) % paso;
        double izquierda = pista.getIzquierda() - 16;
        double derecha = pista.getIzquierda() + pista.getAnchura() + 10;
        for (double y = alto * 0.52 - paso + desfase; y < alto; y += paso) {
            g.fill(new RoundRectangle2D.Double(izquierda, y, 8, 60, 6, 6));
            g.fill(new RoundRectangle2D.Double(derecha, y, 8, 60, 6, 6));
        }
    }

    private static void dibujarDecoracion(Graphics2D g, Pista pista, int alto, double desplazamiento, double distancia) {
        double paso = 260.0;
        double desfase = ((desplazamiento % paso) + paso) % paso;
        double izquierda = pista.getIzquierda();
        int indice = 0;
        for (double y = alto * 0.5 - paso * 2 + desfase; y < alto + paso; y += paso) {
            double escala = 0.55 + (y / (double) alto) * 0.65;
            int tipo = (indice + (int) (distancia / 12000.0)) % 4;
            dibujarObjetoBorde(g, izquierda - 40 - escala * 26, y, escala, tipo);
            dibujarObjetoBorde(g, izquierda + pista.getAnchura() + 40 + escala * 26, y, escala, tipo + 2);
            indice++;
        }
    }

    private static void dibujarObjetoBorde(Graphics2D g, double x, double y, double escala, int tipo) {
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new Ellipse2D.Double(x - 22 * escala + 8, y + 10, 46 * escala, 16 * escala));
        switch (tipo) {
            case 0: {
                g.setColor(new Color(0x6B4A2A));
                g.fill(new RoundRectangle2D.Double(x - 5 * escala, y - 10 * escala, 10 * escala, 60 * escala, 8, 8));
                g.setColor(new Color(0x2F7D4F));
                for (int i = 0; i < 5; i++) {
                    double ax = x + (i - 2) * 13 * escala;
                    double ay = y - 16 * escala - (i % 2) * 10 * escala;
                    g.fill(new Ellipse2D.Double(ax - 16 * escala, ay - 14 * escala, 34 * escala, 26 * escala));
                }
                break;
            }
            case 1: {
                g.setColor(new Color(0x6C7480));
                g.fill(new RoundRectangle2D.Double(x - 4 * escala, y - 6 * escala, 8 * escala, 54 * escala, 6, 6));
                g.setColor(new Color(0xFFD27A));
                g.fill(new RoundRectangle2D.Double(x - 16 * escala, y - 26 * escala, 32 * escala, 18 * escala, 8, 8));
                g.setColor(new Color(255, 255, 255, 120));
                g.fill(new RoundRectangle2D.Double(x - 12 * escala, y - 22 * escala, 24 * escala, 6 * escala, 6, 6));
                break;
            }
            case 2: {
                g.setColor(new Color(0x8C94A0));
                g.fill(new RoundRectangle2D.Double(x - 6 * escala, y, 12 * escala, 50 * escala, 8, 8));
                g.setColor(new Color(0x39D98A));
                g.fill(new RoundRectangle2D.Double(x - 20 * escala, y - 34 * escala, 40 * escala, 30 * escala, 8, 8));
                g.setColor(Estilos.FONDO);
                g.setStroke(new BasicStroke(3f));
                g.draw(new RoundRectangle2D.Double(x - 14 * escala, y - 28 * escala, 28 * escala, 18 * escala, 6, 6));
                break;
            }
            default: {
                g.setColor(new Color(0x39414D));
                g.fill(new RoundRectangle2D.Double(x - 26 * escala, y + 4, 52 * escala, 14 * escala, 8, 8));
                g.setColor(new Color(0xC9D2DC));
                g.fill(new RoundRectangle2D.Double(x - 24 * escala, y, 48 * escala, 8 * escala, 8, 8));
                break;
            }
        }
        g.setStroke(new BasicStroke(1f));
    }

    private static void dibujarMeta(Graphics2D g, Pista pista, int alto) {
        double ancho = pista.getAnchura();
        double izquierda = pista.getIzquierda();
        double inicio = alto * 0.52 - 420.0;
        for (double y = inicio; y < alto * 0.52; y += 44.0) {
            int columnas = 12;
            double anchoCuadro = ancho / columnas;
            for (int i = 0; i < columnas; i++) {
                g.setColor(((i + (int) (y / 44.0)) % 2 == 0) ? Color.WHITE : Color.BLACK);
                g.fill(new RoundRectangle2D.Double(izquierda + i * anchoCuadro, y, anchoCuadro, 44, 0, 0));
            }
        }
        g.setColor(Estilos.ACENTO);
        g.fill(new RoundRectangle2D.Double(izquierda, alto * 0.52 - 470.0, ancho, 26, 12, 12));
        Estilos.textoCentro(g, "META", Estilos.FUENTE_HUD, Estilos.FONDO,
                (int) (izquierda + ancho / 2.0), alto * 0.52 - 450.0);
    }

    public static void vehiculo(Graphics2D g, Vehiculo vehiculo, Pista pista, double y,
            double faseRuedas, boolean faros) {
        double anchoCarril = pista.getAnchoCarril();
        double centro = vehiculo.obtenerCentroX(pista.getIzquierda(), anchoCarril);
        double ancho = vehiculo.obtenerAnchoPx(anchoCarril);
        double alto = vehiculo.obtenerAltoPx(anchoCarril);
        double x = centro - ancho / 2.0;
        sombra(g, x, y, ancho, alto);
        switch (vehiculo.tipo()) {
            case CAMION:
                camion(g, x, y, ancho, alto, new Color(vehiculo.colorBase()), new Color(vehiculo.colorDetalle()), faros);
                break;
            case MOTO:
                moto(g, x, y, ancho, alto, new Color(vehiculo.colorBase()), new Color(vehiculo.colorDetalle()), faros);
                break;
            default:
                carro(g, x, y, ancho, alto, new Color(vehiculo.colorBase()), new Color(vehiculo.colorDetalle()),
                        faseRuedas, faros);
                break;
        }
    }

    public static void sombra(Graphics2D g, double x, double y, double ancho, double alto) {
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new RoundRectangle2D.Double(x + 6, y + 10, ancho, alto, 14, 14));
    }

    private static void ruedas(Graphics2D g, double x, double y, double ancho, double alto, double fase, int pares) {
        g.setColor(new Color(0x11151B));
        double anchoRueda = Math.max(6, ancho * 0.16);
        double largoRueda = Math.max(10, alto * 0.13);
        double[] posiciones = pares == 2
                ? new double[]{y + alto * 0.16, y + alto * 0.72}
                : new double[]{y + alto * 0.10, y + alto * 0.38, y + alto * 0.78};
        double desfase = Math.sin(fase) * 1.2;
        for (double posicion : posiciones) {
            g.fill(new RoundRectangle2D.Double(x - anchoRueda * 0.35 + desfase, posicion, anchoRueda, largoRueda, 5, 5));
            g.fill(new RoundRectangle2D.Double(x + ancho - anchoRueda * 0.65 + desfase, posicion, anchoRueda, largoRueda, 5, 5));
        }
    }

    public static void carro(Graphics2D g, double x, double y, double ancho, double alto,
            Color base, Color detalle, double fase, boolean faros) {
        ruedas(g, x, y, ancho, alto, fase, 2);
        g.setPaint(new GradientPaint((float) x, (float) y, base.brighter(),
                (float) x, (float) (y + alto), base.darker()));
        g.fill(new RoundRectangle2D.Double(x, y, ancho, alto, ancho * 0.28, ancho * 0.28));

        g.setColor(new Color(255, 255, 255, 60));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.12, y + alto * 0.05, ancho * 0.76, alto * 0.10,
                ancho * 0.12, ancho * 0.12));

        g.setColor(new Color(0x0E1622));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.16, y + alto * 0.20, ancho * 0.68, alto * 0.17,
                ancho * 0.14, ancho * 0.14));
        g.setColor(new Color(127, 227, 255, 90));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.19, y + alto * 0.215, ancho * 0.62, alto * 0.06,
                ancho * 0.08, ancho * 0.08));

        g.setColor(detalle);
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.14, y + alto * 0.40, ancho * 0.72, alto * 0.26,
                ancho * 0.16, ancho * 0.16));
        g.setColor(new Color(255, 255, 255, 26));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.18, y + alto * 0.42, ancho * 0.64, alto * 0.05,
                ancho * 0.08, ancho * 0.08));

        g.setColor(new Color(0x0E1622));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.20, y + alto * 0.70, ancho * 0.60, alto * 0.10,
                ancho * 0.10, ancho * 0.10));

        faros(g, x, y, ancho, faros);
        g.setColor(new Color(0xE03A3A));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.10, y + alto * 0.90, ancho * 0.24, alto * 0.06, 5, 5));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.66, y + alto * 0.90, ancho * 0.24, alto * 0.06, 5, 5));

        g.setColor(new Color(0, 0, 0, 120));
        g.setStroke(new BasicStroke(Math.max(1.5f, (float) (ancho * 0.05))));
        g.draw(new RoundRectangle2D.Double(x, y, ancho, alto, ancho * 0.28, ancho * 0.28));
        g.setStroke(new BasicStroke(1f));
    }

    public static void moto(Graphics2D g, double x, double y, double ancho, double alto,
            Color base, Color detalle, boolean faros) {
        ruedas(g, x, y, ancho, alto, 0.0, 2);
        g.setPaint(new GradientPaint((float) x, (float) y, base.brighter(), (float) x, (float) (y + alto), base.darker()));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.22, y + alto * 0.05, ancho * 0.56, alto * 0.90,
                ancho * 0.3, ancho * 0.3));

        g.setColor(new Color(0x1B2027));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.02, y + alto * 0.14, ancho * 0.96, ancho * 0.14,
                6, 6));
        g.setColor(new Color(0x6C7480));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.30, y + alto * 0.40, ancho * 0.40, alto * 0.16,
                6, 6));

        g.setColor(detalle);
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.20, y + alto * 0.50, ancho * 0.60, alto * 0.26, 10, 10));
        g.setColor(new Color(0x2C3440));
        g.fill(new Ellipse2D.Double(x + ancho * 0.28, y + alto * 0.50, ancho * 0.44, ancho * 0.44));
        g.setColor(new Color(0xE8EDF3));
        g.fill(new Ellipse2D.Double(x + ancho * 0.36, y + alto * 0.56, ancho * 0.28, ancho * 0.28));
        g.setColor(new Color(57, 217, 138, 160));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.38, y + alto * 0.58, ancho * 0.24, alto * 0.10, 5, 5));

        faros(g, x, y, ancho, faros);
        g.setColor(new Color(0xE03A3A));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.34, y + alto * 0.90, ancho * 0.32, alto * 0.05, 4, 4));
    }

    public static void camion(Graphics2D g, double x, double y, double ancho, double alto,
            Color base, Color detalle, boolean faros) {
        ruedas(g, x, y, ancho, alto, 0.0, 3);
        g.setPaint(new GradientPaint((float) x, (float) y, new Color(0xE8EBEF), (float) x,
                (float) (y + alto * 0.3), new Color(0xB9C1CB)));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.06, y + alto * 0.02, ancho * 0.88, alto * 0.26,
                ancho * 0.14, ancho * 0.14));
        g.setColor(new Color(0x16202E));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.14, y + alto * 0.07, ancho * 0.72, alto * 0.10,
                ancho * 0.08, ancho * 0.08));

        g.setPaint(new GradientPaint((float) x, (float) (y + alto * 0.28), base.brighter(),
                (float) x, (float) (y + alto), base.darker()));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.03, y + alto * 0.30, ancho * 0.94, alto * 0.66,
                ancho * 0.06, ancho * 0.06));
        g.setColor(new Color(0, 0, 0, 40));
        for (int i = 1; i < 5; i++) {
            double linea = y + alto * 0.30 + (alto * 0.66 / 5.0) * i;
            g.fill(new RoundRectangle2D.Double(x + ancho * 0.03, linea, ancho * 0.94, 4, 4, 4));
        }
        g.setColor(detalle);
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.03, y + alto * 0.30, ancho * 0.94, alto * 0.05, 4, 4));

        faros(g, x, y, ancho, faros);
        g.setColor(new Color(0xC0392B));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.06, y + alto * 0.94, ancho * 0.20, alto * 0.05, 4, 4));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.74, y + alto * 0.94, ancho * 0.20, alto * 0.05, 4, 4));
        g.setColor(new Color(0, 0, 0, 120));
        g.setStroke(new BasicStroke(Math.max(1.5f, (float) (ancho * 0.03))));
        g.draw(new RoundRectangle2D.Double(x, y, ancho, alto, ancho * 0.08, ancho * 0.08));
        g.setStroke(new BasicStroke(1f));
    }

    private static void faros(Graphics2D g, double x, double y, double ancho, boolean faros) {
        Color color = faros ? new Color(255, 246, 200, 230) : new Color(220, 225, 232, 170);
        double radio = ancho * 0.10;
        g.setColor(faros ? new Color(255, 240, 170, 70) : new Color(200, 210, 220, 40));
        g.fill(new Ellipse2D.Double(x + ancho * 0.10 - radio, y + 4 - radio, radio * 2, radio * 2));
        g.fill(new Ellipse2D.Double(x + ancho * 0.90 - radio, y + 4 - radio, radio * 2, radio * 2));
        g.setColor(color);
        g.fill(new Ellipse2D.Double(x + ancho * 0.10 - radio * 0.6, y + 4 - radio * 0.6, radio * 1.2, radio * 1.2));
        g.fill(new Ellipse2D.Double(x + ancho * 0.90 - radio * 0.6, y + 4 - radio * 0.6, radio * 1.2, radio * 1.2));
    }

    public static void obstaculo(Graphics2D g, Obstaculo obstaculo, Pista pista) {
        double anchoCarril = pista.getAnchoCarril();
        double ancho = obstaculo.obtenerAnchoPx(anchoCarril);
        double alto = obstaculo.obtenerAltoPx(anchoCarril);
        double x = obstaculo.obtenerCentroX(pista.getIzquierda(), anchoCarril) - ancho / 2.0;
        double y = obstaculo.getPosicionY();
        sombra(g, x, y, ancho, alto);
        cono(g, x, y, ancho, alto);
    }

    private static void cono(Graphics2D g, double x, double y, double ancho, double alto) {
        java.awt.geom.Path2D.Double triangulo = new java.awt.geom.Path2D.Double();
        triangulo.moveTo(x + ancho / 2.0, y);
        triangulo.lineTo(x + ancho, y + alto);
        triangulo.lineTo(x, y + alto);
        triangulo.closePath();
        g.setColor(new Color(0, 0, 0, 60));
        g.fill(new RoundRectangle2D.Double(x - 4, y + alto - 6, ancho + 8, 10, 6, 6));
        g.setPaint(new GradientPaint((float) x, (float) y, new Color(0xFF9A4D), (float) x,
                (float) (y + alto), new Color(0xD2521A)));
        g.fill(triangulo);
        g.setColor(new Color(255, 255, 255, 210));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.22, y + alto * 0.42, ancho * 0.56, alto * 0.14, 4, 4));
    }

    public static void recurso(Graphics2D g, Recurso recurso, Pista pista) {
        double anchoCarril = pista.getAnchoCarril();
        double x = recurso.obtenerCentroX(pista.getIzquierda(), anchoCarril) - anchoCarril * 0.18;
        double y = recurso.getPosicionY() - anchoCarril * 0.24;
        double ancho = anchoCarril * 0.36;
        double alto = ancho * 1.3;
        double salto = Math.abs(Math.sin(recurso.getAnimacion())) * 6.0;
        g.setColor(new Color(47, 227, 138, 60));
        g.fill(new Ellipse2D.Double(x - 12, y - 12 - salto, ancho + 24, ancho + 24));
        g.setColor(new Color(0x1F7A4D));
        g.fill(new RoundRectangle2D.Double(x + 2, y + alto * 0.42, ancho - 4, alto * 0.58, 8, 8));
        g.setPaint(new GradientPaint((float) x, (float) (y - salto), new Color(0x39D98A),
                (float) x, (float) (y + alto * 0.6 - salto), new Color(0x14703F)));
        g.fill(new RoundRectangle2D.Double(x, y - salto, ancho, alto * 0.55, 8, 8));
        g.setColor(new Color(0xFFE9A8));
        g.fill(new RoundRectangle2D.Double(x + ancho * 0.28, y + alto * 0.12 - salto, ancho * 0.44, alto * 0.16, 4, 4));
        g.setColor(new Color(15, 26, 20, 140));
        g.fill(new RoundRectangle2D.Double(x - 4, y + alto * 0.95, ancho + 8, 8, 6, 6));
    }

    public static void efecto(Graphics2D g, Efecto efecto) {
        float alfa = (float) efecto.getAlfa();
        if (alfa <= 0f) {
            return;
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa));
        g.setColor(efecto.getColor());
        double tamano = efecto.getTamano() * (efecto.isGravedad() ? 1.0 : 2.0 - alfa);
        g.fill(new Ellipse2D.Double(efecto.getX() - tamano / 2.0, efecto.getY() - tamano / 2.0, tamano, tamano));
        g.setComposite(AlphaComposite.SrcOver);
    }

    public static void vistaPrevia(Graphics2D g, TipoVehiculo tipo, int x, int y, int ancho, int alto) {
        Vehiculo vehiculo;
        try {
            vehiculo = tipo.crear();
        } catch (IllegalStateException ex) {
            return;
        }
        int centroX = x + ancho / 2;
        Color base = new Color(vehiculo.colorBase());
        switch (tipo) {
            case CAMION:
                camion(g, centroX - ancho * 0.30, y + alto * 0.06, ancho * 0.60, alto * 0.88, base,
                        new Color(vehiculo.colorDetalle()), true);
                break;
            case MOTO:
                moto(g, centroX - ancho * 0.18, y + alto * 0.06, ancho * 0.36, alto * 0.88, base,
                        new Color(vehiculo.colorDetalle()), true);
                break;
            default:
                carro(g, centroX - ancho * 0.26, y + alto * 0.06, ancho * 0.52, alto * 0.88, base,
                        new Color(vehiculo.colorDetalle()), 0.0, true);
                break;
        }
    }
}