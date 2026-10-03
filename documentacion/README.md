# Documentación — Gran Rally Turbo

Juego de carreras en Java (Swing) con 1 o 2 jugadores y rival de la computadora.

## Documentos

| Documento | Contenido |
|---|---|
| [`Manual-de-usuario.md`](Manual-de-usuario.md) | Instalación, menú, controles, reglas de carrera, vehículos, modos, récords y solución de problemas. |
| [`UML.md`](UML.md) | Diagramas de clases (Mermaid), paquetes, jerarquía, relaciones, patrones y secuencias. |
| [`POO-y-excepciones.md`](POO-y-excepciones.md) | Herencia, polimorfismo, sobrecarga, enums, interfaces y catálogo completo de `try-catch`. |
| [`diagramas/`](diagramas/) | Fuentes PlantUML (`.puml`) de los diagramas para exportar a PNG/SVG. |

## Diagramas PlantUML

| Fichero | Contenido |
|---|---|
| [`paquetes.puml`](diagramas/paquetes.puml) | Mapa de paquetes y dependencias. |
| [`modelo-vehiculos.puml`](diagramas/modelo-vehiculos.puml) | Jerarquía `Vehiculo` → `Carro`, `Moto`, `Camion`, `Rival`. |
| [`modelo-nucleo.puml`](diagramas/modelo-nucleo.puml) | Núcleo del modelo: `Juego`, `Jugador`, `Pista`, `Generador`, efectos, configuración y enums. |
| [`vista-control.puml`](diagramas/vista-control.puml) | Vista Swing y control: `VentanaJuego`, paneles, `BucleJuego`, `EntradaTeclado`, `Dibujante`, `Estilos`. |
| [`observer.puml`](diagramas/observer.puml) | Patrón Observer entre modelo y vista. |
| [`secuencia-bucle.puml`](diagramas/secuencia-bucle.puml) | Secuencia del bucle de juego a 60 FPS. |
| [`secuencia-colision.puml`](diagramas/secuencia-colision.puml) | Inicio de partida, colisión con un cono y fin de partida. |
| [`maquina-estados.puml`](diagramas/maquina-estados.puml) | Máquina de estados de la partida. |

## Cómo se ejecuta el juego

```powershell
jugar.bat
```

Más detalles en el [manual de usuario](Manual-de-usuario.md#1-requisitos-e-instalación).

## Estructura del código

```
src/carreras/
├── Main.java                 punto de entrada
├── modelo/                   dominio puro (sin Swing)
│   ├── Vehiculo.java         clase abstracta base
│   ├── Carro/Moto/Camion     vehículos del jugador
│   ├── Rival.java            vehículo con IA
│   ├── Juego.java            motor de la carrera
│   ├── Jugador.java          piloto + estadísticas
│   ├── Pista/Obstaculo/Recurso
│   ├── Generador.java        creación de conos y recursos
│   ├── GestorEfectos.java    partículas
│   ├── Configuracion.java    ajustes de partida
│   ├── Marcador.java         récords en puntajes.dat
│   ├── ObservableJuego.java  interfaz Observer
│   ├── ExcepcionJuego.java   excepción de dominio
│   └── *                     enums (Modalidad, ModoJuego, Dificultad, TipoVehiculo,
│                             EstadoJuego, EventoJuego, CausaFin)
├── control/
│   ├── BucleJuego.java       hilo de 60 FPS con paso fijo
│   └── EntradaTeclado.java   adaptador de teclado por jugador
├── vista/
│   ├── VentanaJuego.java     CardLayout menú → juego → resultado
│   ├── PanelMenu.java        configuración de la partida
│   ├── PanelJuego.java       escena y HUD
│   ├── PanelResultado.java   resumen final
│   ├── Dibujante.java        dibujado procedural con Graphics2D
│   └── Estilos.java          paleta, fuentes y utilidades de dibujo
└── util/
    ├── Preferencias.java     preferencias.dat
    └── Sonido.java           tonos sintetizados (enum Tono anidado)
```