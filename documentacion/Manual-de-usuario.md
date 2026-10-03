# Manual de usuario — Gran Rally Turbo

Juego de carreras en Java, para **1 o 2 jugadores** en el mismo teclado, con un rival de la
computadora. Funciona sin conexión y sin instalar nada más que Java.

---

## 1. Requisitos e instalación

| Requisito | Detalle |
|---|---|
| Java | JDK o JRE **17 o superior** (probado con el JDK de IntelliJ IDEA 2026.2). |
| Sistema | Windows, macOS o Linux (usa Swing, que viene con Java). |
| Bibliotecas | Ninguna. El juego no descarga nada. |
| Espacio | Muy poco: el juego entero ocupa unos pocos cientos de kilobytes. |

### Opción A — Ejecutar con doble clic (Windows)

1. Descomprime el proyecto en una carpeta.
2. Comprueba que tienes `jugar.bat` en la raíz del proyecto.
3. Haz doble clic en `jugar.bat`.

`jugar.bat` compila el código fuente y abre el juego automáticamente. Si tu Java está en el
`PATH`, no hay que configurar nada.

### Opción B — NetBeans / IntelliJ IDEA

1. **NetBeans:** *Archivo → Abrir proyecto* y selecciona la carpeta del proyecto
   (contiene `nbproject` y `build.xml`). Pulsa **F6** o el botón *Ejecutar*.
2. **IntelliJ IDEA:** *Open* la carpeta y ejecuta la clase `carreras.Main`.
   Si el JDK no está configurado, añádelo en *Settings → Build → Build Tools → Gradle/JDK*
   (o en *Project Structure → SDKs*) y marca el módulo con el nivel 17.

### Opción C — Línea de comandos

```powershell
# Windows (PowerShell) — ajusta la ruta si tu java está en otro sitio
$jdk = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2\jbr\bin"
& "$jdk\javac" -encoding UTF-8 -d build\classes (Get-ChildItem -Recurse -Filter *.java src).FullName
& "$jdk\java" -Dfile.encoding=UTF-8 -cp build\classes carreras.Main
```

### Archivos que crea el juego

| Archivo | Contenido | Se puede borrar |
|---|---|---|
| `preferencias.dat` | Modalidad, modo, dificultad, vehículos y nombres elegidos. | Sí, se regenera. |
| `puntajes.dat` | Récords por piloto, modalidad y modo. | Sí, se pierden los récords. |

Ambos se guardan en la carpeta desde la que se ejecuta el juego. Si están corruptos, el juego
no falla: los vuelve a crear con valores por defecto.

---

## 2. Pantalla de menú

Al abrir el juego aparece el menú, dibujado con un tamaño fijo de **940 × 800 píxeles**.
De arriba abajo:

1. **Marcador** — récords actuales por modalidad, modo y piloto.
2. **Tarjetas de vehículo** — `CARRO`, `MOTO`, `CAMIÓN`. Cada tarjeta muestra una vista previa
   y tres barras:
   - **VEL** = velocidad máxima,
   - **ACC** = aceleración,
   - **RES** = resistencia.
   Haz clic para seleccionar. Las barras muestran valores comparados entre los tres vehículos.
3. **Modalidad** — decide cuántos jugadores y si hay rival de PC:

   | Modalidad | Jugadores humanos | Rival PC | Teclas del 2.º jugador |
   |---|---|---|---|
   | **2 Jugadores** | 2 | No | Se muestra el recordatorio `I K L J` |
   | **1 vs PC** | 1 | 1 | No se muestra |
   | **2 Jugadores + PC** | 2 | 1 | Se muestra el recordatorio `I K L J` |

4. **Vehículo del jugador 2** — solo aparece en las modalidades con dos jugadores humanos.
5. **Modo de juego** — `CARRERA` o `INFINITO` (ver sección 4).
6. **Dificultad** — `Fácil`, `Normal` o `Difícil` (ver sección 5).
7. **Nombres** — hasta **16 caracteres** por piloto. El del jugador 1 se rellena con `Piloto 1`
   y el del jugador 2 con `Piloto 2`. Si dejas el campo vacío, se usa ese nombre por defecto.
