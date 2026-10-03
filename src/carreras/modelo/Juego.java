package carreras.modelo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

public class Juego {

    private static final double ANCHO_INICIAL = 900.0;
    private static final double ALTO_INICIAL = 640.0;
    private static final double PUNTOS_POR_METRO = 1.4;
    private static final double DANO_CONO = 6.0;
    private static final double VELOCIDAD_BASE_PC = 26.0;
    private static final double VELOCIDAD_POR_DIFICULTAD = 18.0;
    private static final double PENALIZACION_GOLPE = 0.7;
    private static final int[] COLORES_JUGADOR = {0x35D6C4, 0xF97316};
    private static final int[] COLORES_DETALLE_JUGADOR = {0xB7F5EC, 0xFFD4A8};

    private final Configuracion configuracion;
    private final ArrayList<Jugador> jugadores = new ArrayList<Jugador>();
    private final Pista pista;
    private final Generador generador;
    private final GestorEfectos efectos;
    private final ArrayList<Rival> rivales = new ArrayList<Rival>();
    private final double[] distancias;
    private final boolean[] finalizadoPorJugador;
    private final CausaFin[] causaPorJugador;

    private EstadoJuego estado = EstadoJuego.MENU;
    private CausaFin causaFin = CausaFin.ABANDONO;
    private boolean finalizada;
    private double tiempo;
    private double cuentaAtras = 3.0;
    private double[] ultimoGolpe;
    private double sacudida;
    private ObservableJuego observador;

    public Juego(Configuracion configuracion) throws ExcepcionJuego {
        if (configuracion == null) {
            throw new ExcepcionJuego("Falta la configuracion de la partida");
        }
        this.configuracion = configuracion;
        this.pista = new Pista(ANCHO_INICIAL, ALTO_INICIAL);
        this.generador = new Generador(pista, configuracion.getDificultad(), configuracion.getModo());
        this.efectos = new GestorEfectos();
        crearJugadores();
        crearRivales();
        this.distancias = new double[jugadores.size()];
        this.finalizadoPorJugador = new boolean[jugadores.size()];
        this.causaPorJugador = new CausaFin[jugadores.size()];
        this.ultimoGolpe = new double[jugadores.size()];
        for (int i = 0; i < causaPorJugador.length; i++) {
            causaPorJugador[i] = CausaFin.ABANDONO;
        }
        distinguirJugadores();
    }

    private void crearJugadores() throws ExcepcionJuego {
        jugadores.clear();
        int cuantos = configuracion.getModalidad().getJugadoresHumanos();
        for (int i = 0; i < cuantos; i++) {
            Vehiculo vehiculo = configuracion.crearVehiculo(i);
            vehiculo.setCarril(i == 0 ? 1 : 2);
            jugadores.add(new Jugador(vehiculo, configuracion.getNombrePiloto(i), i + 1));
        }
    }

    private void distinguirJugadores() {
        if (jugadores.size() < 2) {
            return;
        }
        boolean mismoVehiculo = jugadores.get(0).getVehiculo().tipo() == jugadores.get(1).getVehiculo().tipo();
        if (!mismoVehiculo) {
            return;
        }
        for (int i = 0; i < jugadores.size(); i++) {
            jugadores.get(i).getVehiculo().aplicarTinte(
                    COLORES_JUGADOR[i % COLORES_JUGADOR.length],
                    COLORES_DETALLE_JUGADOR[i % COLORES_DETALLE_JUGADOR.length]);
        }
    }

    private void crearRivales() {
        rivales.clear();
        int cuantos = configuracion.getModalidad().getRivales();
        if (!configuracion.getModo().tieneRivales() || cuantos <= 0) {
            return;
        }
        String[] nombres = {"Rayo", "Cola", "Furia", "Tronco", "Vela"};
        int[] colores = {0x2F6FED, 0x27AE60, 0xB03AE8, 0xE67E22, 0xE74C3C};
        double base = VELOCIDAD_BASE_PC + configuracion.getDificultad().getFactorVelocidad()
                * VELOCIDAD_POR_DIFICULTAD;
        for (int i = 0; i < cuantos; i++) {
            double velocidad = base + ThreadLocalRandom.current().nextDouble() * 6.0;
            int carril = i % Vehiculo.CARRILES;
            Rival rival = new Rival(carril, velocidad, colores[i % colores.length], 0x1B1F26, nombres[i]);
            rival.setVentajaInicial(10.0 + i * 8.0);
            rivales.add(rival);
        }
    }

    public void setObservador(ObservableJuego observador) {
        this.observador = observador;
    }

