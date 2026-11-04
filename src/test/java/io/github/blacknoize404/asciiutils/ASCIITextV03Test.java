package io.github.blacknoize404.asciiutils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de las funcionalidades de la versión 0.3: FIGlet, smushing,
 * salida (lines/write/save), ANSI, rotación, fuentes de recursos y CLI.
 */
class ASCIITextV03Test {

    private static ASCIIFont tinyFont() {
        ASCIIFont f = new ASCIIFont();
        f.put('\0', ASCIIGlyph.from("?"));
        f.put(' ', ASCIIGlyph.from(" "));
        f.put('a', ASCIIGlyph.from("▓"));
        f.put('b', ASCIIGlyph.from("▓▓"));
        return f;
    }

    // ------------------------------------------------------------------
    // ASCIIFont: fallback, charWidth, save/load
    // ------------------------------------------------------------------

    @Test
    void fallbackConfigurable() {
        ASCIIFont f = tinyFont().setFallback('b');
        // 'z' no existe: cae al glifo de 'b' en vez del '\0'
        assertEquals("▓▓", ASCIIText.render("z", f));
    }

    @Test
    void charWidthDevuelveElAnchoDelGlifo() {
        ASCIIFont f = tinyFont();
        assertEquals(1, f.charWidth('a'));
        assertEquals(2, f.charWidth('b'));
        assertEquals(1, f.charWidth('z')); // respaldo \0 -> "?"
    }

    @Test
    void saveYLoadHacenRoundTrip() throws IOException {
        ASCIIFont original = ASCIIFonts.MINIMAL;
        StringWriter sw = new StringWriter();
        original.save(sw);
        ASCIIFont copia = ASCIIFont.load(new StringReader(sw.toString()));
        assertEquals(original.getHeight(), copia.getHeight());
        assertEquals(original.size(), copia.size());
        assertEquals(ASCIIText.render("hola", original), ASCIIText.render("hola", copia));
    }

    @Test
    void saveYLoadConservanDirectivas() throws IOException {
        ASCIIFont f = tinyFont().setFallback('b').setHardBlank('$');
        StringWriter sw = new StringWriter();
        f.save(sw);
        ASCIIFont copia = ASCIIFont.load(new StringReader(sw.toString()));
        assertEquals('b', copia.getFallback());
        assertEquals('$', copia.getHardBlank());
    }

    // ------------------------------------------------------------------
    // Fuentes de recursos
    // ------------------------------------------------------------------

    @Test
    void fuentesSeCarganDesdeElClasspath() {
        assertNotNull(ASCIIFonts.MINIMAL);
        assertNotNull(ASCIIFonts.MODERN);
        assertTrue(ASCIIFonts.MINIMAL.size() > 30);
        assertTrue(ASCIIFonts.MODERN.size() > 30);
        assertTrue(ASCIIText.render("ok", ASCIIFonts.MINIMAL).contains("▓"));
    }

    @Test
    void loadResourceDesconocidoFalla() {
        assertThrows(java.io.UncheckedIOException.class,
                () -> ASCIIFonts.loadResource("/fonts/inexistente.txt"));
    }

    // ------------------------------------------------------------------
    // FIGlet (.flf)
    // ------------------------------------------------------------------

    /**
     * Fuente FIGlet mínima: altura 3, hardblank '$', kerning (oldLayout 0).
     * Define solo espacio, '!' y '"' (códigos 32-34); el fichero se trunca
     * tras ellos, lo que el parser tolera.
     */
    private static String flfFixture() {
        return """
                flf2a$ 3 2 5 0 0
                   @
                   @
                   @@
                | @
                | @
                . @@
                   @
                   @
                   @@
                """;
    }

    @Test
    void flfParseaCabeceraYGlifos() throws IOException {
        ASCIIFont f = ASCIIFont.loadFlf(new StringReader(flfFixture()));
        assertEquals(3, f.getHeight());
        assertEquals('$', f.getHardBlank());
        assertFalse(f.isOnlyLowerCases());
        assertTrue(f.contains(' '));
        assertTrue(f.contains('!'));
        assertTrue(f.contains('"'));
        assertFalse(f.contains('#'));
    }

