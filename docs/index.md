# Documentación de ASCIIUtils

## Contenido

- [API](api.md) — clases y uso.

## Resumen

Genera texto decorado en ASCII a partir de cadenas normales. Paquete `io.github.blacknoize404.asciiutils`. Modelo: `ASCIIFont` contiene `ASCIIGlyph`s (también cargables desde texto con `load` y reducibles con `subset`); `ASCIIText` compone el resultado con kerning, interlineado, fondo, alineación, word-wrap, marco, tinta y espejado configurables; `ASCIIFonts` registra las fuentes incluidas (`MINIMAL`, `MODERN`).
