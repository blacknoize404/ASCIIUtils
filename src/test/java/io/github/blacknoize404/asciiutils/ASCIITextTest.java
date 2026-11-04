package io.github.blacknoize404.asciiutils;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class ASCIITextTest {

    private static ASCIIFont tinyFont() {
        // Fuente mínima de 1×3 para tests deterministas: a="▓", b="▓▓", espacio 1 col, \0="?"
        ASCIIFont f = new ASCIIFont();
        f.put('\0', ASCIIGlyph.from("?"));
        f.put(' ', ASCIIGlyph.from(" "));
        f.put('a', ASCIIGlyph.from("▓"));
        f.put('b', ASCIIGlyph.from("▓▓"));
        return f;
    }

    @Test
    void renderBasicoComponeGlifos() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0);
        assertEquals("▓▓▓", t.render("ab")); // a=▓ + b=▓▓
    }

    @Test
    void kerningSeparaCaracteres() {
        ASCIIText t = new ASCIIText(tinyFont());
        assertEquals("▓ ▓", t.render("aa")); // kerning=1 por defecto
    }

    @Test
    void mayusculasSeNormalizan() {
        assertEquals("▓", ASCIIText.render("A", tinyFont()));
    }

    @Test
    void caracterDesconocidoUsaRespaldo() {
        assertEquals("?", ASCIIText.render("z", tinyFont()));
    }

    @Test
    void textoVacioDevuelveCadenaVacia() {
        ASCIIText t = new ASCIIText(tinyFont());
        assertEquals("", t.render(""));
        assertEquals("▓", t.render("a")); // tras texto vacío, la instancia sigue funcionando
    }

    @Test
    void multilineaApilaBandas() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0).setLineHeight(0);
        assertEquals("▓\n▓", t.render("a\na"));
        assertEquals(2, t.getHeight());
    }

    @Test
    void lineHeightInsertaFilasVacias() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0).setLineHeight(1);
        assertEquals("▓\n \n▓", t.render("a\na"));
        assertEquals(3, t.getHeight());
    }

    @Test
    void alineacionDerechaYCentro() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0).setLineHeight(0);
        t.setAlignment(ASCIIText.Alignment.RIGHT);
        assertEquals(" ▓\n▓▓", t.render("a\nb")); // línea corta pegada a la derecha del ancho máx (2)
        t.setAlignment(ASCIIText.Alignment.CENTER);
        assertEquals("▓ \n▓▓", t.render("a\nb")); // (2-1)/2 = 0: queda a la izquierda
    }

    @Test
    void fondoRellenaHuecos() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0).setLineHeight(0)
                .setBackground(ASCIIText.BackgroundType.PERCENT25);
        // b = "▓▓" ancho 2, a = "▓" ancho 1 -> la línea corta deja un hueco al fondo... no:
        // la matriz se dimensiona al ancho máx y el hueco sobrante es '\0' -> ░
        assertEquals("▓░\n▓▓", t.render("a\nb"));
    }

    @Test
    void wordWrapDividePorAncho() {
        // fuente tiny: "a a" mide 1+1(k)+1+2k... con kerning 0: a=1, ' '=1, a=1 -> 3
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0).setLineHeight(0).setMaxWidth(2);
        String out = t.render("a a");
        assertEquals(2, out.split("\n").length);
    }

    @Test
    void marcoEnvuelveElTexto() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0).setFramed(true);
        String out = t.render("a");
        String[] lines = out.split("\n");
        assertEquals(3, lines.length);
        assertTrue(lines[0].startsWith("┌"));
        assertTrue(lines[1].startsWith("│") && lines[1].endsWith("│"));
        assertTrue(lines[2].startsWith("└"));
    }

    @Test
    void tintaSustituyeElRelleno() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0).setInk('@');
        assertEquals("@", t.render("a"));
    }

    @Test
    void flipHorizontalInvierteColumnas() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0).setLineHeight(0).setFlipHorizontal(true);
        assertEquals(" ▓\n▓▓", t.render("a\nb")); // filas "▓ " y "▓▓" -> " ▓" y "▓▓"
    }

    @Test
    void flipVerticalInvierteFilas() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0).setLineHeight(0).setFlipVertical(true);
        assertEquals("▓▓\n▓ ", t.render("a\nb")); // filas "▓ " y "▓▓" -> "▓▓" y "▓ "
    }

    @Test
    void cacheSeInvalidaAlCambiarTexto() {
        ASCIIText t = new ASCIIText(tinyFont()).setKerning(0);
        assertEquals("▓", t.render("a"));
        assertEquals("▓▓", t.render("b")); // tras setText la matriz/representación se regenera
        char[][] m = t.toCharMatrix();
        assertEquals(2, m[0].length);
    }

    @Test
    void glifoFilasDesigualesSeRellena() {
        ASCIIGlyph g = ASCIIGlyph.from("▓-▓▓▓-▓");
        assertEquals(3, g.width());
        assertEquals("▓  \n▓▓▓\n▓  ", g.toStringRepresentation());
    }

    @Test
    void fontPutNormalizaMayusculas() {
        ASCIIFont f = tinyFont();
        f.put('C', ASCIIGlyph.from("█"));
        assertTrue(f.contains('c'));
        assertEquals("█", f.getGlyph('c').toStringRepresentation());
        assertEquals("█", f.getGlyph('C').toStringRepresentation());
    }

    @Test
    void subsetConservaSoloLosCaracteres() {
        ASCIIFont sub = tinyFont().subset("a");
        assertTrue(sub.contains('a'));
        assertFalse(sub.contains('b'));
        assertTrue(sub.contains(' '));  // el espacio y \0 siempre se conservan
    }

    @Test
    void cargaFuenteDesdeReader() throws Exception {
        ASCIIFont f = ASCIIFont.load(new StringReader("# comentario\na:▓\n : \n?:?\n"));
        assertTrue(f.contains('a'));
        assertTrue(f.contains(' '));
        assertEquals("▓", f.getGlyph('a').toStringRepresentation());
    }

    @Test
    void renderEstaticoSinEstado() {
        String s1 = ASCIIText.render("ab", tinyFont());
        String s2 = ASCIIText.render("ab", tinyFont());
        assertEquals(s1, s2);
    }
}
