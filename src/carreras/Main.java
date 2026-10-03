package carreras;

import carreras.vista.VentanaJuego;
import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println("El juego necesita un entorno grafico (no se puede ejecutar en consola).");
                return;
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        aplicarEstilo();
                        new VentanaJuego().setVisible(true);
                    } catch (RuntimeException ex) {
                        error("Ocurrio un error inesperado: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Throwable errorGrave) {
            System.err.println("Fallo grave al arrancar el juego: " + errorGrave.getMessage());
        }
    }

    private static void aplicarEstilo() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception error) {
                System.err.println("Se usara el estilo por defecto de Swing");
            }
        }
    }

    private static void error(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Gran Rally Turbo", JOptionPane.ERROR_MESSAGE);
    }
}