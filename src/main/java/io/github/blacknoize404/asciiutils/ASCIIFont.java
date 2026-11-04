package io.github.blacknoize404.asciiutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
     * Carácter del glifo de respaldo cuando falta uno (por defecto {@code '\0'}).
     */
    private char fallback = '\0';

    /**
     * Carácter "hardblank" de la fuente ({@code '\0'} si no tiene): se muestra
     * como espacio pero cuenta como carácter visible al hacer smushing. Las
     * fuentes FIGlet lo definen en su cabecera.
     */
    private char hardBlank = '\0';

    /**
     * Reglas de smushing declaradas por la fuente (máscara de bits estilo
     * FIGlet, {@code -1} = sin declarar).
     */
    private int layoutFlags = -1;

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
        return this.glyphs.getOrDefault(normalize(c), this.glyphs.get(fallback));
    }

    /**
     * El ancho del glifo de un carácter ({@code 0} si no hay glifo).
     *
     * @param c dicho carácter
     * @return dicho ancho en columnas
     */
    public int charWidth(char c) {
        ASCIIGlyph g = getGlyph(c);
        return g != null ? g.width() : 0;
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
     * El carácter cuyo glifo se usa de respaldo ({@code '\0'} por defecto).
     *
     * @return dicho carácter
     */
    public char getFallback() {
        return fallback;
    }

    /**
     * Define qué glifo se devuelve cuando un carácter no está registrado.
     *
     * @param fallback el carácter del glifo de respaldo
     * @return esta misma fuente (encadenable)
     */
    public ASCIIFont setFallback(char fallback) {
        this.fallback = fallback;
        return this;
    }

    /**
     * El carácter hardblank de la fuente ({@code '\0'} si no tiene).
     *
     * @return dicho carácter
     */
    public char getHardBlank() {
        return hardBlank;
    }

    /**
     * Define el carácter hardblank (se muestra como espacio, cuenta como
     * visible al hacer smushing).
     *
     * @param hardBlank dicho carácter
     * @return esta misma fuente (encadenable)
     */
    public ASCIIFont setHardBlank(char hardBlank) {
        this.hardBlank = hardBlank;
        return this;
    }

    /**
     * Valor de layout declarado por la fuente ({@code -1} si no declara).
     * Corresponde al {@code fullLayout} (o al {@code oldLayout} resuelto) de
     * FIGlet. Usa {@link #getSmushMode()} y {@link #getSmushRules()} para la
     * interpretación ya resuelta.
     *
     * @return dicho valor
     */
    public int getLayoutFlags() {
        return layoutFlags;
    }

    /**
     * Asigna el valor de layout de la fuente (semántica FIGlet).
     *
     * @param layoutFlags dicho valor
     * @return esta misma fuente (encadenable)
     */
    public ASCIIFont setLayoutFlags(int layoutFlags) {
        this.layoutFlags = layoutFlags;
        return this;
    }

    /**
     * El modo de smushing que la fuente sugiere, derivado de
     * {@link #getLayoutFlags()} con la semántica de FIGlet.
     *
     * @return el modo sugerido ({@code FULL_WIDTH} si no declara)
     */
    public ASCIIText.SmushMode getSmushMode() {
        if (layoutFlags < 0) return ASCIIText.SmushMode.FULL_WIDTH;
        if (layoutFlags == 0) return ASCIIText.SmushMode.KERNING;
        if ((layoutFlags & 128) != 0) {
            return (layoutFlags & 63) != 0 ? ASCIIText.SmushMode.CONTROLLED : ASCIIText.SmushMode.UNIVERSAL;
        }
        if ((layoutFlags & 64) != 0) return ASCIIText.SmushMode.KERNING;
        return ASCIIText.SmushMode.CONTROLLED; // oldLayout 1..63
    }

    /**
     * La máscara de reglas (bits 0–5) para {@link ASCIIText.SmushMode#CONTROLLED},
     * derivada de {@link #getLayoutFlags()}.
     *
     * @return dicha máscara ({@code 0} si no aplica)
     */
    public int getSmushRules() {
        if (layoutFlags <= 0) return 0;
        return layoutFlags & 63;
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
     * El carácter de respaldo se puede escribir como {@code \0} y las directivas
     * {@code height: n}, {@code fallback: c} y {@code hardblank: c} ajustan la fuente.
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
                String key;
                String value;
                if (line.startsWith("::")) {
                    // El glifo ':' se serializa como ":: cuerpo"
                    key = ":";
                    value = line.substring(2);
                } else {
                    int sep = line.indexOf(':');
                    if (sep < 0) {
                        throw new IOException("Línea de fuente inválida (falta ':'): " + line);
                    }
                    key = line.substring(0, sep);
                    value = line.substring(sep + 1);
                }
                // Un espacio tras ':' es separador del formato; si el cuerpo
                // quedara vacío, ese espacio ES el cuerpo
                if (value.length() > 1 && value.charAt(0) == ' ') value = value.substring(1);
                char c = "\\0".equals(key) ? '\0' : key.charAt(0);
                switch (key) {
                    case "height" -> font.height = Math.max(font.height, Integer.parseInt(value.trim()));
                    case "fallback" -> font.fallback = value.isEmpty() ? '\0' : value.charAt(0);
                    case "hardblank" -> font.hardBlank = value.isEmpty() ? '\0' : value.charAt(0);
                    case "lowercase" -> font.onlyLowerCases = Boolean.parseBoolean(value.trim());
                    default -> font.put(c, ASCIIGlyph.from(value));
                }
            }
        }
        return font;
    }

    /**
     * Guarda la fuente en el formato de {@link #load(Reader)}.
     *
     * @param writer dónde escribir la fuente
     * @throws IOException si falla la escritura
     */
    public void save(Writer writer) throws IOException {
        writer.write("height: " + getHeight() + "\n");
        if (fallback != '\0') writer.write("fallback: " + fallback + "\n");
        if (hardBlank != '\0') writer.write("hardblank: " + hardBlank + "\n");
        writer.write("lowercase: " + onlyLowerCases + "\n");
        for (Map.Entry<Character, ASCIIGlyph> e : glyphs.entrySet()) {
            char c = e.getKey();
            writer.write((c == '\0' ? "\\0" : String.valueOf(c)) + ": ");
            char[][] rows = e.getValue().toArray();
            for (int r = 0; r < rows.length; r++) {
                if (r > 0) writer.write('-');
                writer.write(rows[r]);
            }
            writer.write('\n');
        }
    }

    /**
     * Carga una fuente en formato FIGlet ({@code .flf}).
     * <p>
     * Lee la cabecera {@code flf2a}, los 95 glifos ASCII ({@code 32}–{@code 126}),
     * los caracteres alemanes opcionales y marca {@code hardBlank},
     * {@code onlyLowerCases = false} y las reglas de smushing declaradas
     * ({@link #getLayoutFlags()}). Los caracteres codetagged se ignoran.
     * </p>
     *
     * @param reader el lector con el contenido del fichero {@code .flf}
     * @return la fuente cargada
     * @throws IOException si el formato es inválido o falla la lectura
     */
    public static ASCIIFont loadFlf(Reader reader) throws IOException {
        try (BufferedReader br = new BufferedReader(reader)) {
            String header = br.readLine();
            if (header == null || !header.startsWith("flf2a") || header.length() < 6) {
                throw new IOException("No es una fuente FIGlet válida (falta cabecera flf2a)");
            }
            char hardBlank = header.charAt(5);
            String[] h = header.substring(6).trim().split("\\s+");
            if (h.length < 5) throw new IOException("Cabecera FIGlet incompleta: " + header);
            int flfHeight = Integer.parseInt(h[0]);
            int commentLines = Integer.parseInt(h[4]);
            // Resolución del layout: fullLayout si existe; si no, se deriva de
            // oldLayout (-1 = ancho completo, 0 = kerning, >0 = reglas activas)
            int oldLayout = Integer.parseInt(h[3]);
            int layout = h.length > 6 ? Integer.parseInt(h[6]) : oldLayout;
            for (int i = 0; i < commentLines; i++) br.readLine();

            ASCIIFont font = new ASCIIFont();
            font.setOnlyLowerCases(false);
            font.setHardBlank(hardBlank);
            font.setLayoutFlags(layout);
            font.height = flfHeight;

            int[] codes = new int[95 + 7];
            for (int i = 0; i < 95; i++) codes[i] = 32 + i;
            int[] german = {196, 214, 220, 228, 246, 252, 223};
            System.arraycopy(german, 0, codes, 95, 7);

            for (int code : codes) {
                String[] rows = new String[flfHeight];
                for (int r = 0; r < flfHeight; r++) {
                    String row = br.readLine();
                    if (row == null) {
                        if (code == 32) throw new IOException("Fuente FIGlet truncada");
                        rows = null;
                        break;
                    }
                    // Marca de fin: cada fila acaba en el endmark; la última lo lleva doble
                    int strip = (r == flfHeight - 1 && row.length() >= 2) ? 2 : 1;
                    row = row.substring(0, Math.max(0, row.length() - strip));
                    rows[r] = row;
                }
                if (rows == null) break;
                char[][] m = toMatrix(rows);
                boolean empty = true;
                for (char[] row : m) if (row.length > 0) { empty = false; break; }
                font.put((char) code, new ASCIIGlyph(empty ? new char[][]{{' '}} : m));
            }
            // Respaldo: el espacio si la fuente no define otro
            if (font.getGlyph('\0') == null && font.contains(' ')) font.setFallback(' ');
            return font;
        }
    }

    /**
     * Convierte filas de texto en la matriz de un glifo.
     *
     * @param rows las filas
     * @return la matriz
     */
    private static char[][] toMatrix(String[] rows) {
        char[][] m = new char[rows.length][];
        for (int i = 0; i < rows.length; i++) m[i] = rows[i].toCharArray();
        return m;
    }
}
