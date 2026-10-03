package carreras.modelo;

public class Jugador {

    private final Vehiculo vehiculo;
    private final String nombrePiloto;
    private final int numero;
    private double puntos;
    private int racha;
    private int mejorRacha;
    private int golpesRecibidos;
    private int adelantamientos;
    private int recursos;

    public Jugador(Vehiculo vehiculo, String nombrePiloto) throws ExcepcionJuego {
        this(vehiculo, nombrePiloto, 1);
    }

    public Jugador(Vehiculo vehiculo, String nombrePiloto, int numero) throws ExcepcionJuego {
        if (vehiculo == null) {
            throw new ExcepcionJuego("El jugador necesita un vehiculo");
        }
        if (numero < 1) {
            throw new ExcepcionJuego("Numero de jugador invalido");
        }
        this.vehiculo = vehiculo;
        this.numero = numero;
        this.nombrePiloto = nombrePiloto == null || nombrePiloto.trim().isEmpty()
                ? (numero == 1 ? "Piloto" : "Piloto " + numero) : nombrePiloto.trim();
    }

    public int getNumero() {
        return numero;
    }

    public String getEtiqueta() {
        return "J" + numero;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public String getNombrePiloto() {
        return nombrePiloto;
    }

    public void sumarRacha(int delta) {
        racha += delta;
        if (racha > mejorRacha) {
            mejorRacha = racha;
        }
        if (racha < 0) {
            racha = 0;
        }
    }

    public void registrarImpacto(double dano) {
        golpesRecibidos++;
        racha = 0;
        this.puntos = Math.max(0.0, this.puntos - 40.0);
    }

    public void registrarAdelantamiento() {
        adelantamientos++;
    }

    public void registrarRecurso() {
        recursos++;
    }

    public void sumarPuntos(double cantidad) {
        if (cantidad > 0.0) {
            puntos += cantidad;
        }
    }

    public double getPuntos() {
        return puntos;
    }

    public int getRacha() {
        return racha;
    }

    public int getMejorRacha() {
        return mejorRacha;
    }

    public int getGolpesRecibidos() {
        return golpesRecibidos;
    }

    public int getAdelantamientos() {
        return adelantamientos;
    }

    public int getRecursos() {
        return recursos;
    }

    public int getCarril() {
        return vehiculo.getCarril();
    }

    public double getVelocidad() {
        return vehiculo.getVelocidad();
    }

    public double getVelocidadKmh() {
        return vehiculo.getVelocidadKmh();
    }

    public double getSalud() {
        return vehiculo.getSalud();
    }

    public double getCombustible() {
        return vehiculo.getCombustible();
    }

    public double obtenerVelocidadRelativa() {
        return vehiculo.obtenerVelocidadRelativa();
    }

    public double obtenerCentroX(double izquierda, double anchoCarril) {
        return vehiculo.obtenerCentroX(izquierda, anchoCarril);
    }

    public double obtenerAnchoPx(double anchoCarril) {
        return vehiculo.obtenerAnchoPx(anchoCarril);
    }

    public double obtenerAltoPx(double anchoCarril) {
        return vehiculo.obtenerAltoPx(anchoCarril);
    }

    public boolean isAveriado() {
        return vehiculo.isAveriado();
    }

    public String nombreVehiculo() {
        return vehiculo.nombre();
    }
}