    public void iniciar() {
        estado = EstadoJuego.CUENTA_ATRAS;
        cuentaAtras = 3.0;
        for (int i = 0; i < causaPorJugador.length; i++) {
            finalizadoPorJugador[i] = false;
            causaPorJugador[i] = CausaFin.ABANDONO;
        }
        notificar(EventoJuego.CUENTA_ATRAS, 1.0);
    }

    public void actualizar(double dt, boolean acelerar, boolean frenar, boolean izquierda, boolean derecha) {
        Controles[] controles = new Controles[jugadores.size()];
        for (int i = 0; i < controles.length; i++) {
            controles[i] = i == 0
                    ? new Controles(acelerar, frenar, izquierda, derecha)
                    : Controles.NADA;
        }
        actualizar(dt, controles);
    }

    public void actualizar(double dt, Controles[] controles) {
        if (estado == EstadoJuego.MENU || finalizada || controles == null) {
            return;
        }
        try {
            double paso = validarDelta(dt);
            if (estado == EstadoJuego.CUENTA_ATRAS) {
                cuentaAtras -= paso;
                if (cuentaAtras <= 0.0) {
                    cuentaAtras = 0.0;
                    estado = EstadoJuego.JUGANDO;
                }
                return;
            }
            if (estado == EstadoJuego.PAUSA) {
                return;
            }
            if (estado != EstadoJuego.JUGANDO) {
                efectos.actualizar(paso);
                return;
            }
            ejecutarSimulacion(paso, controles);
        } catch (IllegalStateException ex) {
            finalizar(CausaFin.ERROR_INTERNO);
        } catch (ArithmeticException ex) {
            finalizar(CausaFin.ERROR_INTERNO);
        }
    }

    private Controles controlesDe(int indice, Controles[] controles) {
        if (controles.length > indice && controles[indice] != null) {
            return controles[indice];
        }
        return Controles.NADA;
    }

    private double validarDelta(double dt) {
        if (Double.isNaN(dt) || Double.isInfinite(dt)) {
            return 0.0;
        }
        if (dt <= 0.0) {
            return 0.0;
        }
        return dt > 0.1 ? 0.1 : dt;
    }

