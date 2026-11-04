# API — `io.github.blacknoize404.asciiutils`

## Clases

| Clase | Rol |
|---|---|
| `ASCIIFont` | Fuente ASCII: mapa carácter → glifo; carga propia y FIGlet. |
| `ASCIIFonts` | Fuentes incluidas: `MINIMAL` (6 filas) y `MODERN` (8 filas), `final`, cargadas desde `resources/fonts/`. Letras acentuadas, `ñ`, dígitos y puntuación. |
| `ASCIIGlyph` | Glifo individual (internamente `char[][]`). |
| `ASCIIText` | Render de texto a ASCII art con opciones. |
| `Smusher` | *(interno)* Motor de smushing FIGlet. |
| `Ansi` | Códigos ANSI: `RESET`, básicos, `fg/bg(256)`, `rgb(r,g,b)`, `parse`, `parseRgb`. |
| `Cli` | CLI ejecutable (`java -jar`, Main-Class del jar). |

## `ASCIIFont`

| Método | Descripción |
|---|---|
| `put(c, glyph)` | Registra un glifo (normaliza a minúscula según `isOnlyLowerCases`). |
| `getGlyph(c)` | Glifo del carácter; el del `getFallback()` si no está. |
| `contains(c)` / `size()` / `glyphs()` | Consulta y vista inmutable del mapa. |
| `getHeight()` | Altura de banda de la fuente. |
| `charWidth(c)` | Ancho del glifo de un carácter (0 si no hay). |
| `setOnlyLowerCases(bool)` | Activa/desactiva la normalización a minúsculas. |
| `setFallback(c)` / `getFallback()` | Glifo de respaldo para caracteres no registrados (`'\0'` por defecto). |
| `setHardBlank(c)` / `getHardBlank()` | Carácter hardblank (visible al smushear, se dibuja como espacio). |
| `setLayoutFlags(n)` / `getLayoutFlags()` | Layout FIGlet declarado por la fuente. |
| `getSmushMode()` / `getSmushRules()` | Modo y reglas de smushing derivados del layout. |
| `from(Map)` *(static)* | Fuente desde un diccionario. |
| `load(Reader)` *(static)* | Carga una fuente desde texto: `c: cuerpo` o `c:cuerpo` por línea, `-` separa filas, `#` comentario, directivas `height:`/`fallback:`/`hardblank:`/`lowercase:`, `\0` para el respaldo. |
| `loadFlf(Reader)` *(static)* | Carga una fuente FIGlet `.flf` (cabecera `flf2a`, hardblank, endmarks, 95 ASCII + alemanes). |
| `save(Writer)` | Serializa la fuente en el formato de `load` (round-trip). |
| `subset(chars)` | Nueva fuente conservando solo esos caracteres (+ `'\0'` y espacio). |

## `ASCIIFonts`

| Método | Descripción |
|---|---|
| `MINIMAL` / `MODERN` | Fuentes incluidas (constantes `final`). |
| `loadResource(ruta)` *(static)* | Carga una fuente desde un recurso del classpath. |

## `ASCIIGlyph`

`ASCIIGlyph.from("▓▓▓-▓ ▓-...")` — `-` separa filas; las cortas se rellenan con espacios.
`width()`, `height()`, `toArray()` (copia), `toStringRepresentation()`.

## `ASCIIText`

| Método | Descripción |
|---|---|
| `render(text)` / `render(text, font)` *(static)* | Render directo (la estática no guarda estado). |
| `getOf(text)` | *Deprecated* → `render`. |
| `toStringRepresentation()` | Cadena ASCII del texto (cacheada; se regenera al cambiar opciones). |
| `toCharMatrix()` | Matriz generada (copia; `'\0'` = hueco). |
| `lines()` | Las líneas de salida como lista inmutable, sin códigos ANSI. |
| `write(Writer)` / `save(Path)` | Exporta la representación (UTF-8 en `save`). |
| `print()` | Imprime la representación. |
| `setKerning(n)` / `setLineHeight(n)` | Espacio entre letras / entre líneas. |
| `setBackground(BackgroundType)` / `setBackground(char)` | Relleno de los huecos: `BLANK`, `░▒▓█`, o carácter propio. |
| `setAlignment(LEFT\|CENTER\|RIGHT)` | Alineación de cada línea en el ancho total. |
| `setMaxWidth(cols)` | Word-wrap: envuelve por palabras si se excede (0 = sin límite). |
| `setInk(c)` | Tinta propia: sustituye al `▓` del glifo (0 = original). |
| `setFramed(bool)` | Marco `┌─┐│└─┘` alrededor. |
| `setFlipHorizontal(bool)` / `setFlipVertical(bool)` | Espejado. |
| `setSmushMode(SmushMode)` | `FULL_WIDTH` (defecto), `KERNING`, `UNIVERSAL`, `CONTROLLED`. |
| `setSmushRules(mask)` | Máscara de reglas FIGlet para `CONTROLLED` (`-1` = las de la fuente). |
| `setColor(ansiCode)` | Prefijo ANSI por línea con `RESET` (p.ej. `Ansi.RED`, `Ansi.rgb(...)`). |
| `setGradient(r1,g1,b1,r2,g2,b2)` / `clearGradient()` | Gradiente truecolor por columnas sobre la tinta. |
| `setRotation(Rotation)` | `NONE`, `CLOCKWISE`, `COUNTERCLOCKWISE`, `UPSIDE_DOWN`. |
| `setRotated(bool)` / `isRotated()` | *Deprecated* → `setRotation`. |
| `getWidth()` / `getHeight()` | Dimensiones del texto en columnas/filas. |

Todos los setters devuelven `this` (encadenables) e invalidan la caché.

## `Smusher` (interno)

Modos: `FULL_WIDTH` (concatena + kerning), `KERNING` (solapa mientras choquen
espacios), `UNIVERSAL` (gana el carácter de la derecha), `CONTROLLED` (máscara
de reglas). Reglas FIGlet (bits): `RULE_EQUAL` (1), `RULE_UNDERSCORE` (2),
`RULE_HIERARCHY` (4), `RULE_OPPOSITE` (8), `RULE_BIGX` (16), `RULE_HARDBLANK`
(32); `RULE_ALL` las activa todas. El hardblank nunca actúa como espacio.

## `Ansi`

| Miembro | Descripción |
|---|---|
| `RESET`, `BLACK`…`WHITE`, `GRAY`, `BOLD`, `DIM` | Constantes de escape. |
| `fg(n)` / `bg(n)` | Paleta de 256 colores. |
| `rgb(r,g,b)` | Truecolor de primer plano. |
| `parse(nombre)` | `red`, `cyan`… o `#rrggbb` → código ANSI (null si no existe). |
| `parseRgb(nombre)` | Ídem pero devuelve `{r,g,b}` (para gradientes). |

## `Cli`

`java -jar ASCIIUtils.jar [opciones] texto...` — ver `--help` o el README
para la lista completa de opciones (fuente, alineación, marco, tinta, fondo,
kerning, wrap, flips, rotación, smushing, color, gradiente, `--out`).

## module-info

```java
module io.github.blacknoize404.asciiutils {
    exports io.github.blacknoize404.asciiutils;
}
```

## Demo

`src/test/java/.../App.java` — calculadora de edad de consola que muestra el texto letra a letra (no forma parte del artefacto publicado).
