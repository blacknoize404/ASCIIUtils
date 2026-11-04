# Documentación de ASCIIUtils

## Contenido

- [API](api.md) — clases y uso.
- [CHANGELOG](../CHANGELOG.md) — historial de versiones.

## Resumen

Genera texto decorado en ASCII a partir de cadenas normales. Paquete `io.github.blacknoize404.asciiutils`. Modelo: `ASCIIFont` contiene `ASCIIGlyph`s (cargables desde texto con `load`, desde FIGlet `.flf` con `loadFlf`, y reducibles con `subset`); `ASCIIText` compone el resultado con kerning, smushing estilo FIGlet, interlineado, fondo, alineación, word-wrap, marco, tinta, espejado, rotación y color ANSI configurables; `ASCIIFonts` expone las fuentes incluidas (`MINIMAL`, `MODERN`) cargadas desde recursos del classpath; `Cli` proporciona un jar ejecutable.
