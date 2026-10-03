package carreras.modelo;

import carreras.util.Preferencias;

public class Configuracion {

    public static final int LONGITUD_MAXIMA_NOMBRE = 16;

    private final TipoVehiculo tipoVehiculo;
    private final TipoVehiculo tipoVehiculo2;
    private final ModoJuego modo;
    private final Modalidad modalidad;
    private final Dificultad dificultad;
    private final boolean sonido;
    private final String nombrePiloto;
    private final String nombrePiloto2;

    public Configuracion(TipoVehiculo tipoVehiculo, ModoJuego modo, Dificultad dificultad,
            boolean sonido, String nombrePiloto) throws ExcepcionJuego {
        this(tipoVehiculo, tipoVehiculo, modo, Modalidad.CONTRA_PC, dificultad, sonido, nombrePiloto, "Piloto 2");
    }

    public Configuracion(TipoVehiculo tipoVehiculo, TipoVehiculo tipoVehiculo2, ModoJuego modo,
            Modalidad modalidad, Dificultad dificultad, boolean sonido,
            String nombrePiloto, String nombrePiloto2) throws ExcepcionJuego {
        if (tipoVehiculo == null) {
            throw new ExcepcionJuego("No se selecciono ningun vehiculo");
        }
        if (modalidad == null) {
            throw new ExcepcionJuego("No se selecciono ningun modo de jugadores");
        }
        if (modo == null) {
            throw new ExcepcionJuego("No se selecciono ningun modo de juego");
        }
        if (dificultad == null) {
            throw new ExcepcionJuego("No se selecciono ninguna dificultad");
        }
        if (modalidad.esDuo() && tipoVehiculo2 == null) {
            throw new ExcepcionJuego("El jugador 2 necesita su propio vehiculo");
        }
        this.tipoVehiculo = tipoVehiculo;
        this.tipoVehiculo2 = tipoVehiculo2 == null ? tipoVehiculo : tipoVehiculo2;
        this.modo = modo;
        this.modalidad = modalidad;
        this.dificultad = dificultad;
        this.sonido = sonido;
        this.nombrePiloto = validarNombre(nombrePiloto, 1);
        this.nombrePiloto2 = modalidad.esDuo() ? validarNombre(nombrePiloto2, 2) : "Piloto 2";
    }

    private String validarNombre(String nombre, int numero) throws ExcepcionJuego {
        if (nombre == null || nombre.trim().isEmpty()) {
            return numero == 1 ? "Piloto" : "Piloto " + numero;
        }
        String limpio = nombre.trim().replaceAll("\\s+", " ");
        if (limpio.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new ExcepcionJuego("El nombre del piloto no puede pasar de " + LONGITUD_MAXIMA_NOMBRE + " caracteres");
        }
        for (int i = 0; i < limpio.length(); i++) {
            char c = limpio.charAt(i);
            if (c < 32 || c == 127) {
                throw new ExcepcionJuego("El nombre del piloto contiene caracteres no validos");
            }
        }
        return limpio;
    }

    public static Configuracion desdePreferencias(Preferencias preferencias) throws ExcepcionJuego {
        if (preferencias == null) {
            throw new ExcepcionJuego("No hay preferencias guardadas");
        }
        TipoVehiculo tipo = TipoVehiculo.desdeNombre(preferencias.get("vehiculo", TipoVehiculo.CARRO.name()));
        TipoVehiculo tipo2 = TipoVehiculo.desdeNombre(preferencias.get("vehiculo2", TipoVehiculo.MOTO.name()));
        ModoJuego modo = ModoJuego.desdeNombre(preferencias.get("modo", ModoJuego.CARRERA.name()));
        Modalidad modalidad = Modalidad.desdeNombre(preferencias.get("modalidad", Modalidad.CONTRA_PC.name()));
        Dificultad dificultad = Dificultad.desdeNombre(preferencias.get("dificultad", Dificultad.NORMAL.name()));
        String piloto = preferencias.get("piloto", "Piloto");
        String piloto2 = preferencias.get("piloto2", "Piloto 2");
        boolean sonido = preferencias.get("sonido", "1").equals("1");
        return new Configuracion(tipo, tipo2, modo, modalidad, dificultad, sonido, piloto, piloto2);
    }

    public Vehiculo crearVehiculo() throws ExcepcionJuego {
        return crearVehiculo(0);
    }

    public Vehiculo crearVehiculo(int indiceJugador) throws ExcepcionJuego {
        TipoVehiculo tipo = indiceJugador == 0 ? tipoVehiculo : tipoVehiculo2;
        try {
            Vehiculo vehiculo = tipo.crear();
            if (vehiculo == null) {
                throw new ExcepcionJuego("No se pudo crear el vehiculo seleccionado");
            }
            return vehiculo;
        } catch (IllegalStateException ex) {
            throw new ExcepcionJuego("El vehiculo " + tipo.getEtiqueta() + " no esta disponible", ex);
        }
    }

    public String getNombrePiloto(int indiceJugador) {
        return indiceJugador == 0 ? nombrePiloto : nombrePiloto2;
    }

    public String resumen() {
        StringBuilder texto = new StringBuilder();
        texto.append(modalidad.getEtiqueta()).append(" - ")
                .append(modo.getEtiqueta()).append(" - ")
                .append(dificultad.getEtiqueta());
        return texto.toString();
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public TipoVehiculo getTipoVehiculo2() {
        return tipoVehiculo2;
    }

    public ModoJuego getModo() {
        return modo;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public Dificultad getDificultad() {
        return dificultad;
    }

    public boolean isSonido() {
        return sonido;
    }

    public String getNombrePiloto() {
        return nombrePiloto;
    }

    public String getNombrePiloto2() {
        return nombrePiloto2;
    }
}