package io.github.blacknoize404.asciiutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interpreta textos y genera texto ASCII, dado un texto y una fuente.
 *
 * @author Barnés Inside
 * @version 0.2
 * <p><b>Ejemplo:</b></p>
 * <pre>{@code
 * ASCIIText t = new ASCIIText("Hola mundo", ASCIIFonts.MINIMAL);
 * String output = t.toStringRepresentation(); // representación con la fuente dada
 * }</pre>
 */
public class ASCIIText {

    /**
     * Los tipos de fondos disponibles en opacidad ASCII.
     * <table>
     *     <caption>Fondos disponibles</caption>
     *     <tr><td>BLANK</td><td>Espacios en blanco</td></tr>
     *     <tr><td>PERCENT25</td><td>░</td></tr>
     *     <tr><td>PERCENT50</td><td>▒</td></tr>
     *     <tr><td>PERCENT75</td><td>▓</td></tr>
     *     <tr><td>PERCENT100</td><td>█</td></tr>
     * </table>
     */
    public enum BackgroundType {
        BLANK,
        PERCENT25,
        PERCENT50,
        PERCENT75,
        PERCENT100,
    }

    /**
     * Alineación horizontal de cada línea dentro del ancho del texto.
     */
    public enum Alignment {
        LEFT,
        CENTER,
        RIGHT,
    }

    /**
     * Modo de composición de los glifos (los layouts de FIGlet).
     * <table>
     *     <caption>Modos de smushing</caption>
     *     <tr><td>FULL_WIDTH</td><td>Sin solape: glifos enteros más kerning</td></tr>
     *     <tr><td>KERNING</td><td>Solapa mientras choquen espacios</td></tr>
     *     <tr><td>UNIVERSAL</td><td>Cualquier choque: gana el carácter de la derecha</td></tr>
     *     <tr><td>CONTROLLED</td><td>Aplica las reglas FIGlet de {@link #setSmushRules(int)}</td></tr>
     * </table>
     */
    public enum SmushMode {
        FULL_WIDTH,
        KERNING,
        UNIVERSAL,
        CONTROLLED,
    }

    /**
     * Rotación aplicada a la representación final.
     * <table>
     *     <caption>Rotaciones disponibles</caption>
     *     <tr><td>NONE</td><td>Sin rotación</td></tr>
     *     <tr><td>CLOCKWISE</td><td>90° en el sentido del reloj</td></tr>
     *     <tr><td>COUNTERCLOCKWISE</td><td>90° contra el reloj</td></tr>
     *     <tr><td>UPSIDE_DOWN</td><td>180°</td></tr>
     * </table>
     */
    public enum Rotation {
        NONE,
        CLOCKWISE,
        COUNTERCLOCKWISE,
        UPSIDE_DOWN,
    }

    /**
     * La cadena a procesar en formato ASCII.
     */
    private String text;

    /**
     * La fuente del formato ASCII.
     */
    private ASCIIFont font;

    /**
     * La matriz de caracteres que representa al texto ({@code '\0'} = hueco
     * que se rellena con el fondo al representar). Se regenera cuando algo
     * que la afecta cambia ({@link #isModified}).
     */
    private char[][] matrix;

    /**
     * Define el nivel de espaciado entre las letras.
     */
    private int kerning = 1;

    /**
     * Define el espacio entre las líneas del salto de línea.
     */
    private int lineHeight = 1;

    /**
     * El ancho que ocupa el texto (en columnas de la matriz).
     */
    private int width = 0;

    /**
     * La altura que ocupa el texto (en filas de la matriz).
     */
    private int height = 0;

    /**
     * Bandera de "sucio": hay que regenerar matriz y representación la próxima
     * vez que se pidan.
     */
    private boolean isModified = true;

    /**
     * Representación en caché del texto, válida hasta que cambie algo que la afecte.
     */
    private String representation;

    /**
     * El tipo de fondo con que rellenar los huecos.
     */
    private BackgroundType background = BackgroundType.BLANK;

