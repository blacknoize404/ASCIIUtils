package ASCIIUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa una fuente en formato ASCII.
 * <p>
 * Esta clase permitirá almacenar una fuente ASCII personalizada para que
 * resulte más cómodo pedir la representación del carácter requerido en la fuente
 * en cuestión.
 * </p>
 * @version 0.1
 * @author Barnés Inside
 */
public class ASCIIFont {

    /**
     * Los glifos asignados a cada carácter.
     */
    private final Map<Character, ASCIIGlyph> glyphs;

    /**
     * Máxima altura de los glifos en esta fuente
     */
    private int height;

    private final boolean onlyLowerCases = true;

    public ASCIIFont() {
        glyphs = new HashMap<>();
        height = 0;
    }

    /**
     * Constructor privado que crea una nueva fuente dado un diccionario
     *
     * @param glyphs los glifos registrados a ese diccionario
     */
    private ASCIIFont(Map<Character, ASCIIGlyph> glyphs) {
        this.glyphs = glyphs;
        this.height = getMaxHeightInGlyphs(glyphs);
    }

    /**
     * Obtiene la altura máxima de todos los glifos en el diccionario.
     * @param glyphs los glifos a buscar la máxima altura
     * @return dicha altura máxima
     */
    private int getMaxHeightInGlyphs(Map<Character, ASCIIGlyph> glyphs) {

        int size = 0;

        // Por cada glifo en el mapa, maximiza el tamaño del glifo
        for (ASCIIGlyph glyph : glyphs.values()) size = Math.max(size, glyph.height());

        return size;

    }

    /**
     * Obtiene el glifo ASCII que representa al carácter del argumento.
     *
     * @param c dicho carácter
     * @return el glifo requerido o null si no está registrado el carácter en la fuente
     */
    public ASCIIGlyph getGlyph(Character c) {

        if (onlyLowerCases) c = Character.toLowerCase(c);

        return this.glyphs.getOrDefault(c, this.glyphs.get('\0'));
    }

    /**
     * Registra un carácter con un glifo.
     *
     * @param c     dicho carácter
     * @param glyph el glifo en cuestión
     */
    public void put(Character c, ASCIIGlyph glyph) {
        this.glyphs.put(c, glyph);

        // Determina si la altura del nuevo carácter es mayor que la máxima
        this.height = Math.max(this.height, glyph.height());
    }

    /**
     * Obtiene la máxima altura de los glifos en esta fuente.
     * @return dicho valor
     */
    public int getHeight() {
        return height;
    }

    /**
     * Construye una nueva fuente dado un diccionario de glifos registrados
     *
     * @param glyphs dicho diccionario
     * @return una nueva instancia de una fuente ASCII
     */
    public ASCIIFont from(Map<Character, ASCIIGlyph> glyphs) {
        return new ASCIIFont(glyphs);
    }

}
