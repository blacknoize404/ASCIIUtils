# ASCIIUtils

Librería utilitaria para **generar texto decorado en ASCII** (ASCII art) a partir de cadenas de texto: fuentes y glifos propios.

- **Coordenadas:** `io.github.blacknoize404:ASCIIUtils:0.2-SNAPSHOT`
- **Java:** 21 · **Build:** Maven · **Tests:** JUnit 5 (20 tests)
- **Módulo JPMS:** `io.github.blacknoize404.asciiutils` — exporta `io.github.blacknoize404.asciiutils`

> Proyecto independizado desde `My Libraries/modules/` — se consume como dependencia Maven (`mvn install`).

## Estructura

```
src/main/java/io/github/blacknoize404/asciiutils/
├── ASCIIFont.java       → fuente: mapa carácter→glifo (put/get/load/subset)
├── ASCIIFonts.java      → fuentes incluidas: MINIMAL, MODERN (final)
├── ASCIIGlyph.java      → glifo individual (char[][], filas auto-rellenadas)
└── ASCIIText.java       → render: kerning, interlineado, fondo, alineación,
                           word-wrap, marco, tinta, espejado, caché

src/test/java/io/github/blacknoize404/asciiutils/
├── ASCIITextTest.java   → 20 tests
└── App.java             → demo de consola (no se publica)
```

## Uso como dependencia

```xml
<dependency>
    <groupId>io.github.blacknoize404</groupId>
    <artifactId>ASCIIUtils</artifactId>
    <version>0.2-SNAPSHOT</version>
</dependency>
```

```java
System.out.println(ASCIIText.render("Hola mundo", ASCIIFonts.MODERN));

ASCIIText t = new ASCIIText(ASCIIFonts.MINIMAL)
        .setKerning(2)
        .setBackground(ASCIIText.BackgroundType.PERCENT25)
        .setAlignment(ASCIIText.Alignment.CENTER)
        .setMaxWidth(40)     // word-wrap
        .setInk('#')         // tinta propia
        .setFramed(true);    // marco ┌─┐│└─┘
t.print();
```

Consumido por: [MazeGame](../MazeGame/README.md).

## Documentación

Ver [docs/index.md](docs/index.md).

## Licencia

Distribuido bajo [Apache License 2.0](LICENSE).
