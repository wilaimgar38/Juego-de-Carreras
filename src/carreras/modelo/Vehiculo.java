package carreras.modelo;

public abstract class Vehiculo {

    public static final int CARRILES = 4;

    protected int carril;
    protected int carrilObjetivo;
    protected double progresoCarril;
    protected double velocidad;
    protected double salud;
    protected double combustible;
    protected boolean averiado;
    protected int colorBasePersonalizada = -1;
    protected int colorDetallePersonalizado = -1;

    protected Vehiculo() {
        this(1);
    }

    protected Vehiculo(int carrilInicial) {
        this.carril = limitarCarril(carrilInicial);
        this.carrilObjetivo = this.carril;
        this.progresoCarril = 0.0;
        this.velocidad = 0.0;
        this.salud = saludMaxima();
        this.combustible = 100.0;
    }

    public abstract String nombre();

    public abstract TipoVehiculo tipo();

    public abstract double anchoRelativo();

    public abstract double factorAspecto();

    public abstract double velocidadMaxima();

    public abstract double aceleracion();

    public abstract double frenado();

    public abstract double consumo();

    public abstract double agilidadCarril();

    public abstract double saludMaxima();

    public abstract int colorBase();

    public abstract int colorDetalle();

    public void actualizarFisica(double dt, boolean acelerar, boolean frenar, boolean izquierda, boolean derecha) {
        if (dt <= 0) {
            return;
        }
        if (izquierda) {
            cambiarCarril(-1);
        }
        if (derecha) {
            cambiarCarril(1);
        }
        if (frenar) {
            velocidad = Math.max(0.0, velocidad - frenado() * dt);
        } else if (acelerar) {
            double potencia = aceleracion();
            if (averiado) {
                potencia *= 0.55;
            }
            if (salud < 35.0) {
                potencia *= 0.8;
            }
            if (combustible <= 0.0) {
                potencia = 0.0;
            }
            velocidad = Math.min(velocidadMaxima(), velocidad + potencia * dt);
            if (acelerar) {
                combustible = Math.max(0.0, combustible - consumo() * dt);
            }
        } else {
            velocidad = Math.max(0.0, velocidad - frenado() * 0.3 * dt);
        }
        if (combustible <= 0.0) {
            velocidad = Math.max(0.0, velocidad - aceleracion() * 0.4 * dt);
        }
        actualizarProgresoCarril(dt);
    }

    protected void actualizarProgresoCarril(double dt) {
        double diferencia = carrilObjetivo - (carril + progresoCarril);
        double paso = agilidadCarril() * dt;
        if (Math.abs(diferencia) <= paso) {
            carril = limitarCarril(carrilObjetivo);
            progresoCarril = 0.0;
        } else {
            progresoCarril += Math.signum(diferencia) * paso;
        }
    }

    public void cambiarCarril(int delta) {
        irACarril(carrilObjetivo + delta);
    }

    public void aplicarTinte(int colorBase, int colorDetalle) {
        this.colorBasePersonalizada = colorBase;
        this.colorDetallePersonalizado = colorDetalle;
    }

    public boolean tieneTinte() {
        return colorBasePersonalizada >= 0;
    }

    public void limpiarTinte() {
        colorBasePersonalizada = -1;
        colorDetallePersonalizado = -1;
    }

    public void irACarril(int destino) {
        carrilObjetivo = limitarCarril(destino);
    }

    public void recibirImpacto(double dano) {
        salud = Math.max(0.0, salud - dano);
        velocidad *= 0.55;
        if (salud <= 0.0) {
            averiado = true;
            velocidad = 0.0;
        } else if (salud < 30.0) {
            averiado = true;
        }
    }

    public void recibirRoce(double dano) {
        salud = Math.max(0.0, salud - dano);
        velocidad *= 0.9;
        if (salud < 30.0) {
            averiado = true;
        }
    }

    public void recibirCombustible(double cantidad) {
        combustible = Math.min(100.0, combustible + cantidad);
    }

    public boolean colisiona(Obstaculo obstaculo, double izquierda, double anchoCarril, double y, double alto) {
        if (obstaculo == null || !obstaculo.estaActivo()) {
            return false;
        }
        double miAncho = obtenerAnchoPx(anchoCarril);
        double miX = obtenerCentroX(izquierda, anchoCarril) - miAncho / 2.0;
        double suAncho = obstaculo.obtenerAnchoPx(anchoCarril);
        double suX = obstaculo.obtenerCentroX(izquierda, anchoCarril) - suAncho / 2.0;
        boolean solapaX = miX < suX + suAncho && miX + miAncho > suX;
        boolean solapaY = y < obstaculo.getPosicionY() + obstaculo.obtenerAltoPx(anchoCarril)
                && y + alto > obstaculo.getPosicionY();
        return solapaX && solapaY;
    }

    public boolean tocaRecurso(Recurso recurso, double izquierda, double anchoCarril, double y, double alto) {
        if (recurso == null || !recurso.estaActivo()) {
            return false;
        }
        double radio = Math.max(14.0, obtenerAnchoPx(anchoCarril) * 0.45);
        double miX = obtenerCentroX(izquierda, anchoCarril);
        double suX = recurso.obtenerCentroX(izquierda, anchoCarril);
        double diferenciaX = miX - suX;
        double diferenciaY = (y + alto / 2.0) - recurso.getPosicionY();
        return (diferenciaX * diferenciaX + diferenciaY * diferenciaY) <= radio * radio;
    }

    public double obtenerCentroX(double izquierda, double anchoCarril) {
        return izquierda + (carril + 0.5 + progresoCarril) * anchoCarril;
    }

    public double obtenerAnchoPx(double anchoCarril) {
        return anchoRelativo() * anchoCarril * 0.9;
    }

    public double obtenerAltoPx(double anchoCarril) {
        return obtenerAnchoPx(anchoCarril) * factorAspecto();
    }

    public double obtenerVelocidadRelativa() {
        return velocidadMaxima() <= 0 ? 0.0 : Math.min(1.0, velocidad / velocidadMaxima());
    }

    public double obtenerSaludRelativa() {
        return saludMaxima() <= 0 ? 0.0 : Math.max(0.0, salud / saludMaxima());
    }

    public int getCarril() {
        return carril;
    }

    public int getCarrilObjetivo() {
        return carrilObjetivo;
    }

    public void setCarril(int nuevoCarril) {
        this.carril = limitarCarril(nuevoCarril);
        this.carrilObjetivo = this.carril;
        this.progresoCarril = 0.0;
    }

    public double getProgresoCarril() {
        return progresoCarril;
    }

    public double getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(double velocidad) {
        this.velocidad = Math.max(0.0, Math.min(velocidadMaxima(), velocidad));
    }

    public double getVelocidadKmh() {
        return velocidad * 3.6;
    }

    public double getSalud() {
        return salud;
    }

    public void setSalud(double valor) {
        this.salud = Math.max(0.0, Math.min(saludMaxima(), valor));
    }

    public double getCombustible() {
        return combustible;
    }

    public void setCombustible(double valor) {
        this.combustible = Math.max(0.0, Math.min(100.0, valor));
    }

    public boolean isAveriado() {
        return averiado;
    }

    protected static int limitarCarril(int valor) {
        if (valor < 0) {
            return 0;
        }
        if (valor > Vehiculo.CARRILES - 1) {
            return Vehiculo.CARRILES - 1;
        }
        return valor;
    }

    public static int limitarCarrilPublico(int valor) {
        return limitarCarril(valor);
    }
}