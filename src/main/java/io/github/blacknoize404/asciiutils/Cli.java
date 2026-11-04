package io.github.blacknoize404.asciiutils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Interfaz de línea de comandos de ASCIIUtils.
 * <p>
 * Uso:
 * </p>
 * <pre>
 *   java -jar ASCIIUtils.jar [opciones] texto...
 *
 *   --font &lt;nombre|ruta&gt;     minimal, modern, o ruta a un .txt/.flf
 *   --align &lt;left|center|right&gt;
 *   --frame                   dibuja un marco alrededor
 *   --ink &lt;char&gt;            carácter de tinta
 *   --background &lt;char&gt;     carácter de fondo
 *   --kerning &lt;n&gt;           espacio extra entre glifos
 *   --line-height &lt;n&gt;       filas extra entre líneas
 *   --max-width &lt;n&gt;         ancho máximo (word-wrap)
 *   --flip-h | --flip-v       volteo horizontal / vertical
 *   --rotate &lt;cw|ccw|180&gt;   rotación
 *   --smush &lt;none|kerning|universal|controlled&gt;
 *   --color &lt;nombre|#rrggbb&gt;
 *   --gradient &lt;c1&gt;:&lt;c2&gt;    degradado entre dos colores por fila
 *   --no-ansi                 desactiva los códigos de color
 *   --out &lt;fichero&gt;         guarda la salida en vez de imprimirla
 *   --help                    muestra esta ayuda
 * </pre>
 *
 * @author Barnés Inside
 * @version 0.3
 */
public final class Cli {

    private Cli() {
    }

    public static void main(String[] args) {
        try {
            System.exit(run(args));
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Usa --help para ver las opciones.");
            System.exit(2);
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
            System.exit(1);
        }
    }

    static int run(String[] args) throws IOException {
        Deque<String> queue = new ArrayDeque<>(List.of(args));
        List<String> text = new ArrayList<>();
        String fontRef = "minimal";
        String align = null, ink = null, background = null, rotate = null;
        String smush = null, color = null, gradient = null, out = null;
        Integer kerning = null, lineHeight = null, maxWidth = null;
        boolean frame = false, flipH = false, flipV = false, ansi = true;

        while (!queue.isEmpty()) {
            String arg = queue.poll();
            switch (arg) {
                case "--help", "-h" -> {
                    printHelp();
                    return 0;
                }
                case "--font" -> fontRef = require(queue, arg);
                case "--align" -> align = require(queue, arg);
                case "--frame" -> frame = true;
                case "--ink" -> ink = require(queue, arg);
                case "--background" -> background = require(queue, arg);
                case "--kerning" -> kerning = parseInt(require(queue, arg), arg);
                case "--line-height" -> lineHeight = parseInt(require(queue, arg), arg);
                case "--max-width" -> maxWidth = parseInt(require(queue, arg), arg);
                case "--flip-h" -> flipH = true;
                case "--flip-v" -> flipV = true;
                case "--rotate" -> rotate = require(queue, arg);
                case "--smush" -> smush = require(queue, arg);
                case "--color" -> color = require(queue, arg);
                case "--gradient" -> gradient = require(queue, arg);
                case "--no-ansi" -> ansi = false;
                case "--out" -> out = require(queue, arg);
                default -> {
                    if (arg.startsWith("--")) {
                        throw new IllegalArgumentException("Opción desconocida: " + arg);
                    }
                    text.add(arg);
                }
            }
        }

        if (text.isEmpty()) {
            printHelp();
            return 0;
        }

        ASCIIFont font = loadFont(fontRef);
        ASCIIText t = new ASCIIText(String.join(" ", text), font);
        if (align != null) {
            t.setAlignment(switch (align.toLowerCase()) {
                case "left" -> ASCIIText.Alignment.LEFT;
                case "center", "centre" -> ASCIIText.Alignment.CENTER;
                case "right" -> ASCIIText.Alignment.RIGHT;
                default -> throw new IllegalArgumentException("Alineación inválida: " + align);
            });
        }
        if (frame) t.setFramed(true);
        if (ink != null) t.setInk(oneChar(ink, "--ink"));
        if (background != null) t.setBackground(oneChar(background, "--background"));
        if (kerning != null) t.setKerning(kerning);
        if (lineHeight != null) t.setLineHeight(lineHeight);
        if (maxWidth != null) t.setMaxWidth(maxWidth);
        if (flipH) t.setFlipHorizontal(true);
        if (flipV) t.setFlipVertical(true);
        if (rotate != null) {
            switch (rotate.toLowerCase()) {
                case "cw", "90" -> t.setRotation(ASCIIText.Rotation.CLOCKWISE);
                case "ccw", "270" -> t.setRotation(ASCIIText.Rotation.COUNTERCLOCKWISE);
                case "180" -> t.setRotation(ASCIIText.Rotation.UPSIDE_DOWN);
                default -> throw new IllegalArgumentException("Rotación inválida: " + rotate);
            }
        }
        if (smush != null) {
            t.setSmushMode(switch (smush.toLowerCase()) {
                case "none", "full", "full-width" -> ASCIIText.SmushMode.FULL_WIDTH;
                case "kerning" -> ASCIIText.SmushMode.KERNING;
                case "universal" -> ASCIIText.SmushMode.UNIVERSAL;
                case "controlled" -> ASCIIText.SmushMode.CONTROLLED;
                default -> throw new IllegalArgumentException("Modo smush inválido: " + smush);
            });
        } else if (font.getSmushMode() != ASCIIText.SmushMode.FULL_WIDTH) {
            // Fuentes FIGlet: aplicar el layout que declara la propia fuente
            t.setSmushMode(font.getSmushMode());
            t.setSmushRules(font.getSmushRules());
        }
        if (ansi) {
            if (color != null) {
                String code = Ansi.parse(color);
                if (code == null) {
                    throw new IllegalArgumentException("Color inválido: " + color);
                }
                t.setColor(code);
            }
            if (gradient != null) {
                String[] parts = gradient.split(":");
                int[] c1 = parts.length > 0 ? Ansi.parseRgb(parts[0]) : null;
                int[] c2 = parts.length > 1 ? Ansi.parseRgb(parts[1]) : null;
                if (c1 == null || c2 == null) {
                    throw new IllegalArgumentException("Gradiente inválido: " + gradient + " (usa c1:c2)");
                }
                t.setGradient(c1[0], c1[1], c1[2], c2[0], c2[1], c2[2]);
            }
        }

        if (out != null) {
            t.save(Path.of(out));
            System.out.println("Guardado en " + out);
        } else {
            t.print();
        }
        return 0;
    }

