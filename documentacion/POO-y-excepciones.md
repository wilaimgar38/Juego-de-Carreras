# Herencia, polimorfismo y manejo de excepciones

Análisis conceptual de **Gran Rally Turbo** basado en el código real de `src/carreras`.
Cada ejemplo incluye la referencia al fichero y a la línea para que pueda verificarse.

---

# 1. Herencia

## 1.1 La jerarquía

```
Vehiculo (abstract)
   ├── Carro
   ├── Moto
   ├── Camion
   └── Rival
```

Declaración en `src/carreras/modelo/Vehiculo.java:12`:

```java
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
    protected int colorDetallePersonalizada = -1;
```

Cuatro decisiones de diseño que explican esta jerarquía:

| Decisión | Motivo |
|---|---|
| La clase es `abstract` | Un `Vehiculo` «a pelo» no tiene anchura, velocidad máxima ni aspecto: no se puede dibujar ni conducir. |
| Los constructores son `protected` (`Vehiculo.java:44` y `Vehiculo.java:47`) | Nadie fuera de las subclases puede crear vehículos genéricos. Impide instanciar una clase abstracta por accidente. |
| Los atributos son `protected` | Las subclases los leen y modifican directamente (`vehocidad`, `carril`…) sin necesidad de una cadena de *getters*. |
| Los métodos de física son `public` y los auxiliares `protected` | El motor (`Juego`) llama a `actualizarFisica(...)`, pero solo las subclases usan `actualizarProgresoCarril(...)`. |

## 1.2 Qué hereda cada subclase

Las subclases heredan automáticamente:

