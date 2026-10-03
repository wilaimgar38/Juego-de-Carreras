package carreras.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Preferencias {

    private final Path archivo;
    private final Properties propiedades = new Properties();
    private boolean guardadas;

    public Preferencias(Path archivo) {
        this.archivo = archivo;
        cargar();
    }

    public final void cargar() {
        if (archivo == null) {
            return;
        }
        try (InputStream entrada = Files.newInputStream(archivo)) {
            propiedades.load(entrada);
        } catch (IOException ex) {
            propiedades.clear();
        } catch (IllegalArgumentException ex) {
            propiedades.clear();
        }
    }

    public void guardar() {
        if (archivo == null) {
            return;
        }
        try (OutputStream salida = Files.newOutputStream(archivo)) {
            propiedades.store(salida, "Preferencias de Carreras");
            guardadas = true;
        } catch (IOException ex) {
            guardadas = false;
            System.err.println("No se pudieron guardar las preferencias: " + ex.getMessage());
        }
    }

    public String get(String clave, String valorPorDefecto) {
        String valor = propiedades.getProperty(clave);
        return valor == null || valor.trim().isEmpty() ? valorPorDefecto : valor;
    }

    public void set(String clave, String valor) {
        if (clave == null || valor == null) {
            return;
        }
        propiedades.setProperty(clave, valor);
    }

    public int getEntero(String clave, int valorPorDefecto) {
        String valor = get(clave, null);
        if (valor == null) {
            return valorPorDefecto;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException ex) {
            return valorPorDefecto;
        }
    }

    public void setEntero(String clave, int valor) {
        set(clave, Integer.toString(valor));
    }

    public boolean isGuardadas() {
        return guardadas;
    }
}