8. **Sonido** — casilla para activar o desactivar los efectos de sonido.
9. **Botones** — `JUGAR` y `BORRAR RÉCORDS`.

Todo se guarda automáticamente: si cierras el juego, la próxima vez abre el menú con la
última configuración válida.

> **Toca la ventana con el ratón** una vez dentro de la partida si las teclas no responden:
> Swing necesita el foco para recibir el teclado.

---

## 3. Controles

### Jugador 1

| Acción | Teclas |
|---|---|
| Acelerar | `↑` o `W` |
| Frenar / marcha atrás | `↓` o `S` |
| Carril izquierdo | `←` o `A` |
| Carril derecho | `→` o `D` |

### Jugador 2 (solo en modalidades con 2 jugadores)

| Acción | Tecla |
|---|---|
| Acelerar | `I` |
| Frenar / marcha atrás | `K` |
| Carril izquierdo | `J` |
| Carril derecho | `L` |

### Teclas comunes

| Tecla | Acción |
|---|---|
| `ESC` o `P` | Pausar / reanudar |
| `ENTER` | En pausa: reanudar. Con la partida terminada: ver el resultado |
| `R` | Reiniciar (solo con la partida terminada) |
| `M` | Volver al menú |
| `Q` | Abandonar la carrera |

> Si eliges el mismo vehículo para los dos jugadores, el juego les aplica automáticamente
> un color distinto a cada uno para que los distingas.

---

## 4. Modos de juego

### 4.1 CARRERA

- Hay una **meta a 3500 metros**.
- Empiezas con el depósito **lleno (100 %)**.
- La partida acaba cuando **todos** los participantes terminan: llega a la meta o queda
  incapacitado.
- **Sí hay vehículo de PC** en las modalidades `1 vs PC` y `2 Jugadores + PC`.
- Al cruzar la meta aparece la pantalla de resultados con el cartel de victoria.

### 4.2 INFINITO

- **No hay meta**: la distancia no tiene límite.
- La pista se vuelve más difícil con el tiempo: aparecen más conos y los recursos se
  separan más.
- La puntuación sube conforme avanzas: los puntos por metro se multiplican por
  `1 + distancia / 12000`.
- **No hay vehículo rival** en este modo, aunque elijas `1 vs PC` o `2 Jugadores + PC`.
  El reto es sobrevivir: esquivar, recoger combustible y mantener la máquina viva.
- Se puede jugar con un jugador (récord personal) o con dos jugadores (partida por puntos).

---

## 5. Dificultad

| Dificultad | Densidad de conos | Velocidad del rival | Puntos |
|---|---|---|---|
| **Fácil** | ×0.65 | ×0.82 | ×0.75 |
| **Normal** | ×1.00 | ×1.00 | ×1.00 |
| **Difícil** | ×1.45 | ×1.22 | ×1.40 |

Además del efecto en la pista, la dificultad cambia el ritmo al que se acumulan los
puntos: en `Difícil` puntúas más por lo mismo, pero la carretera es bastante más exigente.

---

## 6. Los vehículos

| | Carro | Moto | Camión |
|---|---|---|---|
| Velocidad máxima | 62.0 | **68.0** | 52.0 |
| Aceleración | 24.0 | **28.0** | 14.0 |
| Frenado | **36.0** | 30.0 | 22.0 |
| Consumo | 1.7 | **1.0** | 2.5 |
| Agilidad de carril | 3.4 | **4.8** | 2.0 |
| Resistencia | 100.0 | 75.0 | **160.0** |
| Ancho relativo | 0.72 | 0.42 | **0.92** |
| Daño recibido | Normal | Normal | **Reducido un 30 %** |

Traducción práctica:

- **Moto**: la más rápida y la más ágil, pero se rompe antes y golpea con frecuencia
  cualquier cono por ser estrecha.
- **Carro**: el equilibrio. Buena en todo y sin sorpresas.
- **Camión**: lenta y torpe al cambiar de carril, pero aguanta casi todo y frena poco
  (tarda más en detenerse). Ocupa mucho carril: cuidado con los conos.

