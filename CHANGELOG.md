# Changelog

## [0.3] — 2026-02-22

### Añadido

- **Soporte FIGlet**: `ASCIIFont.loadFlf(Reader)` carga fuentes `.flf`
  (cabecera `flf2a`, hardblank, endmarks, glifos ASCII 32–126 y caracteres
  alemanes). Las fuentes FIGlet declaran su layout y `getSmushMode()` /
  `getSmushRules()` lo resuelven con la semántica de `oldLayout`/`fullLayout`.
- **Smushing estilo FIGlet**: `Smusher` interno con los modos `KERNING`,
  `UNIVERSAL` y `CONTROLLED` (las seis reglas: iguales, subrayado, jerarquía,
  parejas opuestas, X grande y hardblank). Se activa con
  `ASCIIText.setSmushMode()` y `setSmushRules()`; el hardblank cuenta como
  carácter visible en las colisiones y se dibuja como espacio.
- **Salida flexible**: `lines()` (lista inmutable sin códigos ANSI),
  `write(Writer)` y `save(Path)` en UTF-8.
- **Color ANSI**: `setColor(String)` aplica un prefijo por línea con reset;
  `setGradient(r,g,b, r,g,b)` interpola un gradiente truecolor por columnas.
  Nueva clase `Ansi` con colores básicos, 256 colores, RGB y `parse`/`parseRgb`.
- **Rotación**: `Rotation.NONE/CLOCKWISE/COUNTERCLOCKWISE/UPSIDE_DOWN` vía
  `setRotation()`; las transformaciones se aplican sobre una copia de la
  matriz (ya no se muta la caché).
- **CLI ejecutable**: `java -jar ASCIIUtils.jar` (`io.github…asciiutils.Cli`)
  con opciones `--font`, `--align`, `--frame`, `--ink`, `--background`,
  `--kerning`, `--line-height`, `--max-width`, `--flip-h`, `--flip-v`,
  `--rotate`, `--smush`, `--color`, `--gradient`, `--no-ansi`, `--out`
  y `--help`.
- **Fuentes empaquetadas**: `MINIMAL` y `MODERN` viven en
  `src/main/resources/fonts/` (formato propio de `load`) y se cargan del
  classpath con `ASCIIFonts.loadResource()`.
- **`ASCIIFont`**: `setFallback(c)`, `setHardBlank(c)`, `setLayoutFlags(n)`,
  `getSmushMode()`, `getSmushRules()`, `charWidth(c)` y `save(Writer)`
  (round-trip con `load`).
- Fondo personalizable con carácter arbitrario: `setBackground(char)`.

### Cambiado

- El parser de `ASCIIFont.load` admite `c:cuerpo` y `c: cuerpo`, y las
  directivas `height:`, `fallback:`, `hardblank:` y `lowercase:`.
- Versión: `0.2-SNAPSHOT` → `0.3`.
- Las fuentes `MINIMAL`/`MODERN` dejan de ser código Java (≈1000 líneas) y
  pasan a recursos de texto.

### Deprecado

- `setRotated(boolean)` e `isRotated()` → `setRotation(Rotation)` /
  `getRotation()`.

## [0.2] — 2026-02

Versión con la refactorización inicial tras independizar el proyecto:
alineación, word-wrap, marco, tinta, espejado, `char[][]` defensivo,
caché corregida, `load(Reader)`/`subset()`/`contains()` y 20 tests.