    @Test
    void flfRenderizaGlifos() throws IOException {
        ASCIIFont f = ASCIIFont.loadFlf(new StringReader(flfFixture()));
        String out = new ASCIIText("!", f).setKerning(0).toStringRepresentation();
        assertEquals("| \n| \n. ", out);
    }

    @Test
    void flfSinCabeceraFalla() {
        assertThrows(IOException.class,
                () -> ASCIIFont.loadFlf(new StringReader("esto no es flf\n")));
    }

    @Test
    void flfCabeceraIncompletaFalla() {
        assertThrows(IOException.class,
                () -> ASCIIFont.loadFlf(new StringReader("flf2a$ 3\n")));
    }

    @Test
    void flfTruncadaEnPrimerGlifoFalla() {
        String truncada = "flf2a$ 3 2 5 0 0\n"; // sin glifos
        assertThrows(IOException.class,
                () -> ASCIIFont.loadFlf(new StringReader(truncada)));
    }

    @Test
    void flfHardblankSeRenderizaComoEspacio() throws IOException {
        // glifo '!' cuyo trazo usa el hardblank '$': se muestra como espacio
        String flf = """
                flf2a$ 2 1 5 0 0
                   @
                   @@
                $|@
                $|@@
                """;
        ASCIIFont f = ASCIIFont.loadFlf(new StringReader(flf));
        String out = new ASCIIText("!", f).setKerning(0).toStringRepresentation();
        assertEquals(" |\n |", out); // $ -> espacio visible
    }

    @Test
    void flfLayoutDerivadoDeOldLayout() throws IOException {
        // oldLayout 0 -> kerning
        ASCIIFont f = ASCIIFont.loadFlf(new StringReader(flfFixture()));
        assertEquals(ASCIIText.SmushMode.KERNING, f.getSmushMode());
        // oldLayout 63 -> controlled con todas las reglas
        ASCIIFont g = ASCIIFont.loadFlf(new StringReader(flfFixture().replace(" 0 0\n", " 63 0\n")));
        assertEquals(ASCIIText.SmushMode.CONTROLLED, g.getSmushMode());
        assertEquals(63, g.getSmushRules());
    }

    // ------------------------------------------------------------------
    // Smushing
    // ------------------------------------------------------------------

    @Test
    void smushKerningJuntaEspacios() {
        // Dos glifos con espacio en la frontera: se solapan
        ASCIIFont f = new ASCIIFont();
        f.put('\0', ASCIIGlyph.from("?"));
        f.put('a', ASCIIGlyph.from("▓ "));
        f.put('b', ASCIIGlyph.from(" ▓"));
        String full = new ASCIIText("ab", f).setKerning(0).toStringRepresentation();
        assertEquals("▓  ▓", full); // sin solape: 4 cols
        String kerned = new ASCIIText("ab", f).setKerning(0)
                .setSmushMode(ASCIIText.SmushMode.KERNING).toStringRepresentation();
        assertEquals("▓▓", kerned); // los huecos se funden del todo: 2 cols
    }

    @Test
    void smushUniversalFusionaColisiones() {
        ASCIIFont f = new ASCIIFont();
        f.put('\0', ASCIIGlyph.from("?"));
        f.put('x', ASCIIGlyph.from("AB"));
        f.put('y', ASCIIGlyph.from("CD"));
        String out = new ASCIIText("xy", f).setKerning(0)
                .setSmushMode(ASCIIText.SmushMode.UNIVERSAL).toStringRepresentation();
        // sin espacios que solapar, universal solo puede colapsar la frontera B/C -> C
        assertEquals("ACD", out);
    }

    @Test
    void smushControlledReglaIguales() {
        ASCIIFont f = new ASCIIFont();
        f.put('\0', ASCIIGlyph.from("?"));
        f.put('x', ASCIIGlyph.from("A"));
        f.put('y', ASCIIGlyph.from("A"));
        String out = new ASCIIText("xy", f).setKerning(0)
                .setSmushMode(ASCIIText.SmushMode.CONTROLLED)
                .setSmushRules(Smusher.RULE_EQUAL)
                .toStringRepresentation();
        assertEquals("A", out); // A+A se fusionan por regla 1
    }

