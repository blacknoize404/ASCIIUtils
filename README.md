# ASCIIUtils

Librería utilitaria para **generar texto decorado en ASCII** (ASCII art) a partir de cadenas de texto: fuentes propias y compatibles con FIGlet (`.flf`).

- **Coordenadas:** `io.github.blacknoize404:ASCIIUtils:0.3` · **JitPack:** `com.github.blacknoize404:ASCIIUtils:v0.3`
- **Java:** 21 · **Build:** Maven · **Tests:** JUnit 5 (54 tests)
- **Módulo JPMS:** `io.github.blacknoize404.asciiutils` — exporta `io.github.blacknoize404.asciiutils`
- **CLI:** `java -jar ASCIIUtils.jar "Hola" --font modern --frame --color cyan`

> Proyecto independizado desde `My Libraries/modules/` — se consume como dependencia Maven o vía JitPack.

## Estructura

```
src/main/java/io/github/blacknoize404/asciiutils/
├── ASCIIFont.java       → fuente: put/get, load/save, loadFlf, subset, fallback, hardblank
├── ASCIIFonts.java      → fuentes incluidas: MINIMAL, MODERN (recursos del classpath)
├── ASCIIGlyph.java      → glifo individual (char[][], filas auto-rellenadas)
├── ASCIIText.java       → render: kerning, smushing FIGlet, fondo, alineación,
│                          word-wrap, marco, tinta, espejado, rotación, ANSI, caché
├── Smusher.java         → motor de smushing (kerning/universal/6 reglas) [interno]
├── Ansi.java            → códigos ANSI: colores básicos, 256, RGB, parse
└── Cli.java             → CLI ejecutable (Main-Class del jar)

src/main/resources/fonts/    → minimal.txt, modern.txt (formato propio)
src/test/java/...            → ASCIITextTest (20) + ASCIITextV03Test (34) + App (demo)
```

## Uso como dependencia

```xml
<dependency>
    <groupId>io.github.blacknoize404</groupId>
    <artifactId>ASCIIUtils</artifactId>
    <version>0.3</version>
</dependency>
```

```java
System.out.println(ASCIIText.render("Hola mundo", ASCIIFonts.MODERN));

ASCIIText t = new ASCIIText("Hola", ASCIIFonts.MINIMAL)
        .setKerning(2)
        .setBackground(ASCIIText.BackgroundType.PERCENT25)
        .setAlignment(ASCIIText.Alignment.CENTER)
        .setMaxWidth(40)                              // word-wrap
        .setInk('#')                                  // tinta propia
        .setFramed(true)                              // marco ┌─┐│└─┘
        .setColor(Ansi.CYAN)                          // color de terminal
        .setRotation(ASCIIText.Rotation.CLOCKWISE);   // 90°
t.print();
t.save(Path.of("salida.txt"));                      // o a fichero UTF-8
```

### Fuentes FIGlet

```java
ASCIIFont f = ASCIIFont.loadFlf(Files.newBufferedReader(Path.of("standard.flf")));
ASCIIText t = new ASCIIText("Hello", f)
        .setSmushMode(f.getSmushMode())   // layout declarado por la fuente
        .setSmushRules(f.getSmushRules());
t.print();
```

## CLI

```
java -jar ASCIIUtils.jar [opciones] texto...

  --font minimal|modern|ruta(.txt|.flf)   --align left|center|right
  --frame                                 --ink <c>            --background <c>
  --kerning <n>      --line-height <n>    --max-width <n>
  --flip-h           --flip-v             --rotate cw|ccw|180
  --smush none|kerning|universal|controlled
  --color nombre|#rrggbb                  --gradient c1:c2     --no-ansi
  --out <fichero>                         --help
```

## Documentación

Ver [docs/index.md](docs/index.md) y [CHANGELOG.md](CHANGELOG.md).

## Licencia

Distribuido bajo [Apache License 2.0](LICENSE).