    private void ejecutarSimulacion(double dt, Controles[] controles) {
        double velocidadMaxima = 0.0;
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador jugador = jugadores.get(i);
            Controles control = controlesDe(i, controles);
            jugador.getVehiculo().actualizarFisica(dt, control.isAcelerar(), control.isFrenar(),
                    control.isCarrilIzquierda(), control.isCarrilDerecha());
            if (jugador.getVelocidad() > velocidadMaxima) {
                velocidadMaxima = jugador.getVelocidad();
            }
        }
        actualizarRivales(dt);
        pista.actualizar(dt, velocidadMaxima);
        generador.actualizar(dt, velocidadMaxima, distanciaMedia());
        detectarColisiones(dt);
        recogerRecursos();
        actualizarEfectosAmbientales();
        efectos.actualizar(dt);
        for (int i = 0; i < jugadores.size(); i++) {
            distancias[i] += jugadores.get(i).getVelocidad() * dt;
        }
        tiempo += dt;
        calcularPuntuacion(dt);
        comprobarFin();
        sacudida = Math.max(0.0, sacudida - dt * 1.8);
    }

    private double distanciaMedia() {
        if (distancias.length == 0) {
            return 0.0;
        }
        double suma = 0.0;
        for (double distancia : distancias) {
            suma += distancia;
        }
        return suma / distancias.length;
    }

    private void actualizarRivales(double dt) {
        double velocidadLider = velocidadLeader();
        for (Rival rival : rivales) {
            rival.actualizarIA(dt, pista);
            double ventajaAnterior = rival.getVentaja();
            rival.avanzar(dt, velocidadLider);
            for (int i = 0; i < jugadores.size(); i++) {
                Jugador jugador = jugadores.get(i);
                if (finalizadoPorJugador[i]) {
                    continue;
                }
                if (!rival.isAdelantado() && ventajaAnterior >= 0.0 && rival.getVentaja() < 0.0) {
                    jugador.registrarAdelantamiento();
                    jugador.sumarRacha(1);
                    jugador.sumarPuntos(150.0 * factorPuntuacion(i));
                    notificar(EventoJuego.ADELANTAMIENTO, 1.0);
                }
                if (rival.getCarril() == jugador.getCarril() && Math.abs(rival.getVentaja()) < 4.5) {
                    jugador.getVehiculo().recibirRoce(5.0 * dt);
                }
            }
        }
    }

    private double velocidadLeader() {
        double mejor = 0.0;
        for (Jugador jugador : jugadores) {
            if (jugador.getVelocidad() > mejor) {
                mejor = jugador.getVelocidad();
            }
        }
        return mejor;
    }

    private void detectarColisiones(double dt) {
        double anchoCarril = pista.getAnchoCarril();
        double izquierda = pista.getIzquierda();
        for (int i = 0; i < jugadores.size(); i++) {
            if (finalizadoPorJugador[i]) {
                continue;
            }
            Jugador jugador = jugadores.get(i);
            Vehiculo vehiculo = jugador.getVehiculo();
            double centro = vehiculo.obtenerCentroX(izquierda, anchoCarril);
            double alto = vehiculo.obtenerAltoPx(anchoCarril);
            double y = pista.getPosicionJugador(i) - alto / 2.0;
            for (Obstaculo obstaculo : pista.getObstaculos()) {
                if (!obstaculo.estaActivo() || !vehiculo.colisiona(obstaculo, izquierda, anchoCarril, y, alto)) {
                    continue;
                }
                obstaculo.golpear();
                double dano = DANO_CONO;
                vehiculo.recibirImpacto(dano);
                jugador.registrarImpacto(dano);
                vehiculo.setVelocidad(vehiculo.getVelocidad() * PENALIZACION_GOLPE);
                sacudida = 1.0;
                efectos.crearChispas(centro, pista.getPosicionJugador(i), 22, obstaculo.obtenerColorBase());
                efectos.crearHumo(centro, pista.getPosicionJugador(i), 8);
                if (tiempo - ultimoGolpe[i] > 0.3) {
                    ultimoGolpe[i] = tiempo;
                    notificar(EventoJuego.GOLPE, 1.0);
                }
                if (dt <= 0.0) {
                    return;
                }
            }
        }
    }

    private void recogerRecursos() {
        double anchoCarril = pista.getAnchoCarril();
        double izquierda = pista.getIzquierda();
        for (int i = 0; i < jugadores.size(); i++) {
            if (finalizadoPorJugador[i]) {
                continue;
            }
            Jugador jugador = jugadores.get(i);
            Vehiculo vehiculo = jugador.getVehiculo();
            double alto = vehiculo.obtenerAltoPx(anchoCarril);
            double y = pista.getPosicionJugador(i) - alto / 2.0;
            for (Recurso recurso : pista.getRecursos()) {
                if (!recurso.estaActivo() || !vehiculo.tocaRecurso(recurso, izquierda, anchoCarril, y, alto)) {
                    continue;
                }
                recurso.recolectar();
                vehiculo.recibirCombustible(22.0);
                jugador.registrarRecurso();
                jugador.sumarPuntos(60.0 * factorPuntuacion(i));
                efectos.crearResplandor(recurso.obtenerCentroX(izquierda, anchoCarril),
                        recurso.getPosicionY(), new Color(0x2FE38A), 26.0);
                notificar(EventoJuego.RECOLECTADO, 1.0);
            }
        }
    }

    private void actualizarEfectosAmbientales() {
        double anchoCarril = pista.getAnchoCarril();
        for (int i = 0; i < jugadores.size(); i++) {
            Vehiculo vehiculo = jugadores.get(i).getVehiculo();
            double x = vehiculo.obtenerCentroX(pista.getIzquierda(), anchoCarril);
            double y = pista.getPosicionJugador(i);
            if (vehiculo.obtenerVelocidadRelativa() > 0.92
                    && ThreadLocalRandom.current().nextDouble() < 0.3) {
                efectos.crearHumo(x, y + vehiculo.obtenerAltoPx(anchoCarril) / 2.0, 1);
            }
            if (vehiculo.isAveriado() && ThreadLocalRandom.current().nextDouble() < 0.2) {
                efectos.crearHumo(x, y, 1);
            }
        }
    }

    private void calcularPuntuacion(double dt) {
        double factorModo = configuracion.getModo().esInfinito() ? 1.0 + distanciaMedia() / 12000.0 : 1.0;
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador jugador = jugadores.get(i);
            if (jugador.getVelocidad() > 5.0) {
                jugador.sumarPuntos(jugador.getVelocidad() * PUNTOS_POR_METRO * dt
                        * factorPuntuacion(i) * factorModo);
            }
        }
    }

    private double factorPuntuacion(int indiceJugador) {
        TipoVehiculo tipo = jugadores.get(indiceJugador).getVehiculo().tipo();
        return configuracion.getDificultad().getFactorPuntuacion() * tipo.getFactorPuntuacion();
    }

    private CausaFin evaluarJugador(int indice) {
        Jugador jugador = jugadores.get(indice);
        if (jugador.getSalud() <= 0.0) {
            return CausaFin.FALLO_MECANICO;
        }
        if (jugador.getCombustible() <= 0.0 && jugador.getVelocidad() < 1.0) {
            return CausaFin.SIN_COMBUSTIBLE;
        }
        if (!configuracion.getModo().esInfinito() && distancias[indice] >= configuracion.getModo().getDistanciaMeta()) {
            return CausaFin.META;
        }
        return null;
    }

    private void comprobarFin() {
        boolean alguienTermino = false;
        for (int i = 0; i < jugadores.size(); i++) {
            if (finalizadoPorJugador[i]) {
                continue;
            }
            CausaFin causa = evaluarJugador(i);
            if (causa == null) {
                continue;
            }
            finalizarJugador(i, causa);
            alguienTermino = true;
        }
        if (alguienTermino && todosFinalizados()) {
            finalizar(causaGlobal());
        }
    }

    private void finalizarJugador(int indice, CausaFin causa) {
        finalizadoPorJugador[indice] = true;
        causaPorJugador[indice] = causa;
        Jugador jugador = jugadores.get(indice);
        double x = jugador.obtenerCentroX(pista.getIzquierda(), pista.getAnchoCarril());
        double y = pista.getPosicionJugador(indice);
        if (causa == CausaFin.META) {
            efectos.crearResplandor(x, y, new Color(0xFFD34E), 70.0);
        } else {
            efectos.crearChispas(x, y, 32, new Color(0xFF6B35));
        }
    }

    private boolean todosFinalizados() {
        for (boolean terminado : finalizadoPorJugador) {
            if (!terminado) {
                return false;
            }
        }
        return jugadores.size() > 0;
    }

    private CausaFin causaGlobal() {
        for (int i = 0; i < causaPorJugador.length; i++) {
            if (causaPorJugador[i] == CausaFin.META) {
                return CausaFin.META;
            }
        }
        return causaPorJugador.length > 0 ? causaPorJugador[0] : CausaFin.ABANDONO;
    }

    private void finalizar(CausaFin causa) {
        if (finalizada) {
            return;
        }
        finalizada = true;
        causaFin = causa;
        estado = causa == CausaFin.META ? EstadoJuego.VICTORIA : EstadoJuego.DERROTA;
        for (int i = 0; i < jugadores.size(); i++) {
            if (!finalizadoPorJugador[i]) {
                finalizadoPorJugador[i] = true;
                causaPorJugador[i] = causa;
            }
        }
        if (jugadores.isEmpty()) {
            return;
        }
        double x = jugadores.get(0).obtenerCentroX(pista.getIzquierda(), pista.getAnchoCarril());
        if (causa == CausaFin.META) {
            efectos.crearResplandor(x, pista.getPosicionJugador(0), new Color(0xFFD34E), 95.0);
            notificar(EventoJuego.VICTORIA, 1.0);
        } else {
            efectos.crearChispas(x, pista.getPosicionJugador(0), 45, new Color(0xFF6B35));
            sacudida = 1.5;
            notificar(EventoJuego.DERROTA, 1.0);
        }
    }

    public void pausar() {
        if (estado == EstadoJuego.JUGANDO || estado == EstadoJuego.CUENTA_ATRAS) {
            estado = EstadoJuego.PAUSA;
        }
    }

    public void reanudar() {
        if (estado == EstadoJuego.PAUSA) {
            estado = EstadoJuego.JUGANDO;
        }
    }

    public void alternarPausa() {
        if (estado == EstadoJuego.PAUSA) {
            reanudar();
        } else {
            pausar();
        }
    }

    public void abandonar() {
        finalizar(CausaFin.ABANDONO);
    }

    public void redimensionar(double ancho, double alto) {
        pista.redimensionar(ancho, alto);
    }

    private void notificar(EventoJuego evento, double intensidad) {
        ObservableJuego destino = observador;
        if (destino == null) {
            return;
        }
        try {
            destino.alSucceder(evento, intensidad);
        } catch (RuntimeException ex) {
            observador = null;
        }
    }

    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    public int getNumeroJugadores() {
        return jugadores.size();
    }

    public boolean esMultijugador() {
        return jugadores.size() > 1;
    }

    public Jugador getJugador(int indice) {
        return jugadores.get(Math.max(0, Math.min(indice, jugadores.size() - 1)));
    }

    public Vehiculo getVehiculo() {
        return jugadores.get(0).getVehiculo();
    }

    public Vehiculo getVehiculo(int indice) {
        return getJugador(indice).getVehiculo();
    }

    public Jugador getJugador() {
        return jugadores.get(0);
    }

    public Pista getPista() {
        return pista;
    }

    public Generador getGenerador() {
        return generador;
    }

    public GestorEfectos getEfectos() {
        return efectos;
    }

    public Configuracion getConfiguracion() {
        return configuracion;
    }

    public ArrayList<Rival> getRivales() {
        return rivales;
    }

    public ArrayList<Rival> getClasificacion() {
        ArrayList<Rival> copia = new ArrayList<Rival>(rivales);
        Collections.sort(copia, new Comparator<Rival>() {
            @Override
            public int compare(Rival primero, Rival segundo) {
                return Double.compare(segundo.getVentaja(), primero.getVentaja());
            }
        });
        return copia;
    }

    public double getDistancia() {
        return getDistancia(0);
    }

    public double getDistancia(int indice) {
        if (distancias.length == 0) {
            return 0.0;
        }
        return distancias[Math.max(0, Math.min(indice, distancias.length - 1))];
    }

    public double getDistanciaMeta() {
        return configuracion.getModo().getDistanciaMeta();
    }

    public double getProgreso() {
        return getProgreso(0);
    }

    public double getProgreso(int indice) {
        if (configuracion.getModo().esInfinito() || getDistanciaMeta() <= 0.0) {
            return 0.0;
        }
        double progreso = getDistancia(indice) / getDistanciaMeta();
        return progreso > 1.0 ? 1.0 : progreso;
    }

    public double getProgresoLeader() {
        double mejor = 0.0;
        for (int i = 0; i < jugadores.size(); i++) {
            if (getProgreso(i) > mejor) {
                mejor = getProgreso(i);
            }
        }
        return mejor;
    }

    public int getPuesto(int indice) {
        int puesto = 1;
        double distancia = getDistancia(indice);
        for (Rival rival : rivales) {
            if (rival.getVentaja() > 0.0) {
                puesto++;
            }
        }
        for (int i = 0; i < jugadores.size(); i++) {
            if (i != indice && getDistancia(i) > distancia) {
                puesto++;
            }
        }
        return puesto;
    }

    public int getTotalCompetidores() {
        return jugadores.size() + rivales.size();
    }

    public int getNumeroRivales() {
        return rivales.size();
    }

    public String getNombreRival(int indice) {
        if (indice < 0 || indice >= rivales.size()) {
            return "-";
        }
        return rivales.get(indice).nombre();
    }

    public double getDistanciaRival(int indice) {
        if (indice < 0 || indice >= rivales.size()) {
            return 0.0;
        }
        return Math.max(0.0, distanciaMedia() + rivales.get(indice).getVentaja());
    }

    public int getPuestoRival(int indice) {
        if (indice < 0 || indice >= rivales.size()) {
            return 0;
        }
        double distancia = getDistanciaRival(indice);
        int puesto = 1;
        for (int i = 0; i < jugadores.size(); i++) {
            if (getDistancia(i) > distancia) {
                puesto++;
            }
        }
        for (int j = 0; j < rivales.size(); j++) {
            if (j != indice && getDistanciaRival(j) > distancia) {
                puesto++;
            }
        }
        return puesto;
    }

    public double getVelocidadRival(int indice) {
        if (indice < 0 || indice >= rivales.size()) {
            return 0.0;
        }
        return rivales.get(indice).getVelocidad();
    }

    public boolean rivalTermino(int indice) {
        return !configuracion.getModo().esInfinito()
                && getDistanciaRival(indice) >= configuracion.getModo().getDistanciaMeta();
    }

    public double getMeta() {
        return configuracion.getModo().getDistanciaMeta();
    }

    public double getTiempo() {
        return tiempo;
    }

    public double getPuntos() {
        return getPuntos(0);
    }

    public double getPuntos(int indice) {
        return getJugador(indice).getPuntos();
    }

    public int getPuntosEnteros() {
        return getPuntosEnteros(0);
    }

    public int getPuntosEnteros(int indice) {
        return (int) Math.floor(getPuntos(indice));
    }

    public double getCuentaAtras() {
        return cuentaAtras;
    }

    public double getSacudida() {
        return sacudida;
    }

    public EstadoJuego getEstado() {
        return estado;
    }

    public CausaFin getCausaFin() {
        return causaFin;
    }

    public CausaFin getCausaFin(int indice) {
        return causaPorJugador[Math.max(0, Math.min(indice, causaPorJugador.length - 1))];
    }

    public boolean isFinalizado(int indice) {
        return finalizadoPorJugador[Math.max(0, Math.min(indice, finalizadoPorJugador.length - 1))];
    }

    public boolean isFinalizado() {
        return finalizada;
    }
}