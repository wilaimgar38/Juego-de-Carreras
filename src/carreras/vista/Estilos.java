package carreras.vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public final class Estilos {

    public static final int ANCHO = 940;
    public static final int ALTO = 800;

    public static final Color FONDO = new Color(0x0B1018);
    public static final Color CIELO_ALTO = new Color(0x14284F);
    public static final Color CIELO_MEDIO = new Color(0x3D5A94);
    public static final Color CIELO_BAJO = new Color(0xE9823C);
    public static final Color SOL = new Color(0xFFD27A);
    public static final Color MONTAÑA_LEJANA = new Color(0x243B5E);
    public static final Color MONTAÑA_CERCA = new Color(0x1B2C48);
    public static final Color CESPED = new Color(0x1F4D33);
    public static final Color CESPED_ALT = new Color(0x27603F);
    public static final Color ASFALTO = new Color(0x23272E);
    public static final Color ASFALTO_ALT = new Color(0x2C3138);
    public static final Color LINEA = new Color(0xE8EDF3);
    public static final Color BORDE = new Color(0x0E1116);
    public static final Color ACENTO = new Color(0x39D98A);
    public static final Color ACENTO_ALT = new Color(0x8B5CF6);
    public static final Color PELIGRO = new Color(0xFF5A5A);
    public static final Color COMBUSTIBLE = new Color(0xFFC64E);
    public static final Color TEXTO = new Color(0xF2F6FA);
    public static final Color TEXTO_TENUE = new Color(0x9AA7B4);

    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 42);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.PLAIN, 16);
    public static final Font FUENTE_SECCION = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FUENTE_HUD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FUENTE_CIFRA = new Font("SansSerif", Font.BOLD, 30);
    public static final Font FUENTE_PEQUENA = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FUENTE_BOTON = new Font("SansSerif", Font.BOLD, 16);

    private Estilos() {
    }

    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static GradientPaint degradadoVertical(double x, double y, double alto, Color arriba, Color abajo) {
        return new GradientPaint((float) x, (float) y, arriba, (float) x, (float) (y + alto), abajo);
    }

    public static void panel(Graphics2D g, int x, int y, int ancho, int alto, int radio, Color color, float alfa) {
        g.setColor(color);
        int alfaValor = Math.max(0, Math.min(255, (int) (alfa * 255)));
        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alfaValor / 255.0f));
        g.fillRoundRect(x, y, ancho, alto, radio, radio);
        g.setComposite(java.awt.AlphaComposite.SrcOver);
    }

    public static void texto(Graphics2D g, String contenido, Font fuente, Color color, double x, double y) {
        g.setFont(fuente);
        g.setColor(color);
        g.drawString(contenido, (float) x, (float) y);
    }

    public static void textoCentro(Graphics2D g, String contenido, Font fuente, Color color,
            int centroX, double y) {
        java.awt.FontMetrics metricas = g.getFontMetrics(fuente);
        int x = centroX - metricas.stringWidth(contenido) / 2;
        g.setFont(fuente);
        g.setColor(color);
        g.drawString(contenido, x, (float) y);
    }

    public static void textoDerecha(Graphics2D g, String contenido, Font fuente, Color color,
            int derechaX, double y) {
        java.awt.FontMetrics metricas = g.getFontMetrics(fuente);
        g.setFont(fuente);
        g.setColor(color);
        g.drawString(contenido, derechaX - metricas.stringWidth(contenido), (float) y);
    }

    public static void barra(Graphics2D g, int x, int y, int ancho, int alto, double proporcion, Color color) {
        double p = Math.max(0.0, Math.min(1.0, proporcion));
        panel(g, x, y, ancho, alto, alto / 2, new Color(0x11161F), 0.75f);
        if (p > 0.001) {
            int relleno = (int) (ancho * p);
            g.setColor(color);
            g.fillRoundRect(x, y, Math.max(alto, relleno), alto, alto / 2, alto / 2);
        }
    }

    public static JButton boton(String texto, final Color fondo, final Color textoColor) {
        JButton boton = new JButton(texto);
        boton.setFont(FUENTE_BOTON);
        boton.setForeground(textoColor);
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(10, 22, 10, 22));
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setBackground(fondo);
        return boton;
    }
}