    @Test
    void smushControlledSinReglaNoFusiona() {
        ASCIIFont f = new ASCIIFont();
        f.put('\0', ASCIIGlyph.from("?"));
        f.put('x', ASCIIGlyph.from("A"));
        f.put('y', ASCIIGlyph.from("B"));
        String out = new ASCIIText("xy", f).setKerning(0)
                .setSmushMode(ASCIIText.SmushMode.CONTROLLED)
                .setSmushRules(Smusher.RULE_EQUAL)
                .toStringRepresentation();
        assertEquals("AB", out); // A!=B y no hay otra regla
    }

    @Test
    void smushControlledReglaBigX() {
        ASCIIFont f = new ASCIIFont();
        f.put('\0', ASCIIGlyph.from("?"));
        f.put('x', ASCIIGlyph.from("/"));
        f.put('y', ASCIIGlyph.from("\\"));
        String out = new ASCIIText("xy", f).setKerning(0)
                .setSmushMode(ASCIIText.SmushMode.CONTROLLED)
                .setSmushRules(Smusher.RULE_BIGX)
                .toStringRepresentation();
        assertEquals("|", out); // / + \ -> | (regla 5)
    }

    @Test
    void smushHardblankNoEsEspacio() {
        // El hardblank '$' cuenta como visible: no se puede solapar encima
        ASCIIFont f = new ASCIIFont().setHardBlank('$');
        f.put('\0', ASCIIGlyph.from("?"));
        f.put('x', ASCIIGlyph.from("$"));
        f.put('y', ASCIIGlyph.from("A"));
        String out = new ASCIIText("xy", f).setKerning(0)
                .setSmushMode(ASCIIText.SmushMode.KERNING).toStringRepresentation();
        // kerning no funde hardblank: queda $ + A, $ se pinta como espacio
        assertEquals(" A", out);
    }

    // ------------------------------------------------------------------
    // Salida: lines / write / save
    // ------------------------------------------------------------------