    private static void printHelp() {
        System.out.println("""
                ASCIIUtils — render de texto a ASCII art.

                Uso: java -jar ASCIIUtils.jar [opciones] texto...

                  --font <nombre|ruta>      minimal, modern, o ruta a un .txt/.flf
                  --align <left|center|right>
                  --frame                   dibuja un marco alrededor
                  --ink <char>              carácter de tinta
                  --background <char>       carácter de fondo
                  --kerning <n>             espacio extra entre glifos
                  --line-height <n>         filas extra entre líneas
                  --max-width <n>           ancho máximo (word-wrap)
                  --flip-h | --flip-v       volteo horizontal / vertical
                  --rotate <cw|ccw|180>     rotación
                  --smush <none|kerning|universal|controlled>
                  --color <nombre|#rrggbb>  color ANSI
                  --gradient <c1>:<c2>      degradado por fila
                  --no-ansi                 desactiva los códigos de color
                  --out <fichero>           guarda la salida en vez de imprimirla
                  --help                    muestra esta ayuda
                """);
    }

    private static ASCIIFont loadFont(String ref) throws IOException {
        return switch (ref.toLowerCase()) {
            case "minimal" -> ASCIIFonts.MINIMAL;
            case "modern" -> ASCIIFonts.MODERN;
            default -> {
                Path p = Path.of(ref);
                try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                    yield ref.toLowerCase().endsWith(".flf")
                            ? ASCIIFont.loadFlf(r)
                            : ASCIIFont.load(r);
                } catch (java.nio.file.NoSuchFileException e) {
                    // Quizá es un recurso empaquetado
                    yield ASCIIFonts.loadResource(ref);
                }
            }
        };
    }

    private static String require(Deque<String> q, String opt) {
        String v = q.poll();
        if (v == null) {
            throw new IllegalArgumentException("La opción " + opt + " requiere un valor");
        }
        return v;
    }

    private static int parseInt(String v, String opt) {
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La opción " + opt + " requiere un entero: " + v);
        }
    }

    private static char oneChar(String v, String opt) {
        if (v.length() != 1) {
            throw new IllegalArgumentException("La opción " + opt + " requiere un único carácter: " + v);
        }
        return v.charAt(0);
    }
}
