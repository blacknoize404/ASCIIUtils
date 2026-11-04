# API — `io.github.blacknoize404.asciiutils`

## Clases

| Clase | Rol |
|---|---|
| `ASCIIFont` | Fuente ASCII: mapa carácter → glifo. |
| `ASCIIFonts` | Fuentes incluidas: `MINIMAL` (6 filas) y `MODERN` (8 filas), `final`. Letras acentuadas, `ñ`, dígitos y puntuación. |
| `ASCIIGlyph` | Glifo individual (internamente `char[][]`). |
| `ASCIIText` | Render de texto a ASCII art con opciones. |

## `ASCIIFont`

| Método | Descripción |
|---|---|
| `put(c, glyph)` | Registra un glifo (normaliza a minúscula según `isOnlyLowerCases`). |
| `getGlyph(c)` | Glifo del carácter; el de `'\0'` como respaldo si no está. |
| `contains(c)` / `size()` / `glyphs()` | Consulta y vista inmutable del mapa. |
| `getHeight()` | Altura de banda de la fuente. |
| `setOnlyLowerCases(bool)` | Activa/desactiva la normalización a minúsculas. |
| `from(Map)` *(static)* | Fuente desde un diccionario. |
| `load(Reader)` *(static)* | Carga una fuente desde texto (`c: cuerpo` por línea, `-` separa filas, `#` comentario). |
| `subset(chars)` | Nueva fuente conservando solo esos caracteres (+ `'\0'` y espacio). |

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
| `print()` | Imprime la representación. |
| `setKerning(n)` / `setLineHeight(n)` | Espacio entre letras / entre líneas. |
| `setBackground(BackgroundType)` | Relleno de los huecos: `BLANK`, `░▒▓█`. |
| `setAlignment(LEFT\|CENTER\|RIGHT)` | Alineación de cada línea en el ancho total. |
| `setMaxWidth(cols)` | Word-wrap: envuelve por palabras si se excede (0 = sin límite). |
| `setInk(c)` | Tinta propia: sustituye al `▓` del glifo (0 = original). |
| `setFramed(bool)` | Marco `┌─┐│└─┘` alrededor. |
| `setFlipHorizontal(bool)` / `setFlipVertical(bool)` | Espejado. |
| `getWidth()` / `getHeight()` | Dimensiones del texto en columnas/filas. |

Todos los setters devuelven `this` (encadenables) e invalidan la caché.

## module-info

```java
module io.github.blacknoize404.asciiutils {
    exports io.github.blacknoize404.asciiutils;
}
```

## Demo

`src/test/java/.../App.java` — calculadora de edad de consola que muestra el texto letra a letra (no forma parte del artefacto publicado).
