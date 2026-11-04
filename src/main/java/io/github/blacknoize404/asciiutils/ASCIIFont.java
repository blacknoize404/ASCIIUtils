package io.github.blacknoize404.asciiutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representa una fuente en formato ASCII.
 * <p>
 * Esta clase permite almacenar una fuente ASCII personalizada para que
 * resulte más cómodo pedir la representación del carácter requerido en la fuente
 * en cuestión.
 * </p>
 *
 * @version 0.2
 * @author Barnés Inside
 */
public class ASCIIFont {

    /**
     * Los glifos asignados a cada carácter.
     */
    private final Map<Character, ASCIIGlyph> glyphs;

    /**
     * Máxima altura de los glifos en esta fuente.
     */
    private int height;

    /**
     * Si solo se usan minúsculas: las mayúsculas se normalizan al registrar y al buscar.
     */
    private boolean onlyLowerCases = true;

    /**
     * Construye una fuente vacía.
     */
    public ASCIIFont() {
        glyphs = new LinkedHashMap<>();
        height = 0;
    }

    /**
     * Constructor privado que crea una nueva fuente dado un diccionario.
     *
     * @param glyphs los glifos registrados a ese diccionario
     */
    private ASCIIFont(Map<Character, ASCIIGlyph> glyphs) {
        this.glyphs = new LinkedHashMap<>(glyphs);
        this.height = getMaxHeightInGlyphs(this.glyphs);
    }

    /**
     * Obtiene la altura máxima de todos los glifos en el diccionario.
     *
     * @param glyphs los glifos a buscar la máxima altura
     * @return dicha altura máxima
     */
    private int getMaxHeightInGlyphs(Map<Character, ASCIIGlyph> glyphs) {
        int size = 0;
        for (ASCIIGlyph glyph : glyphs.values()) {
            size = Math.max(size, glyph.height());
        }
        return size;
    }

    /**
     * Normaliza el carácter según {@link #isOnlyLowerCases()}.
     *
     * @param c el carácter original
     * @return el carácter a usar como clave
     */
    private char normalize(char c) {
        return onlyLowerCases ? Character.toLowerCase(c) : c;
    }

    /**
     * Obtiene el glifo ASCII que representa al carácter del argumento.
     * <p>
     * Las mayúsculas se normalizan a minúsculas (si {@link #isOnlyLowerCases()}).
     * Si el carácter no está registrado se devuelve el glifo de respaldo asignado
     * a {@code '\0'} (o {@code null} si la fuente tampoco lo define).
     * </p>
     *
     * @param c dicho carácter
     * @return el glifo requerido, o el de respaldo si el carácter no está registrado
     */
    public ASCIIGlyph getGlyph(Character c) {
        return this.glyphs.getOrDefault(normalize(c), this.glyphs.get('\0'));
    }

    /**
     * Registra un carácter con un glifo.
     *
     * @param c     dicho carácter
     * @param glyph el glifo en cuestión
     */
    public void put(Character c, ASCIIGlyph glyph) {
        this.glyphs.put(normalize(c), glyph);
        this.height = Math.max(this.height, glyph.height());
    }

    /**
     * Indica si el carácter tiene un glifo propio registrado (no cuenta el respaldo {@code '\0'}).
     *
     * @param c dicho carácter
     * @return {@code true} si está registrado
     */
    public boolean contains(char c) {
        return this.glyphs.containsKey(normalize(c));
    }

    /**
     * Cantidad de glifos registrados en la fuente.
     *
     * @return dicho valor
     */
    public int size() {
        return glyphs.size();
    }

    /**
     * Vista inmutable del mapa carácter → glifo.
     *
     * @return dicho mapa
     */
    public Map<Character, ASCIIGlyph> glyphs() {
        return Collections.unmodifiableMap(glyphs);
    }

    /**
     * Obtiene la máxima altura de los glifos en esta fuente.
     *
     * @return dicho valor
     */
    public int getHeight() {
        return height;
    }

    /**
     * Indica si la fuente normaliza mayúsculas a minúsculas (por defecto {@code true}).
     *
     * @return dicho valor
     */
    public boolean isOnlyLowerCases() {
        return onlyLowerCases;
    }

    /**
     * Define si la fuente normaliza mayúsculas a minúsculas.
     *
     * @param onlyLowerCases dicho valor
     * @return esta misma fuente (encadenable)
     */
    public ASCIIFont setOnlyLowerCases(boolean onlyLowerCases) {
        this.onlyLowerCases = onlyLowerCases;
        return this;
    }

    /**
     * Construye una nueva fuente dado un diccionario de glifos registrados.
     *
     * @param glyphs dicho diccionario
     * @return una nueva instancia de una fuente ASCII
     */
    public static ASCIIFont from(Map<Character, ASCIIGlyph> glyphs) {
        return new ASCIIFont(glyphs);
    }

    /**
     * Construye una sub-fuente conservando solo los caracteres indicados
     * (más el espacio y el respaldo {@code '\0'}, si existen).
     *
     * @param chars los caracteres a conservar
     * @return la nueva fuente reducida
     */
    public ASCIIFont subset(CharSequence chars) {
        ASCIIFont sub = new ASCIIFont();
        for (int i = 0; i < chars.length(); i++) {
            char c = normalize(chars.charAt(i));
            ASCIIGlyph g = this.glyphs.get(c);
            if (g != null) sub.put(c, g);
        }
        for (char c : new char[]{'\0', ' '}) {
            ASCIIGlyph g = this.glyphs.get(c);
            if (g != null) sub.put(c, g);
        }
        return sub;
    }

    /**
     * Carga una fuente desde un lector de texto.
     * <p>
     * Formato: un glifo por línea, {@code c: cuerpoDelGlifo}, donde el cuerpo usa
     * {@code -} como separador de filas (el mismo formato de {@link ASCIIGlyph#from}).
     * Las líneas en blanco se ignoran y las que empiezan por {@code #} son comentarios.
     * </p>
     * <pre>
     * # comentario
     * a:    -▓▓▓-▓ ▓-▓ ▓-▓▓▓-▓ ▓
     * </pre>
     *
     * @param reader el lector con la definición de la fuente
     * @return la fuente cargada
     * @throws IOException si falla la lectura
     */
    public static ASCIIFont load(Reader reader) throws IOException {
        ASCIIFont font = new ASCIIFont();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;
                int sep = line.indexOf(':', 1);
                if (sep < 0) {
                    throw new IOException("Línea de fuente inválida (falta ':'): " + line);
                }
                font.put(line.charAt(0), ASCIIGlyph.from(line.substring(sep + 1)));
            }
        }
        return font;
    }
}