- El estado: `carril`, `velocidad`, `salud`, `combustible`, `averiado`.
- El comportamiento común:
  `actualizarFisica`, `cambiarCarril`, `irACarril`, `aplicarTinte`, `limpiarTinte`,
  `recibirImpacto`, `recibirRoce`, `recibirCombustible`, `colisiona`, `tocaRecurso`,
  `obtenerCentroX`, `obtenerAnchoPx`, `obtenerAltoPx`, `obtenerVelocidadRelativa`,
  `obtenerSaludRelativa` y todos los *getters*/*setters*.
- La constante `Vehiculo.CARRILES`, usada por `Pista`, `Generador`, `Rival` y `Recurso`
  para garantizar que nadie use un número de carriles distinto.

## 1.3 Los 12 métodos abstractos: «qué es» frente a «cómo se mueve»

`Vehiculo.java:53-64` declara:

```java
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
```

| Método abstracto | Responde a… | Ejemplo (`Carro.java`) | Ejemplo (`Camion.java`) | Ejemplo (`Moto.java`) |
|---|---|---|---|---|
| `nombre()` | ¿Cómo se llama? | `"Carro"` | `"Camion"` | `"Moto"` |
| `tipo()` | ¿Qué categoría es? | `CARRO` | `CAMION` | `MOTO` |
| `anchoRelativo()` | ¿Cuánto ocupa de carril? | `0.72` | `0.92` | `0.42` |
| `factorAspecto()` | ¿Proporción ancho/alto? | `1.85` | `2.90` | `1.70` |
| `velocidadMaxima()` | ¿Tope de velocidad? | `62.0` | `52.0` | `68.0` |
| `aceleracion()` | ¿Cuánto gana por segundo? | `24.0` | `14.0` | `28.0` |
| `frenado()` | ¿Cuánto frena por segundo? | `36.0` | `22.0` | `30.0` |
| `consumo()` | ¿Gasolina por segundo? | `1.7` | `2.5` | `1.0` |
| `agilidadCarril()` | ¿Rapidez al cambiar de carril? | `3.4` | `2.0` | `4.8` |
| `saludMaxima()` | ¿Resistencia total? | `100.0` | `160.0` | `75.0` |
| `colorBase()` / `colorDetalle()` | ¿De qué color se pinta? | `0xE8413C` / `0x1C2430` | `0xF2A03D` / `0x2A2118` | `0x2F7BE8` / `0x14181F` |

### Cómo el motor reutiliza esas respuestas

`Vehiculo.actualizarFisica` (`Vehiculo.java:47`) es el **único** sitio donde se decide el
algoritmo de conducción:

```java
if (frenar) {
    velocidad = Math.max(0.0, velocidad - frenado() * dt);          // ← polimorfismo
} else if (acelerar) {
    double potencia = aceleracion();                                // ← polimorfismo
    if (averiado) {
        potencia *= 0.55;
    }
    if (salud < 35.0) {
        potencia *= 0.8;
    }
    if (combustible <= 0.0) {
        potencia = 0.0;
    }
    velocidad = Math.min(velocidadMaxima(), velocidad + potencia * dt);   // ← polimorfismo
    if (acelerar) {
        combustible = Math.max(0.0, combustible - consumo() * dt);       // ← polimorfismo
    }
} else {
    velocidad = Math.max(0.0, velocidad - frenado() * 0.3 * dt);    // ← polimorfismo
}
```

Este es un ejemplo de **Template Method**: el «qué» (frenado, aceleración, consumo) queda
delegado en las subclases; el «cómo» (física de frenado, penalización por avería, tope de
velocidad) queda escrito una sola vez. Si hubiera que añadir un vehículo nuevo, **no se
tocaría el motor**.

## 1.4 Sobrescritura como punto de extensión

### `Camion`: Hook sobre el comportamiento heredado

`Camion.java:44` y `Camion.java:48` son las dos únicas sobrescrituras de un método concreto
de toda la jerarquía:

```java
@Override
public void recibirImpacto(double dano) {
    super.recibirImpacto(dano * 0.7);   // 30 % menos de daño
}

@Override
public void recibirRoce(double dano) {
    super.recibirRoce(dano * 0.7);
}
```

Llamando a `super` se reutiliza todo el algoritmo heredado (restar salud, reducir velocidad,
marcar `averiado`) y solo se altera el dato de entrada. Este patrón se llama **hook**:
el padre ofrece un método que la subclase puede envolver sin duplicar código.

### `Rival`: hereda la física pero con energía infinita

`Rival.java:168`:

```java
@Override
public double consumo() {
    return 0.0;      // los rivales no gastan combustible
}
```

Como `consumo()` vale `0.0`, `actualizarFisica` nunca le reduce el combustible: el rival hereda
todo el movimiento, la aceleración y el frenado, pero es inmune a quedarse sin gasolina.

### `Rival`: una clase propia que se «disfraza» de carro

`Rival.java:138`:

```java
@Override
public TipoVehiculo tipo() {
    return TipoVehiculo.CARRO;
}
```

La PC es un `Rival` (clase propia, con IA), pero declara ser de tipo `CARRO` porque la
lógica de juego —y el dibujado— trabaja con tres categorías: carro, moto y camión.
Esto es una decisión de diseño deliberada: **el tipo es una etiqueta de juego, no la clase real**.

### Personalización de color: dos rutas

`Carro.java:36`:

```java
@Override
public int colorBase() {
    return tieneTinte() ? colorBasePersonalizada : COLOR_BASE;
}
```

Cada jugador puede elegir el mismo vehículo que su rival (`Juego.distinguirJugadores()`,
`Juego.java:70`): en ese caso se llama a `aplicarTinte(...)` y el color propio tiene prioridad
sobre el color de la clase.

---

# 2. Polimorfismo

## 2.1 Polimorfismo de sustitución (herencia)

La forma más visible: una referencia de tipo `Vehiculo` que en realidad apunta a un `Camion`.

```java
// En Configuracion.java:87
public Vehiculo crearVehiculo(int indiceJugador) throws ExcepcionJuego {
    TipoVehiculo tipo = indiceJugador == 0 ? tipoVehiculo : tipoVehiculo2;
    try {
        Vehiculo vehiculo = tipo.crear();          // ← la variable es Vehiculo
        ...
        return vehiculo;                          // ← el objeto puede ser Carro/Moto/Camion
    } catch (IllegalStateException ex) {
        throw new ExcepcionJuego("El vehiculo " + tipo.getEtiqueta() + " no esta disponible", ex);
    }
}
```

En tiempo de ejecución, el `Vehiculo` devuelto **es** un `Carro`, una `Moto` o un `Camion`, pero
quien lo usa solo conoce el contrato de `Vehiculo`.

## 2.2 Polimorfismo en la simulación

`Juego.ejecutarSimulacion` (`Juego.java:174`) trata a los dos jugadores de la misma manera:

```java
for (int i = 0; i < jugadores.size(); i++) {
    Jugador jugador = jugadores.get(i);
    Controles control = controlesDe(i, controles);
    jugador.getVehiculo().actualizarFisica(dt, control.isAcelerar(), control.isFrenar(),
            control.isCarrilIzquierda(), control.isCarrilDerecha());
    ...
}
```

No hay ni un `if (esCamion)` en el motor. La llamada es idéntica para todos; lo que cambia
es la implementación que se ejecuta: la moto acelerará más y tendrá más agilidad, el camión
frenará menos y resistirá más.

Lo mismo ocurre en `Juego.detectarColisiones` (`Juego.java:246`):

```java
vehiculo.recibirImpacto(dano);   // 6.0 de daño para todos…
```

…pero si ese vehículo es un `Camion`, internamente solo pierde `6.0 * 0.7 = 4.2` puntos de salud.

## 2.3 Polimorfismo como fábrica: `TipoVehiculo.crear()`

`TipoVehiculo.java:31` es una fábrica estática dentro del propio enum:

```java
public Vehiculo crear() {
    switch (this) {
        case CARRO:  return new Carro();
        case MOTO:   return new Moto();
        case CAMION: return new Camion();
        default:
            throw new IllegalStateException("Tipo de vehiculo no soportado: " + this);
    }
}
```

Ventajas para el proyecto:

1. Nadie escribe `new Camion()` repartido por el código: se pide por tipo.
2. Añadir un vehículo obliga a tocar un solo sitio.
3. El error «tipo no soportado» se detecta en el punto de creación.

## 2.4 Polimorfismo en el dibujado

`Dibujante.vehiculo` (`Dibujante.java:226`) recibe un `Vehiculo`:

```java
public static void vehiculo(Graphics2D g, Vehiculo vehiculo, Pista pista, double y,
        double faseRuedas, boolean faros) {
    ...
    switch (vehiculo.tipo()) {                 // ← valor polimórfico (método abstracto)
        case CAMION: camion(g, x, y, ancho, alto, ...); break;
        case MOTO:   moto(g, x, y, ancho, alto, ...);   break;
        default:     carro(g, x, y, ancho, alto, ...);
    }
}
```

Reflexión honesta: aquí hay **dos niveles**. El `switch` NO es polimorfismo; lo es la llamada
`vehiculo.tipo()`, que se resuelve dinámicamente (si el objeto es un `Rival`, devuelve `CARRO`
y se dibuja como un carro). El `switch` existe porque *dibujar* no es responsabilidad del modelo:
si cada subclase implementara `dibujarse(...)`, el modelo tendría que importar `java.awt`,
y el proyecto habría perdido la separación entre capas.

Lo mismo ocurre en `Dibujante.vistaPrevia` (`Dibujante.java:436`), que crea un vehículo
temporal solo para leer sus estadísticas.

## 2.5 Polimorfismo de interfaz

Tres implementaciones de interfaces del JDK y una propia:

| Clase | Interfaz | Qué aporta |
|---|---|---|
| `EntradaTeclado` | `KeyListener` | Implementa `keyPressed`, `keyReleased` y `keyTyped`; traduce teclas a acciones y llama a `evento.consume()`. Se registra directamente con `addKeyListener(...)`. |
| `BucleJuego` | `Runnable` | Su método `run()` es el bucle de juego; así puede lanzarse en un `Thread`. |
| `PanelJuego`, `VentanaJuego` | `ObservableJuego` (propia) | Reciben los eventos del modelo sin conocer `Juego`. |

`ObservableJuego` (`ObservableJuego.java:4`) es una interfaz con un único método:

```java
public interface ObservableJuego {
    void alSucceder(EventoJuego evento, double intensidad);
}
```

`Juego` solo conoce ese contrato:

```java
private void notificar(EventoJuego evento, double intensidad) {
    ObservableJuego destino = observador;
    if (destino == null) {
        return;
    }
    try {
        destino.alSucceder(evento, intensidad);
    } catch (RuntimeException ex) {
        observador = null;      // un observador roto no puede tumbar la partida
    }
}
```

Ventaja: `Juego` no importa **ni una sola clase de Swing**. Si mañana se añade una vista de
consola o una prueba automática, basta con implementar `ObservableJuego`.

## 2.6 Sobrecarga (*overloading*)

Métodos con el mismo nombre y distinta lista de parámetros; no es herencia, pero aparece por todo el código:

| Método | Sobrecargas |
|---|---|
| `Juego.actualizar` | `(double, boolean, boolean, boolean, boolean)` y `(double, Controles[])` |
| `Vehiculo` (constructores) | `Vehiculo()` y `Vehiculo(int carrilInicial)` |
| `Pista.getPosicionJugador` | `()` y `(int indice)` |
| `Juego.getJugador` / `getVehiculo` | `()` y `(int indice)` |
| `Generador.elegirCarrilLibre` | `()` y `(int excepto)` |
| `Marcador` (constructores) | `Marcador()` y `Marcador(Path archivo)` |
| `Dibujante.carro` / `moto` / `camion` | Todos con la misma firma salvo el tipo de vehículo |

`Juego.actualizar(double, boolean, boolean, boolean, boolean)` es, además, una **puente hacia
la compatibilidad con versiones anteriores**: convierte los cuatro `boolean` en un array de
`Controles` donde el segundo jugador recibe `Controles.NADA`.

## 2.7 Enums como sustitutos de las clases de constantes

Los siete enums del modelo no son solo etiquetas: llevan datos y comportamiento.

```java
public enum Dificultad {
    FACIL("Facil", "Menos conos y rivales mas lentos", 0.65, 0.82, 0.75),
    NORMAL("Normal", "El equilibrio recomendado", 1.0, 1.0, 1.0),
    DIFICIL("Dificil", "Conos densos, rival rapido, puntos x1.4", 1.45, 1.22, 1.4);
    ...
    public static Dificultad desdeNombre(String nombre) { ... }   // tolera mayúsculas/minúsculas
}
```

- `ModoJuego.calcularDificultad(distancia)` implementa la dificultad progresiva del modo Infinito.
- `Modalidad.getJugadoresHumanos()` y `getRivales()` deciden cuántos jugadores y rivales crear.
- `TipoVehiculo.crear()` es una fábrica (sección 2.3).
- `desdeNombre(...)` evita `IllegalArgumentException` al leer `preferencias.dat` corrupto.

---

# 3. Manejo de excepciones con `try-catch`

## 3.0 La excepción propia: `ExcepcionJuego`

`src/carreras/modelo/ExcepcionJuego.java`:

```java
public class ExcepcionJuego extends Exception {
    private static final long serialVersionUID = 1L;

    public ExcepcionJuego(String mensaje) { super(mensaje); }

    public ExcepcionJuego(String mensaje, Throwable causa) { super(mensaje, causa); }
}
```

Es una **excepción comprobada** (`extends Exception`, no `RuntimeException`). Se usa para
errores de dominio que el usuario puede corregir:

- Nombre de piloto vacío o de más de 16 caracteres (`Configuracion.validarNombre`).
- Vehículo no disponible al construir la configuración.
- `new Juego(null)`.

Al ser comprobada, el compilador **obliga** a cada capa a decidir qué hacer:
el menú la enseña, la ventana vuelve al menú.

## 3.1 Inventario completo de bloques `catch`

43 bloques en 14 ficheros. Agrupados por intención:

### A) Validación y robustez del modelo: fallar sin romper la partida

| Fichero:línea | Excepción | Origen | Recuperación |
|---|---|---|---|
| `Configuracion.java:95` | `IllegalStateException` | `TipoVehiculo.crear()` con un tipo no soportado | Se **traduce**: `throw new ExcepcionJuego("El vehiculo " + etiqueta + " no esta disponible", ex)`. El usuario ve un mensaje claro en el menú. |
| `Generador.java:45` | `IllegalArgumentException` | Carril inválido al crear un cono o recurso | `pista.limpiar()`, temporizadores a `2.0` y `6.0` segundos. La pista se regenera. |
| `Generador.java:49` | `IllegalStateException` | Estado interno inconsistente del generador | `pista.limpiar()` y `temporizadorObstaculo = 2.0`. |
| `Juego.java:150` | `IllegalStateException` | Estado de juego inconsistente durante la simulación | `finalizar(CausaFin.ERROR_INTERNO)`: la partida termina de forma controlada. |
| `Juego.java:152` | `ArithmeticException` | División por cero en algún cálculo de la simulación | `finalizar(CausaFin.ERROR_INTERNO)`. |

```java
// Juego.java:148-154
try {
    ejecutarSimulacion(paso, controles);
} catch (IllegalStateException ex) {
    finalizar(CausaFin.ERROR_INTERNO);
} catch (ArithmeticException ex) {
    finalizar(CausaFin.ERROR_INTERNO);
}
```

Lección: en un motor de juego, una excepción en medio del bucle no debe «reventar» la
aplicación; se convierte en un **estado de partida terminada** (`ERROR_INTERNO`), que la
vista sabe representar.

### B) E/S de archivos: el juego funciona aunque no pueda guardar

| Fichero:línea | Excepción | Recuperación |
|---|---|---|
| `Marcador.java:54` | `NumberFormatException` | Línea corrupta en `puntajes.dat` → `continue`: se ignora esa línea y se sigue leyendo. |
| `Marcador.java:58` | `IOException` (leer) | `mejores.clear()` y `pilotos.clear()`: se empieza sin récords. |
| `Marcador.java:76` | `IOException` (escribir) | Mensaje a `System.err`: se pierde el récord nuevo, el juego continúa. |
| `Marcador.java:89` / `:100` | `IOException` (cerrar) | Mensaje a `System.err`. |
| `Preferencias.java:27` | `IOException` | `propiedades.clear()` → ajustes por defecto. |
| `Preferencias.java:29` | `IllegalArgumentException` | `propiedades.clear()` (archivo con formato inválido). |
| `Preferencias.java:41` | `IOException` (guardar) | `guardadas = false` + mensaje. |
| `Preferencias.java:66` | `NumberFormatException` | `getEntero` devuelve el valor por defecto. |

`Preferencias.cargar` usa **try-with-resources** (el `InputStream` se cierra solo):

```java
// Preferencias.java:25
try (InputStream entrada = Files.newInputStream(archivo)) {
    propiedades.load(entrada);
} catch (IOException ex) {
    propiedades.clear();
} catch (IllegalArgumentException ex) {
    propiedades.clear();
}
```

En `Marcador` se usa el patrón clásico `try / catch / finally` con `finally` para cerrar
(`cerrar(lector)` / `cerrarEscritor(escritor)`), que además encapsulan su propio `catch`.

### C) Audio: degradación elegante

`Sonido.java:70-84` captura **cinco** excepciones distintas, y las cuatro primeras comparten
el mismo cuerpo:

```java
} catch (javax.sound.sampled.LineUnavailableException ex) {
    disponible = false;
    return;
} catch (IllegalArgumentException ex) {
    disponible = false;
    return;
} catch (SecurityException ex) {
    disponible = false;
    return;
} catch (IllegalStateException ex) {
    disponible = false;
    return;
} catch (java.io.IOException ex) {
    disponible = false;
    return;
}
```

| Excepción | Causa real |
|---|---|
| `LineUnavailableException` | No hay tarjeta de sonido o la línea está ocupada. |
| `IllegalArgumentException` | Formato de audio no admitido. |
| `SecurityException` | Permisos del sistema. |
| `IllegalStateException` | El `Clip` no está abierto. |
| `IOException` | Fallo de E/S del dispositivo de audio. |

Todas responden a la misma decisión de diseño: **una bandera estática `disponible = false`**
desactiva el audio el resto de la sesión, sinExceptions encadenadas y sin repetir lógica
(`Sonido.isDisponible()` lo consulta `VentanaJuego`).

En Java 17 esto mismo podría escribirse con multi-catch
(`catch (LineUnavailableException | IllegalArgumentException | SecurityException | IllegalStateException | IOException ex)`),
pero el código actual mantiene los `catch` separados para ser más legible para un lector novel.

### D) Concurrencia: respetar la bandera de interrupción

| Fichero:línea | Excepción | Recuperación |
|---|---|---|
| `BucleJuego.java:116` | `InterruptedException` (de `Thread.sleep(8)`) | `Thread.currentThread().interrupt();` |
| `Sonido.java:105` | `InterruptedException` | `Thread.currentThread().interrupt();` |

```java
} catch (InterruptedException ex) {
    Thread.currentThread().interrupt();
}
```

Es **imprescindible** no «tragarse» la interrupción: si no se restaura la bandera con
`interrupt()`, nadie (ni el cierre de la ventana) puede detener el hilo limpiamente.

### E) Aislamiento del dibujado: un objeto roto no rompe el fotograma

En Swing, una excepción dentro de `paintComponent` se repite hasta 60 veces por segundo.
El proyecto la aísla en dos niveles:

| Fichero:línea | Recuperación |
|---|---|
| `PanelJuego.java:159` | Dibuja `"Error al dibujar: " + ex.getMessage()` en la esquina y `finally { g2.dispose(); }`. |
| `PanelJuego.java:209` | Un solo objeto que falla → `continue`: se dibujan los demás. |
| `PanelResultado.java:124` | Mensaje de error + `g2.dispose()`. |
| `PanelMenu.java:472` | `"Error en el menu: " + ex.getMessage()`. |
| `PanelMenu.java:630` | La tarjeta de vehículo afectada se sustituye por `"?"`. |
| `PanelMenu.java:706` | La opción afectada se sustituye por `"?"`. |
| `PanelMenu.java:745` | El botón afectado se sustituye por `"ERROR"`. |
| `Dibujante.java:440` | `return` sin dibujar la vista previa. |
| `PanelMenu.java:564` | Si falla `calcularEstadisticas`, las tres barras se fijan en `0.35`. |

```java
// PanelJuego.java:207-211 — un objeto que falla no impide pintar el resto
for (Dibujable item : lista) {
    try {
        dibujarObjeto(g, item);
    } catch (RuntimeException ex) {
        continue;
    }
}
```

Y el patrón `finally` para liberar el contexto gráfico, presente en los cinco
`paintComponent` del proyecto:

```java
try {
    ... dibujado ...
} catch (RuntimeException ex) {
    g2.setColor(Estilos.PELIGRO);
    g2.drawString("Error al dibujar: " + ex.getMessage(), 20, 40);
} finally {
    g2.dispose();      // ← se ejecuta siempre, también sin excepción
}
```

### F) La capa Swing: avisar al usuario y volver al menú

| Fichero:línea | Excepción | Recuperación |
|---|---|---|
| `PanelMenu.java:425` | `ExcepcionJuego` | `mostrarError(ex.getMessage() ...)`: el jugador ve por qué no puede empezar. |
| `PanelMenu.java:427` | `RuntimeException` | `mostrarError("Ocurrio un error al preparar la carrera")`. |
| `VentanaJuego.java:98` | `ExcepcionJuego` | `mostrarError(...)` + `mostrarMenu()`. |
| `VentanaJuego.java:101` | `RuntimeException` | `"Fallo inesperado al iniciar la carrera"` + `mostrarMenu()`. |
| `VentanaJuego.java:293` | `ExcepcionJuego` (preferencias inválidas) | `"Revisa las preferencias guardadas"` + configuración por defecto (`null`). |
| `VentanaJuego.java:205` | `RuntimeException` (sonido) | `sonidoActivo = false`. |
| `VentanaJuego.java:265` | `RuntimeException` (al cerrar) | Mensaje a `System.err`, y aun así `dispose()` + `System.exit(0)`. |

`VentanaJuego.iniciarPartida` es el ejemplo canónico de **traducción de excepción**:

```java
try {
    panelJuego = new PanelJuego(new Juego(configuracion), this);
    tarjetas.show(raiz, TARJETA_JUEGO);
    panelJuego.iniciar();
    panelJuego.requestFocusInWindow();
} catch (ExcepcionJuego ex) {
    mostrarError(ex.getMessage() == null ? "No se pudo iniciar la carrera" : ex.getMessage());
    mostrarMenu();
} catch (RuntimeException ex) {
    mostrarError("Fallo inesperado al iniciar la carrera");
    mostrarMenu();
}
```

La capa de presentación nunca deja que una excepción escape a Swing: **vuelve al menú**,
que siempre está operativo.

### G) El arranque: red de seguridad final

`Main.java`:

```java
public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            try {
                aplicarEstilo();
                new VentanaJuego().setVisible(true);
            } catch (RuntimeException ex) {
                error("Ocurrio un error inesperado: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    });          // ↑
    try { ... } catch (Throwable errorGrave) { ... }   // red exterior: JVM vs. usuario
}
```

Y el *Look and Feel* con doble plan B (`Main.java:37-46`):

```java
try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
} catch (Exception ex) {
    try {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception error) {
        System.err.println("Se usara el estilo por defecto de Swing");
    }
}
```

---

## 3.2 `BucleJuego`: `try` + `finally` con dos niveles de protección

`BucleJuego.run()` (`BucleJuego.java:87-130`) es el ejemplo más completo del proyecto:

```java
@Override
public void run() {
    long anterior = System.nanoTime();
    double acumulador = 0.0;
    try {
        while (activo) {
            ...
            pintar();
            if (juego.isFinalizado()) {
                terminado = true;
                break;
            }
            Thread.sleep(8L);
        }
    } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();          // 1. respetar la interrupción
    } catch (RuntimeException ex) {
        terminado = true;                           // 2. fallo del bucle → detener limpio
    } finally {
        activo = false;                              // 3. siempre se detiene
        if (alTerminar != null) {
            try {                                   // 4. try anidado: el callback
                alTerminar.run();                   //    no puede tumbar el cierre
            } catch (RuntimeException ex) {
                System.err.println("Error al cerrar el bucle de juego: " + ex.getMessage());
            }
        }
    }
}
```

Y `pintar()` aísla cada callback por separado (`BucleJuego.java:132-140`):

```java
for (Runnable accion : alPintar) {
    try {
        accion.run();
    } catch (RuntimeException ex) {
        System.err.println("Error al repintar: " + ex.getMessage());
    }
}
```

El `finally` garantiza que `activo = false` y `alTerminar.run()` se ejecuten **tanto si el
bucle termina normalmente como si hay excepción o interrupción**. Sin ese bloque, un fallo dejaría
el hilo vivo y la ventana no podría cerrarse limpiamente.

---

## 3.3 Resumen de estrategias

| Estrategia | Dónde | Ejemplo |
|---|---|---|
| **Propagar** (`throws`) | Constructores y métodos de dominio | `Juego(Configuracion) throws ExcepcionJuego`, `Generador(...)` (unchecked) |
| **Traducir** (envoltura) | Frontera entre capas | `Configuracion.java:95` convierte `IllegalStateException` en `ExcepcionJuego` |
| **Valores por defecto** | Ajustes corruptos | `Preferencias.getEntero`, `PanelMenu.calcularEstadisticas` → `0.35` |
| **Estado seguro** | Bucle de juego | `Juego` → `finalizar(CausaFin.ERROR_INTERNO)` |
| **Ignorar la fila inválida** | Lectura de archivos | `Marcador.java:54` → `continue` |
| **Desactivar la función** | Audio, sonido | `Sonido.disponible = false`, `VentanaJuego.sonidoActivo = false` |
| **Aislar el elemento** | Dibujado y callbacks | `continue` por objeto; `catch` por callback |
| **Restaurar la interrupción** | Hilos | `Thread.currentThread().interrupt()` |
| **Mostrar al usuario y volver** | Vista | `mostrarError(...)` + `mostrarMenu()` |
| **Limpiar en `finally`** | Recursos | `g2.dispose()`, cierre de lector/escritor |
| **Red exterior** | Arranque | `catch (Throwable)` en `Main.main` |

---

## 3.4 Reglas que sigue el proyecto

1. **No se traga excepciones sin motivo.** Cada `catch` registra, degrada o propaga.
2. **Se captura la excepción concreta**, no `Exception` genérico, salvo en los dos puntos de
   arranque (`Main`) donde el objetivo es no morir.
3. **`catch (InterruptedException)` siempre restaura la bandera** de interrupción.
4. **Los recursos se cierran en `finally`** o con try-with-resources.
5. **El modelo nunca importa a la vista**: las excepciones de presentación se capturan en
   `PanelMenu`, `PanelJuego`, `PanelResultado` y `VentanaJuego`.
6. **Los errores internos tienen causa de fin**: `CausaFin.ERROR_INTERNO` existe para que un
   fallo técnico termine la partida de forma ordenada y el jugador vea un resultado, no un volcado.

---

# 4. Ejercicios sugeridos

1. Añade un vehículo `Cuatrimotor` que extienda `Vehiculo` con 5 métodos abstractos nuevos.
   Comprueba que `TipoVehiculo.crear()` y `Dibujante.vehiculo` son los únicos sitios a tocar.
2. Cambia `Generador.actualizar` para que lance siempre `IllegalArgumentException` y observa
   cómo el juego sigue funcionando con la pista regenerándose (estrategia A).
3. Borra `puntajes.dat` y ejecuta el juego: `Marcador.cargar` captura el `IOException` y el
   marcador empieza vacío (estrategia B).
4. Sustituye `Sonido.tocar` por una llamada a un método inexistente y comprueba que
   `VentanaJuego.sonidoActivo` se desactiva sola sin caída de la aplicación.
5. Rompe `Juego.calcularPuntuacion` con una división por cero: la partida terminará con
   `ERROR_INTERNO` en lugar de propagar la excepción.

---

**Documentos relacionados**

- Diagramas y relaciones: [`UML.md`](UML.md)
- Manual de uso: [`Manual-de-usuario.md`](Manual-de-usuario.md)