---

## 7. Cómo funciona una carrera

### 7.1 El inicio

1. Pulsas `JUGAR` en el menú.
2. El juego se crea y aparece una **cuenta atrás de 3 segundos**.
3. Cuando termina, empieza la carrera.

### 7.2 El circuito

- La carretera tiene **4 carriles**.
- El circuito no está cerrado: el juego genera los obstáculos y los recursos a medida que
  avanzas, en función de tu velocidad y de la dificultad.
- Los **conos naranjas** son el único tipo de obstáculo.
- Los **bidones verdes** son combustible: al recoger uno recuperas **22 puntos** de
  depósito.

### 7.3 Lo que ocurre durante la carrera

| Evento | Efecto |
|---|---|
| **Aceleras** | Ganas velocidad y gastas combustible. |
| **No haces nada** | El vehículo desacelera poco a poco. |
| **Frenas** | Pierdes velocidad rápidamente. |
| **Cambias de carril** | El cambio es progresivo: hay que mantener pulsada la tecla hasta completarlo. |
| **Golpeas un cono** | Pierdes **6 puntos de salud** (4.2 si eres Camión) y pierdes más de la mitad de la velocidad: el impacto deja el 55 % y la penalización por golpe aplica además un 70 %. |
| **Rozas a la PC** | Si compartís carril, pierdes salud poco a poco (5 puntos por segundo). |
| **Recoges un bidón** | +22 de combustible y **+60 puntos**. |
| **Adelantas a la PC** | **+150 puntos** y aumenta tu racha. |

### 7.4 Estado del vehículo

- **Salud**: empieza al 100 % (Camión: 160). Si baja de 35, el motor responde peor; si baja
  de 30, el vehículo se marca como **averiado** (pierde un 45 % de potencia y suelta humo).
  Si llega a 0, la partida termina para ti por `FALLO MECÁNICO`.
- **Combustible**: empieza al 100 %. Si llega a 0, no puedes acelerar. Si la velocidad baja
  de 1 mientras no tienes combustible, la partida termina por `SIN COMBUSTIBLE`.
- **Rozar la meta** no es instantáneo: hay que cruzar la línea de los 3500 m.

### 7.5 Puntuación

```
puntos = velocidad × 1.4 × dt × factor_dificultad × factor_vehículo × factor_modo
```

- Solo se puntúa si vas **más de 5 m/s**.
- `factor_vehículo`: el Camión puntúa algo menos por metro, pero como aguanta y acelera
  menos, compensa con los bonus.
- `factor_modo`: `1.0` en Carrera; en Infinito crece con la distancia.
- Cada jugador tiene **su propio marcador**, aunque compartan pantalla.

### 7.6 Fin de la partida

| Causa | Cuándo ocurre |
|---|---|
| **META** | Alcanzas los 3500 m (solo en Carrera). |
| **FALLO MECÁNICO** | Tu salud llega a 0. |
| **SIN COMBUSTIBLE** | Te quedas sin gasolina y el vehículo se detiene. |
| **ABANDONO** | Pulsas `Q` o vuelves al menú con `M`. |
| **ERROR INTERNO** | Error inesperado del simulador. |

La partida **no termina** en cuanto un jugador acaba: el juego espera a que acaben todos.
Si alguno llegó a la meta, el resultado global es `VICTORIA`; si no, `DERROTA`.

---

## 8. El rival de la computadora

- Tiene nombre y color propios: **Rayo** (azul), **Cola** (verde), **Furia** (morado),
  **Tronco** (naranja) o **Vela** (rojo).
- Su velocidad base es `26 + factor_dificultad × 18`, con una variación aleatoria de hasta
  `+6`. Arranca un poco por delante del jugador (10 m) y **no gasta combustible**.
- **Conduce solo**: si ve un cono en su carril, frena y busca un carril libre; si no ve
  peligro, cambia de carril al azar de vez en cuando.
- Su posición en pantalla se calcula respecto al jugador: si va más rápido que tú, se aleja
  hacia arriba; si va más lento, se acerca.
- En la pantalla de resultado aparece un bloque con su **nombre, puesto, distancia y
  velocidad**.