    @Test
    void linesDevuelveListaSinAnsi() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0).setColor(Ansi.RED);
        t.render("ab");
        List<String> lines = t.lines();
        assertEquals(1, lines.size());
        assertEquals("▓▓▓", lines.get(0));
        assertThrows(UnsupportedOperationException.class, () -> lines.add("x"));
    }

    @Test
    void writeVuelcaLaRepresentacion() throws IOException {
        StringWriter sw = new StringWriter();
        new ASCIIText("a", tinyFont()).setKerning(0).write(sw);
        assertEquals("▓\n", sw.toString());
    }

    @Test
    void saveEscribeFicheroUtf8(@TempDir Path dir) throws IOException {
        Path out = dir.resolve("arte.txt");
        new ASCIIText("ab", tinyFont()).setKerning(0).save(out);
        assertEquals("▓▓▓\n", Files.readString(out, StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------------
    // ANSI y gradiente
    // ------------------------------------------------------------------

    @Test
    void colorEnvuelveCadaLineaConReset() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0).setLineHeight(0)
                .setColor(Ansi.RED);
        String out = t.render("a\na");
        assertEquals(Ansi.RED + "▓" + Ansi.RESET + "\n" + Ansi.RED + "▓" + Ansi.RESET, out);
    }

    @Test
    void gradienteColoreaPorColumna() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0)
                .setGradient(255, 0, 0, 0, 0, 255);
        String out = t.render("aa"); // 3 columnas de tinta
        assertTrue(out.contains("38;2;255;0;0"));   // rojo al inicio
        assertTrue(out.contains("38;2;0;0;255"));    // azul al final
        assertTrue(out.endsWith(Ansi.RESET));
        assertEquals("▓▓", t.lines().get(0)); // lines() limpia los escapes
    }

    @Test
    void ansiParseNombresYHex() {
        assertEquals(Ansi.RED, Ansi.parse("red"));
        assertEquals(Ansi.rgb(255, 128, 0), Ansi.parse("#ff8000"));
        assertNull(Ansi.parse("noexiste"));
        assertArrayEquals(new int[]{0, 255, 255}, Ansi.parseRgb("cyan"));
        assertArrayEquals(new int[]{255, 128, 0}, Ansi.parseRgb("#ff8000"));
        assertNull(Ansi.parseRgb("xyz"));
    }

    // ------------------------------------------------------------------
    // Rotación
    // ------------------------------------------------------------------

    @Test
    void rotacionCwTransponeLaMatriz() {
        // "a a" -> "▓ ▓" (1x3) -> CW da 3 filas x 1 col
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0)
                .setRotation(ASCIIText.Rotation.CLOCKWISE);
        assertEquals("▓\n \n▓", t.render("a a"));
        // "aa" con kerning 0 -> "▓▓" (2 cols) -> CW: 2 filas de 1 col
        assertEquals("▓\n▓", t.render("aa"));
    }

    @Test
    void rotacionCcwYCwSonOpuestas() {
        // con glifos asimétricos se ve el sentido de cada giro
        ASCIIFont f = new ASCIIFont();
        f.put('\0', ASCIIGlyph.from("?"));
        f.put('a', ASCIIGlyph.from("A"));
        f.put('b', ASCIIGlyph.from("BC"));
        ASCIIText cw = new ASCIIText(f).setKerning(0)
                .setRotation(ASCIIText.Rotation.CLOCKWISE);
        ASCIIText ccw = new ASCIIText(f).setKerning(0)
                .setRotation(ASCIIText.Rotation.COUNTERCLOCKWISE);
        assertEquals("A\nB\nC", cw.render("ab"));   // el primero queda arriba
        assertEquals("C\nB\nA", ccw.render("ab"));  // el primero queda abajo
    }

    @Test
    void rotacion180InvierteFilasYColumnas() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0)
                .setRotation(ASCIIText.Rotation.UPSIDE_DOWN);
        // "ab" -> "▓▓▓" con a=1col,b=2col: 180 invierte orden -> "▓▓▓" igual por simetría
        // usa glifos distintos para comprobar
        ASCIIFont f = new ASCIIFont();
        f.put('\0', ASCIIGlyph.from("?"));
        f.put('a', ASCIIGlyph.from("A"));
        f.put('b', ASCIIGlyph.from("BC"));
        ASCIIText t2 = new ASCIIText(f).setKerning(0)
                .setRotation(ASCIIText.Rotation.UPSIDE_DOWN);
        assertEquals("CBA", t2.render("ab"));
    }

    // ------------------------------------------------------------------
    // Combinaciones y caché
    // ------------------------------------------------------------------

    @Test
    void cacheTrasCambiarColorYRotacion() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0);
        String plain = t.render("a");
        assertEquals("▓", plain);
        t.setColor(Ansi.GREEN);
        assertTrue(t.render("a").contains(Ansi.GREEN));
        t.setColor(null);
        t.setRotation(ASCIIText.Rotation.CLOCKWISE);
        assertEquals("▓", t.render("a")); // matriz 1x1 rota igual
    }

    @Test
    void combinadoInkMarcoFlipColorSmush() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0)
                .setInk('#')
                .setFramed(true)
                .setFlipHorizontal(true)
                .setColor(Ansi.CYAN)
                .setSmushMode(ASCIIText.SmushMode.KERNING);
        String out = t.render("ab");
        assertTrue(out.startsWith("┌"));
        assertTrue(out.contains(Ansi.CYAN));
        assertTrue(out.contains("#"));
        assertTrue(t.lines().stream().noneMatch(l -> l.contains("")));
    }

    // ------------------------------------------------------------------
    // CLI
    // ------------------------------------------------------------------

    @Test
    void cliHelpDevuelveCero() throws IOException {
        PrintStream old = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(bos, true, StandardCharsets.UTF_8));
            assertEquals(0, Cli.run(new String[]{"--help"}));
        } finally {
            System.setOut(old);
        }
        assertTrue(bos.toString(StandardCharsets.UTF_8).contains("--font"));
    }

    @Test
    void cliOpcionDesconocidaFalla() {
        assertThrows(IllegalArgumentException.class,
                () -> Cli.run(new String[]{"--inventada", "x"}));
    }

    @Test
    void cliOpcionSinValorFalla() {
        assertThrows(IllegalArgumentException.class,
                () -> Cli.run(new String[]{"--font"}));
    }

    @Test
    void cliOutGuardaElArte(@TempDir Path dir) throws IOException {
        Path out = dir.resolve("cli.txt");
        assertEquals(0, Cli.run(new String[]{"--font", "minimal", "--out", out.toString(), "hi"}));
        assertTrue(Files.readString(out, StandardCharsets.UTF_8).contains("▓"));
    }
}