    /**
     * Alineación horizontal de cada línea.
     */
    private Alignment alignment = Alignment.LEFT;

    /**
     * Ancho máximo del texto en columnas ({@code 0} = sin límite): si el texto
     * lo excede, se divide por palabras (word-wrap).
     */
    private int maxWidth = 0;

    /**
     * Tinta con la que dibujar el trazo de los glifos ({@code 0} = usar el
     * carácter del propio glifo). Sustituye al {@code '▓'} de las fuentes.
     */
    private char ink = 0;

    /**
     * Si hay que dibujar un marco alrededor del texto.
     */
    private boolean framed = false;

    /**
     * Si hay que espejar el texto horizontalmente.
     */
    private boolean flipHorizontal = false;

    /**
     * Si hay que espejar el texto verticalmente.
     */
    private boolean flipVertical = false;

    /**
     * Modo de smushing para componer los glifos.
     */
    private SmushMode smushMode = SmushMode.FULL_WIDTH;

    /**
     * Reglas de smushing para {@link SmushMode#CONTROLLED}
     * ({@code -1} = usar las que declare la fuente, o todas).
     */
    private int smushRules = -1;

    /**
     * Prefijo ANSI de color aplicado a cada línea de salida ({@code null} = sin).
     */
    private String ansiColor = null;

    /**
     * Gradiente RGB por columnas para la tinta: {@code {r1,g1,b1,r2,g2,b2}}
     * ({@code null} = sin).
     */
    private int[] gradient = null;

    /**
     * Carácter de fondo personalizado ({@code null} = usar {@link #background}).
     */
    private Character customBackground = null;

    /**
     * Rotación aplicada a la representación ({@link Rotation#NONE} = ninguna).
     */
    private Rotation rotation = Rotation.NONE;

    /**
     * Construye una nueva instancia con una fuente ASCII dada.
     *
     * @param font dicha fuente
     */
    public ASCIIText(ASCIIFont font) {
        this.font = font;
    }

    /**
     * Construye una nueva instancia con un texto y una fuente ASCII dados.
     *
     * @param text dicho texto
     * @param font dicha fuente
     */
    public ASCIIText(String text, ASCIIFont font) {
        this.font = font;
        this.setText(text);
    }

    // ------------------------------------------------------------------
    // Generación de la matriz
    // ------------------------------------------------------------------