- Los rivales no participan en el modo Infinito.

---

## 9. Pantalla de resultados

Al terminar (o pulsar `ENTER` si la partida ya ha acabado) aparece un resumen con:

- **Por cada jugador**: posición final, distancia recorrida, tiempo, velocidad máxima,
  salud restante, combustible, puntos, racha máxima, golpes recibidos, adelantamientos,
  recursos recogidos y su **récord personal** para esa modalidad y modo.
- **Bloque `VEHÍCULOS DE LA PC`**: rival, puesto, distancia y si ha terminado.
- Si alguien supera su récord, se avisa con `¡NUEVO RÉCORD!`.
- Dos botones: `OTRA VEZ` (vuelve a jugar con la misma configuración) y `MENÚ`.

---

## 10. Récords

- Se guardan por **modo + modalidad + nombre de piloto** (en mayúsculas), de modo que cada
  piloto tiene su propio récord en cada combinación de juego.
- El menú muestra el mejor récord de cada modalidad y modo, y también el total por
  modalidad.
- Se borran con el botón `BORRAR RÉCORDS` del menú (con confirmación).

---

## 11. Sonido

Los efectos se **sintetizan por software** con el propio Java (sin archivos de audio):
clic, choque, recogida, adelantamiento, cuenta atrás, victoria y derrota. No es necesario
tener ningún recurso externo.

Se pueden desactivar en el menú. Si el equipo no tiene tarjeta de sonido o la está usando
otro programa, el juego desactiva el audio automáticamente y sigue funcionando con normalidad.

---

## 12. Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| «ClassNotFoundException» o «no se encuentra la clase principal» | `java` no está en el `PATH` | Usa `jugar.bat` o compila indicando la ruta completa de `javac`. |
| Las teclas no hacen nada | La ventana no tiene el foco | Haz clic dentro de la ventana de juego. |
| El juego se abre con otro tamaño | El sistema tiene escalado de pantalla | Se dibuja siempre a 940 × 800; agranda la ventana si es menor. |
| No se oye nada | No hay dispositivo de audio o está desactivado | Marca la casilla de sonido; el juego sigue funcionando sin audio. |
| «Revisa las preferencias guardadas» | `preferencias.dat` dañado o incompatible | Borra `preferencias.dat` y vuelve a abrir el juego. |
| Los récords no aparecen | `puntajes.dat` no se pudo leer o guardar | Comprueba los permisos de escritura de la carpeta y borra el archivo para regenerarlo. |
| Un jugador va mucho más rápido que el otro | Vehículos y dificultades distintos | El juego es justo por diseño; si quieres igualdad, elige el mismo vehículo y `Normal`. |
| En Infinito no aparece la PC | El modo Infinito no genera rivales | Juega en modo `Carrera` si quieres competir contra la PC. |
| El juego no arranca con doble clic | Falta Java o el antivirus bloquea el `.bat` | Ejecuta `jugar.bat` desde una terminal para ver el mensaje de error. |

---

## 13. Consejos para jugar bien

1. **No mires solo los conos del frente**: mira también los carriles de al lado. El Camión
   tarda más en cambiar de carril, así que decide con antelación.
2. **Recoge combustible aunque te veas lleno**: el depósito nunca supera el 100 %, pero el
   siguiente bidón puede llegar justo.
3. **Frena antes de girar**, no mientras cambias de carril: perder velocidad en un cono es
   la causa número uno de quedarse sin carrera.
4. **Aprovecha los adelantamientos**: +150 puntos y subir la racha son la forma más
   rápida de subir en la clasificación en Carrera.
5. **En Difícil compensa con paciencia**: la densidad de conos es ×1.45, pero los puntos son
   ×1.40; si te breaks con frecuencia, prueba `Normal`.
6. **Practica en Infinito**: es el mejor lugar para aprender los patrones de aparición de
   conos sin el presión de la meta.

---

**Documentos relacionados**

- Diagramas, clases y relaciones: [`UML.md`](UML.md)
- Herencia, polimorfismo y `try-catch`: [`POO-y-excepciones.md`](POO-y-excepciones.md)