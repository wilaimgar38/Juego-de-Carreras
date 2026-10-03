package carreras.modelo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class Marcador {

    private static final String NOMBRE_ARCHIVO = "puntajes.dat";

    private final Path archivo;
    private final Map<String, Integer> mejores = new LinkedHashMap<String, Integer>();
    private final Map<String, String> pilotos = new LinkedHashMap<String, String>();

    public Marcador() {
        this(Paths.get(NOMBRE_ARCHIVO));
    }

    public Marcador(Path archivo) {
        this.archivo = archivo;
        cargar();
    }

    public final void cargar() {
        mejores.clear();
        pilotos.clear();
        if (archivo == null || !Files.exists(archivo)) {
            return;
        }
        BufferedReader lector = null;
        try {
            lector = Files.newBufferedReader(archivo, Charset.forName("UTF-8"));
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                try {
                    String[] partes = linea.split(";");
                    if (partes.length < 2) {
                        continue;
                    }
                    int puntos = Integer.parseInt(partes[1].trim());
                    mejores.put(partes[0].trim(), Integer.max(0, puntos));
                    if (partes.length >= 3) {
                        pilotos.put(partes[0].trim(), partes[2].trim());
                    }
                } catch (NumberFormatException ex) {
                    continue;
                }
            }
        } catch (IOException ex) {
            mejores.clear();
            pilotos.clear();
        } finally {
            cerrar(lector);
        }
    }

    public void guardar() {
        BufferedWriter escritor = null;
        try {
            escritor = Files.newBufferedWriter(archivo, Charset.forName("UTF-8"));
            for (Map.Entry<String, Integer> entrada : mejores.entrySet()) {
                String piloto = pilotos.get(entrada.getKey());
                escritor.write(entrada.getKey() + ";" + entrada.getValue()
                        + ";" + (piloto == null ? "-" : piloto.replace(";", " ")));
                escritor.newLine();
            }
        } catch (IOException ex) {
            System.err.println("No se pudo guardar el marcador: " + ex.getMessage());
        } finally {
            cerrarEscritor(escritor);
        }
    }

    private void cerrar(BufferedReader lector) {
        if (lector == null) {
            return;
        }
        try {
            lector.close();
        } catch (IOException ex) {
            System.err.println("Error al cerrar el archivo de puntajes");
        }
    }

    private void cerrarEscritor(BufferedWriter escritor) {
        if (escritor == null) {
            return;
        }
        try {
            escritor.close();
        } catch (IOException ex) {
            System.err.println("Error al cerrar el archivo de puntajes");
        }
    }

    public boolean registrar(String modo, int puntos, String piloto) {
        if (modo == null) {
            return false;
        }
        int actual = getMejor(modo);
        if (puntos > actual) {
            mejores.put(modo, puntos);
            pilotos.put(modo, piloto == null ? "-" : piloto);
            guardar();
            return true;
        }
        return false;
    }

    public int getMejor(String modo) {
        Integer valor = mejores.get(modo);
        return valor == null ? 0 : valor.intValue();
    }

    public String getPiloto(String modo) {
        String piloto = pilotos.get(modo);
        return piloto == null ? "-" : piloto;
    }

    public void reiniciar() {
        mejores.clear();
        pilotos.clear();
        guardar();
    }

    public Map<String, Integer> getMejores() {
        return mejores;
    }
}