    /**
     * El texto efectivo a renderizar: el original si no hay {@link #getMaxWidth()},
     * o el envuelto por palabras si lo hay.
     *
     * @return dicho texto
     */
    private String effectiveText() {
        if (text == null || text.isEmpty() || maxWidth <= 0) return text;
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\n", -1)) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(wrapLine(line));
        }
        return sb.toString();
    }

    /**
     * El ancho de una palabra según la fuente y el kerning.
     *
     * @param word dicha palabra
     * @return su ancho en columnas
     */
    private int wordWidth(String word) {
        int w = 0;
        for (int i = 0; i < word.length(); i++) {
            ASCIIGlyph g = font.getGlyph(word.charAt(i));
            if (g != null) w += g.width() + (i > 0 ? kerning : 0);
        }
        return w;
    }

    /**
     * El ancho del glifo espacio más el kerning que lo rodea.
     *
     * @return dicho ancho
     */
    private int spaceWidth() {
        ASCIIGlyph space = font.getGlyph(' ');
        return (space != null ? space.width() : 1) + 2 * kerning;
    }

    /**
     * Divide una línea en varias para que ninguna supere {@link #getMaxWidth()}
     * columnas, preferiblemente por palabras; una palabra que no cabe entera se
     * corta a lo bruto.
     *
     * @param line la línea original
     * @return la línea envuelta con saltos de línea
     */
    private String wrapLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int curW = 0;
        for (String word : line.split(" ", -1)) {
            int ww = wordWidth(word);
            int sep = cur.length() == 0 ? 0 : spaceWidth();
            if (cur.length() > 0 && curW + sep + ww > maxWidth) {
                out.add(cur.toString());
                cur.setLength(0);
                curW = 0;
                sep = 0;
            }
            // Palabra que ni sola cabe: corte duro por caracteres
            while (ww > maxWidth && word.length() > 1) {
                StringBuilder piece = new StringBuilder();
                int pw = cur.length() == 0 ? 0 : curW + sep;
                int i = 0;
                ASCIIGlyph g;
                while (i < word.length() && (g = font.getGlyph(word.charAt(i))) != null
                        && pw + g.width() + (piece.length() == 0 ? 0 : kerning) <= maxWidth) {
                    pw += g.width() + (piece.length() == 0 ? 0 : kerning);
                    piece.append(word.charAt(i));
                    i++;
                }
                if (i == 0) break; // ni un carácter cabe: evita bucle infinito
                cur.append(cur.length() == 0 ? "" : " ").append(piece);
                out.add(cur.toString());
                cur.setLength(0);
                curW = 0;
                sep = 0;
                word = word.substring(i);
                ww = wordWidth(word);
            }
            if (cur.length() > 0) { cur.append(' '); curW += sep; }
            cur.append(word);
            curW += ww;
        }
        out.add(cur.toString());
        return String.join("\n", out);
    }

    /**
     * El ancho de cada línea del texto efectivo.
     *
     * @param body el texto efectivo
     * @return array con el ancho de cada línea
     */
    private int[] lineWidths(String body) {
        String[] lines = body.split("\n", -1);
        int[] widths = new int[lines.length];
        for (int i = 0; i < lines.length; i++) {
            widths[i] = wordWidth(lines[i]);
        }
        return widths;
    }

    /**
     * Convierte el texto en una matriz de caracteres correspondiente a la fuente.
     */
    private void fillMatrix() {

        String body = effectiveText();
        if (body == null || body.isEmpty()) {
            this.matrix = new char[0][0];
            return;
        }

        String[] lines = body.split("\n", -1);
        int band = font.getHeight();

        if (smushMode != SmushMode.FULL_WIDTH) {
            fillSmushed(lines, band);
            return;
        }

        int[] widths = lineWidths(body);
        this.matrix = new char[this.height][this.width];

        for (int i = 0; i < lines.length; i++) {

            int row0 = i * (band + lineHeight);
            int carriage = switch (alignment) {
                case LEFT -> 0;
                case CENTER -> Math.max(0, (this.width - widths[i]) / 2);
                case RIGHT -> Math.max(0, this.width - widths[i]);
            };

            String line = font.isOnlyLowerCases() ? lines[i].toLowerCase() : lines[i];
            for (char character : line.toCharArray()) {
                ASCIIGlyph glyph = font.getGlyph(character);
                if (glyph != null) {
                    carriage += write(glyph, row0, carriage, band);
                }
            }
        }
    }

    /**
     * Compone el texto fusionando glifos según el {@link #smushMode}: el solape
     * de cada glifo es el mínimo que permiten todas sus filas (como FIGlet).
     * El ancho real solo se conoce al componer, así que se asigna aquí.
     *
     * @param lines las líneas del texto efectivo
     * @param band  la altura de banda de la fuente
     */
    private void fillSmushed(String[] lines, int band) {
        int rules = smushRules >= 0 ? smushRules
                : font.getSmushRules() > 0 ? font.getSmushRules() : Smusher.RULE_ALL;
        Smusher.Mode mode = switch (smushMode) {
            case KERNING -> Smusher.Mode.KERNING;
            case UNIVERSAL -> Smusher.Mode.UNIVERSAL;
            case CONTROLLED -> Smusher.Mode.CONTROLLED;
            default -> Smusher.Mode.KERNING;
        };
        char hb = font.getHardBlank();

        // Primera pasada: compone cada línea y mide el ancho real
        List<char[][]> rendered = new ArrayList<>();
        List<int[]> lengths = new ArrayList<>();
        int maxW = 0;
        for (String line : lines) {
            String src = font.isOnlyLowerCases() ? line.toLowerCase() : line;
            int bound = wordWidth(line) + 1;
            char[][] rows = new char[band][Math.max(1, bound)];
            int[] lens = new int[band];
            for (char c : src.toCharArray()) {
                ASCIIGlyph g = font.getGlyph(c);
                if (g == null) continue;
                char[][] data = g.toArray();
                int overlap = Integer.MAX_VALUE;
                for (int r = 0; r < Math.min(data.length, band); r++) {
                    if (data[r].length > 0) {
                        overlap = Math.min(overlap, Smusher.smushAmount(rows[r], lens[r], inked(data[r]), mode, rules, hb));
                    }
                }
                if (overlap == Integer.MAX_VALUE) overlap = 0;
                for (int r = 0; r < Math.min(data.length, band); r++) {
                    if (data[r].length > 0) {
                        lens[r] = Smusher.merge(rows[r], lens[r], inked(data[r]), overlap, mode, rules, hb);
                    }
                }
            }
            rendered.add(rows);
            lengths.add(lens);
            for (int l : lens) maxW = Math.max(maxW, l);
        }
        this.width = maxW;

        // Segunda pasada: vuelca en la matriz con la alineación
        this.matrix = new char[this.height][Math.max(1, maxW)];
        for (int i = 0; i < rendered.size(); i++) {
            char[][] rows = rendered.get(i);
            int[] lens = lengths.get(i);
            int lineW = 0;
            for (int l : lens) lineW = Math.max(lineW, l);
            int off = switch (alignment) {
                case LEFT -> 0;
                case CENTER -> Math.max(0, (maxW - lineW) / 2);
                case RIGHT -> Math.max(0, maxW - lineW);
            };
            int row0 = i * (band + lineHeight);
            for (int r = 0; r < band && row0 + r < matrix.length; r++) {
                for (int c = 0; c < lens[r] && off + c < maxW; c++) {
                    matrix[row0 + r][off + c] = rows[r][c];
                }
            }
        }
    }

    /**
     * Devuelve la fila del glifo con la tinta aplicada si {@link #ink} está
     * activa (sustituye al {@code '▓'}).
     *
     * @param row la fila original
     * @return la fila con tinta
     */
    private char[] inked(char[] row) {
        if (ink == 0) return row;
        char[] out = row.clone();
        for (int i = 0; i < out.length; i++) if (out[i] == '▓') out[i] = ink;
        return out;
    }

    /**
     * Escribe un glifo en la matriz en la posición dada, recortándolo a la banda
     * de la fuente ({@link ASCIIFont#getHeight()}) y a los bordes de la matriz.
     *
     * @param glyph  dicho glifo
     * @param row0   fila de inicio
     * @param col0   columna de inicio
     * @param band   altura de banda de la fuente (recorta glifos más altos)
     * @return las columnas que hay que avanzar para el siguiente carácter
     */
    private int write(ASCIIGlyph glyph, int row0, int col0, int band) {
        char[][] characters = glyph.toArray();
        int rows = Math.min(characters.length, band);
        for (int r = 0; r < rows && r + row0 < matrix.length; r++) {
            for (int c = 0; c < characters[r].length && c + col0 < matrix[r].length; c++) {
                if (characters[r][c] == ' ') continue;
                matrix[row0 + r][col0 + c] = (ink != 0 && characters[r][c] == '▓') ? ink : characters[r][c];
            }
        }
        return glyph.width() + kerning;
    }

    /**
     * Recalcula {@link #width} y {@link #height} a partir del texto efectivo.
     */
    private void updateTextSize() {
        String body = effectiveText();
        if (body == null || body.isEmpty()) {
            this.width = 0;
            this.height = 0;
            return;
        }
        int lines = body.split("\n", -1).length;
        if (smushMode == SmushMode.FULL_WIDTH) {
            int maxW = 0;
            for (int w : lineWidths(body)) maxW = Math.max(maxW, w);
            this.width = maxW;
        }
        this.height = font.getHeight() * lines + (lines - 1) * lineHeight;
    }

    /**
     * Regenera la matriz si hay cambios pendientes.
     */
    private void ensureMatrix() {
        if (isModified) {
            fillMatrix();
        }
    }

    // ------------------------------------------------------------------
    // Propiedades
    // ------------------------------------------------------------------

    /**
     * Obtiene el ancho que ocupa el texto (en columnas).
     *
     * @return dicho valor
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Obtiene la altura que ocupa el texto (en filas).
     *
     * @return dicho valor
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Obtiene el valor de la separación entre las letras.
     *
     * @return dicho valor
     */
    public int getKerning() {
        return kerning;
    }

    /**
     * Define el valor de separación entre las letras.
     *
     * @param kerning dicho valor
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setKerning(int kerning) {
        if (this.kerning == kerning) return this;
        this.kerning = kerning;
        this.isModified = true;
        updateTextSize();
        return this;
    }

    /**
     * Obtiene el tamaño de separación de línea.
     *
     * @return dicho tamaño
     */
    public int getLineHeight() {
        return this.lineHeight;
    }

    /**
     * Asigna el tamaño de la separación de línea.
     *
     * @param lineHeight dicho tamaño
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setLineHeight(int lineHeight) {
        if (this.lineHeight == lineHeight) return this;
        this.lineHeight = lineHeight;
        this.isModified = true;
        updateTextSize();
        return this;
    }

    /**
     * Obtiene el texto a convertir en ASCII.
     *
     * @return dicho texto
     */
    public String getText() {
        return text;
    }

    /**
     * Actualiza el texto a representar con la fuente.
     *
     * @param text dicho texto
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setText(String text) {
        if (getText() != null && getText().equals(text)) return this;
        this.text = text;
        this.isModified = true;
        updateTextSize();
        return this;
    }

    /**
     * Asigna un texto y devuelve directamente su representación en ASCII:
     * atajo a {@link #setText(String)} + {@link #toStringRepresentation()}.
     *
     * @param text dicho texto
     * @return la representación ASCII del texto
     * @deprecated usar {@link #render(String)} o {@link #render(String, ASCIIFont)}
     */
    @Deprecated
    public String getOf(String text) {
        return render(text);
    }

    /**
     * Asigna un texto y devuelve directamente su representación en ASCII:
     * atajo a {@link #setText(String)} + {@link #toStringRepresentation()}.
     *
     * @param text dicho texto
     * @return la representación ASCII del texto
     */
    public String render(String text) {
        this.setText(text);
        return toStringRepresentation();
    }

    /**
     * Renderiza un texto con una fuente, sin mantener estado: atajo puro a
     * {@code new ASCIIText(text, font).toStringRepresentation()}.
     *
     * @param text dicho texto
     * @param font dicha fuente
     * @return la representación ASCII del texto
     */
    public static String render(String text, ASCIIFont font) {
        return new ASCIIText(text, font).toStringRepresentation();
    }

    /**
     * Obtiene la fuente que está siendo usada.
     *
     * @return dicha fuente
     */
    public ASCIIFont getFont() {
        return font;
    }

    /**
     * Asigna una nueva fuente a usar.
     *
     * @param newFont dicha fuente
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setFont(ASCIIFont newFont) {
        this.font = newFont;
        this.isModified = true;
        updateTextSize();
        return this;
    }

    /**
     * Obtiene el tipo de fondo seleccionado.
     *
     * @return dicho fondo
     */
    public BackgroundType getBackgroundType() {
        return background;
    }

    /**
     * Obtiene el carácter del fondo seleccionado.
     *
     * @return dicho carácter
     */
    public Character getBackground() {
        if (customBackground != null) return customBackground;
        return switch (background) {
            case BLANK -> ' ';
            case PERCENT25 -> '░';
            case PERCENT50 -> '▒';
            case PERCENT75 -> '▓';
            case PERCENT100 -> '█';
        };
    }

    /**
     * Asigna el fondo del texto.
     *
     * @param background dicho fondo
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setBackground(BackgroundType background) {
        if (this.background == background && this.customBackground == null) return this;
        this.background = background;
        this.customBackground = null;
        this.isModified = true;
        return this;
    }

    /**
     * Asigna un carácter de fondo personalizado con el que rellenar los huecos.
     *
     * @param background dicho carácter
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setBackground(char background) {
        this.customBackground = background;
        this.isModified = true;
        return this;
    }

    /**
     * Obtiene la alineación horizontal.
     *
     * @return dicha alineación
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * Asigna la alineación horizontal de cada línea dentro del ancho del texto.
     *
     * @param alignment dicha alineación
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setAlignment(Alignment alignment) {
        if (this.alignment == alignment) return this;
        this.alignment = alignment;
        this.isModified = true;
        return this;
    }

    /**
     * Obtiene el ancho máximo del texto en columnas ({@code 0} = sin límite).
     *
     * @return dicho ancho
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * Asigna un ancho máximo en columnas: el texto que lo exceda se envuelve
     * por palabras (word-wrap). {@code 0} desactiva el límite.
     *
     * @param maxWidth dicho ancho
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setMaxWidth(int maxWidth) {
        if (this.maxWidth == maxWidth) return this;
        this.maxWidth = Math.max(0, maxWidth);
        this.isModified = true;
        updateTextSize();
        return this;
    }

    /**
     * Obtiene el carácter de tinta ({@code 0} = el del propio glifo).
     *
     * @return dicho carácter
     */
    public char getInk() {
        return ink;
    }

    /**
     * Asigna el carácter de tinta: sustituye al {@code '▓'} del glifo al
     * renderizar. {@code 0} devuelve el comportamiento original.
     *
     * @param ink dicho carácter
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setInk(char ink) {
        if (this.ink == ink) return this;
        this.ink = ink;
        this.isModified = true;
        return this;
    }

    /**
     * Indica si el texto se dibuja con marco.
     *
     * @return dicho valor
     */
    public boolean isFramed() {
        return framed;
    }

    /**
     * Activa o desactiva el marco alrededor del texto (bordes {@code ┌─┐│└─┘}).
     *
     * @param framed dicho valor
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setFramed(boolean framed) {
        if (this.framed == framed) return this;
        this.framed = framed;
        this.isModified = true;
        return this;
    }

    /**
     * Indica si el texto se espeja horizontalmente.
     *
     * @return dicho valor
     */
    public boolean isFlipHorizontal() {
        return flipHorizontal;
    }

    /**
     * Activa o desactiva el espejado horizontal del texto.
     *
     * @param flipHorizontal dicho valor
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setFlipHorizontal(boolean flipHorizontal) {
        if (this.flipHorizontal == flipHorizontal) return this;
        this.flipHorizontal = flipHorizontal;
        this.isModified = true;
        return this;
    }

    /**
     * Indica si el texto se espeja verticalmente.
     *
     * @return dicho valor
     */
    public boolean isFlipVertical() {
        return flipVertical;
    }

    /**
     * Activa o desactiva el espejado vertical del texto.
     *
     * @param flipVertical dicho valor
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setFlipVertical(boolean flipVertical) {
        if (this.flipVertical == flipVertical) return this;
        this.flipVertical = flipVertical;
        this.isModified = true;
        return this;
    }

    /**
     * Obtiene el modo de smushing actual.
     *
     * @return dicho modo
     */
    public SmushMode getSmushMode() {
        return smushMode;
    }

    /**
     * Asigna el modo de smushing: cómo se solapan los glifos al componer.
     * En cualquier modo distinto de {@link SmushMode#FULL_WIDTH} se ignora
     * el {@link #setKerning(int)} y el ancho se conoce solo tras renderizar.
     *
     * @param smushMode dicho modo
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setSmushMode(SmushMode smushMode) {
        if (this.smushMode == smushMode) return this;
        this.smushMode = smushMode;
        this.isModified = true;
        updateTextSize();
        return this;
    }

    /**
     * Obtiene la máscara de reglas de smushing ({@code -1} = auto).
     *
     * @return dicha máscara
     */
    public int getSmushRules() {
        return smushRules;
    }

    /**
     * Asigna la máscara de reglas para {@link SmushMode#CONTROLLED}
     * (bits estilo FIGlet: 1=iguales, 2=subrayado, 4=jerarquía, 8=parejas,
     * 16=X grande, 32=hardblank). {@code -1} usa las de la fuente o todas.
     *
     * @param smushRules dicha máscara
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setSmushRules(int smushRules) {
        if (this.smushRules == smushRules) return this;
        this.smushRules = smushRules;
        this.isModified = true;
        return this;
    }

    /**
     * El prefijo ANSI de color ({@code null} = sin color).
     *
     * @return dicho prefijo
     */
    public String getAnsiColor() {
        return ansiColor;
    }

    /**
     * Colorea la salida con un código ANSI (p.ej. {@code Ansi.RED} o
     * {@code Ansi.rgb(255,128,0)}). Se aplica por línea con reset al final.
     *
     * @param ansiCode el prefijo ANSI, o {@code null} para quitarlo
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setColor(String ansiCode) {
        this.ansiColor = ansiCode;
        this.isModified = true;
        return this;
    }

    /**
     * Define un gradiente horizontal de color para la tinta (truecolor ANSI),
     * interpolado por columnas entre los dos RGB dados.
     *
     * @param r1 rojo inicial   {@code 0-255}
     * @param g1 verde inicial  {@code 0-255}
     * @param b1 azul inicial   {@code 0-255}
     * @param r2 rojo final     {@code 0-255}
     * @param g2 verde final    {@code 0-255}
     * @param b2 azul final     {@code 0-255}
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setGradient(int r1, int g1, int b1, int r2, int g2, int b2) {
        this.gradient = new int[]{r1, g1, b1, r2, g2, b2};
        this.isModified = true;
        return this;
    }

    /**
     * Quita el gradiente de color (el {@link #setColor(String)} sigue aplicando).
     *
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText clearGradient() {
        if (this.gradient == null) return this;
        this.gradient = null;
        this.isModified = true;
        return this;
    }

    /**
     * Indica si el texto se rota.
     *
     * @return dicho valor
     * @deprecated usar {@link #getRotation()}
     */
    @Deprecated
    public boolean isRotated() {
        return rotation != Rotation.NONE;
    }

    /**
     * Activa o desactiva la rotación de 90° en sentido contrario al reloj.
     *
     * @param rotated dicho valor
     * @return esta misma instancia (encadenable)
     * @deprecated usar {@link #setRotation(Rotation)}
     */
    @Deprecated
    public ASCIIText setRotated(boolean rotated) {
        return setRotation(rotated ? Rotation.COUNTERCLOCKWISE : Rotation.NONE);
    }

    /**
     * Obtiene la rotación aplicada a la representación.
     *
     * @return dicha rotación
     */
    public Rotation getRotation() {
        return rotation;
    }

    /**
     * Asigna la rotación de la representación: se aplica tras componer la
     * matriz, antes del volteo y del marco.
     *
     * @param rotation dicha rotación
     * @return esta misma instancia (encadenable)
     */
    public ASCIIText setRotation(Rotation rotation) {
        if (this.rotation == rotation) return this;
        this.rotation = rotation;
        this.isModified = true;
        return this;
    }

    // ------------------------------------------------------------------
    // Salida
    // ------------------------------------------------------------------

    /**
     * Devuelve la matriz de caracteres generada (una copia; {@code '\0'} = hueco).
     *
     * @return dicha matriz
     */
    public char[][] toCharMatrix() {
        ensureMatrix();
        char[][] copy = new char[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = matrix[i].clone();
        }
        return copy;
    }

    /**
     * Devuelve una cadena que representa al texto en ASCII.
     *
     * @return dicha cadena
     */
    public String toStringRepresentation() {

        if (!isModified) return representation;

        fillMatrix();

        // Transformaciones sobre una copia (la matriz queda intacta)
        char[][] m = new char[matrix.length][];
        for (int i = 0; i < matrix.length; i++) m[i] = matrix[i].clone();
        if (rotation != Rotation.NONE && m.length > 0 && m[0].length > 0) {
            char[][] t = switch (rotation) {
                case CLOCKWISE -> {
                    char[][] r90 = new char[m[0].length][m.length];
                    for (int r = 0; r < m.length; r++)
                        for (int c = 0; c < m[r].length; c++) r90[c][m.length - 1 - r] = m[r][c];
                    yield r90;
                }
                case COUNTERCLOCKWISE -> {
                    char[][] r90 = new char[m[0].length][m.length];
                    for (int r = 0; r < m.length; r++)
                        for (int c = 0; c < m[r].length; c++) r90[m[0].length - 1 - c][r] = m[r][c];
                    yield r90;
                }
                case UPSIDE_DOWN -> {
                    char[][] r180 = new char[m.length][m[0].length];
                    for (int r = 0; r < m.length; r++)
                        for (int c = 0; c < m[r].length; c++) r180[m.length - 1 - r][m[0].length - 1 - c] = m[r][c];
                    yield r180;
                }
                default -> m;
            };
            m = t;
        }
        if (flipHorizontal) {
            for (char[] row : m) {
                for (int i = 0, j = row.length - 1; i < j; i++, j--) {
                    char tmp = row[i]; row[i] = row[j]; row[j] = tmp;
                }
            }
        }
        if (flipVertical) {
            for (int i = 0, j = m.length - 1; i < j; i++, j--) {
                char[] tmp = m[i]; m[i] = m[j]; m[j] = tmp;
            }
        }

        StringBuilder sb = new StringBuilder();
        char bg = getBackground();
        char hb = font.getHardBlank();
        for (int r = 0; r < m.length; r++) {
            if (ansiColor != null) sb.append(ansiColor);
            for (int c = 0; c < m[r].length; c++) {
                char ch = m[r][c];
                if (gradient != null && ch != '\0' && ch != ' ' && ch != hb && m[r].length > 1) {
                    double t = (double) c / (m[r].length - 1);
                    sb.append(Ansi.rgb(
                            gradient[0] + (int) ((gradient[3] - gradient[0]) * t),
                            gradient[1] + (int) ((gradient[4] - gradient[1]) * t),
                            gradient[2] + (int) ((gradient[5] - gradient[2]) * t)));
                }
                sb.append(ch == '\0' ? bg : ch == hb ? ' ' : ch);
            }
            if (ansiColor != null || gradient != null) sb.append(Ansi.RESET);
            if (r < m.length - 1) sb.append('\n');
        }

        String out = sb.toString();
        if (framed && m.length > 0 && m[0].length > 0) {
            int fw = m[0].length;
            String top = "┌" + "─".repeat(fw) + "┐";
            String bottom = "└" + "─".repeat(fw) + "┘";
            StringBuilder fb = new StringBuilder(top).append('\n');
            for (String line : out.split("\n", -1)) {
                fb.append('│').append(line).append('│').append('\n');
            }
            out = fb.append(bottom).toString();
        }

        this.representation = out;
        this.isModified = false;
        return this.representation;
    }

    /**
     * Las líneas de la representación como lista inmutable, sin códigos ANSI
     * (los escapa si hay color o gradiente activos).
     *
     * @return dicha lista
     */
    public List<String> lines() {
        ensureMatrix();
        List<String> out = new ArrayList<>();
        for (String l : toStringRepresentation().split("\n", -1)) {
            out.add(l.replaceAll("\\[[0-9;]*m", ""));
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * Escribe la representación en un writer.
     *
     * @param writer el destino
     * @throws java.io.IOException si falla la escritura
     */
    public void write(java.io.Writer writer) throws java.io.IOException {
        writer.write(toStringRepresentation());
        writer.write('\n');
    }

    /**
     * Guarda la representación en un fichero de texto (UTF-8).
     *
     * @param path el fichero destino
     * @throws java.io.IOException si falla la escritura
     */
    public void save(java.nio.file.Path path) throws java.io.IOException {
        java.nio.file.Files.writeString(path, toStringRepresentation() + "\n", java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Imprime la representación del texto por salida estándar.
     */
    public void print() {
        System.out.println(toStringRepresentation());
    }
}
