package io.github.blacknoize404.asciiutils;

import java.util.ArrayList;
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
                while (i < word.length() && pw + font.getGlyph(word.charAt(i)).width() + (piece.length() == 0 ? 0 : kerning) <= maxWidth) {
                    ASCIIGlyph g = font.getGlyph(word.charAt(i));
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
        int[] widths = lineWidths(body);

        int band = font.getHeight();
        this.matrix = new char[this.height][this.width];

        for (int i = 0; i < lines.length; i++) {

            int row0 = i * (band + lineHeight);
            int carriage = switch (alignment) {
                case LEFT -> 0;
                case CENTER -> Math.max(0, (this.width - widths[i]) / 2);
                case RIGHT -> Math.max(0, this.width - widths[i]);
            };

            for (char character : lines[i].toLowerCase().toCharArray()) {
                ASCIIGlyph glyph = font.getGlyph(character);
                if (glyph != null) {
                    carriage += write(glyph, row0, carriage, band);
                }
            }
        }
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
        int maxW = 0;
        for (int w : lineWidths(body)) maxW = Math.max(maxW, w);
        this.width = maxW;
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
        if (this.background == background) return this;
        this.background = background;
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

        // Espejados sobre la matriz
        char[][] m = matrix;
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
        for (int r = 0; r < m.length; r++) {
            for (char c : m[r]) {
                sb.append(c == '\0' ? bg : c);
            }
            if (r < m.length - 1) sb.append('\n');
        }

        String out = sb.toString();
        if (framed && width > 0) {
            String top = "┌" + "─".repeat(width) + "┐";
            String bottom = "└" + "─".repeat(width) + "┘";
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
     * Imprime la representación del texto por salida estándar.
     */
    public void print() {
        System.out.println(toStringRepresentation());
    }
}
