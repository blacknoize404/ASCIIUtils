package ASCIIUtils;

import java.util.Arrays;

/**
 * Interpreta textos y genera texto ASCII, dado un texto y una fuente.
 *
 * @author Barnés Inside
 * @version 0.1
 * @implNote Como ejemplo se puede permitir de argumento cualquier
 * método que coincida con el de la interfaz Consumer,
 * o cualquier clase que la implemente.
 * {@snippet :
 * // Inicializo el intérprete
 *  ASCIIText t = new ASCIIText("Hola mundo", new ASCIIFont());
 *
 * // Devuelve la representación del texto usando la fuente dada
 *  String output = t.toStringRepresentation();
 *}
 */
public class ASCIIText {

    /**
     * Los tipos de fondos disponibles en opacidad ASCII.
     * <table>
     *     <tr>
     *         <td>BLANK</td>
     *         <td>Espacios en blanco</td>
     *     </tr>
     *     <tr>
     *         <td>PERCENT25</td>
     *         <td>░</td>
     *     </tr>
     *     <tr>
     *         <td>PERCENT50</td>
     *         <td>▒</td>
     *     </tr>
     *     <tr>
     *         <td>PERCENT75</td>
     *         <td>▓</td>
     *     </tr>
     *     <tr>
     *         <td>PERCENT100</td>
     *         <td>█</td>
     *     </tr>
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
     * La cadena a procesar en formato ASCII
     */
    private String text;

    /**
     * La fuente del formato ASCII
     */
    private ASCIIFont font;

    /**
     * La matriz de caracteres que representará al texto.
     * <p>
     * Esta se va a actualizar cuando se vaya a generar
     * la matriz de visualización.
     * </p>
     * <p>
     * Una vez finalizada la generación, se queda en caché.
     * </p>
     */
    private Character[][] matrix;

    /**
     * La posición del carrete.
     * <p>
     * Esta se va a actualizar cuando se vaya a generar
     * la matriz de visualización para colocar apropiadamente
     * el carácter en su zona.
     * </p>
     * <p>
     * Una vez finalizada la generación se inicializa en 0.
     * </p>
     */
    private int carriagePosition = 0;

    /**
     * La posición de la línea (para textos multilínea).
     * <p>
     * Esta se va a actualizar cuando se vaya a generar
     * la matriz de visualización para colocar apropiadamente
     * el carácter en su zona.
     * </p>
     * <p>
     * Una vez finalizada la generación se inicializa en 0.
     * </p>
     */
    private int linePosition = 0;

    /**
     * Define el nivel de espaciado entre las letras
     */
    private int kerning = 1;

    /**
     * Define el espacio entre las líneas del salto de línea
     */
    private int lineHeight = 1;

    /**
     * Define el ancho que ocupará el texto
     */
    private int width = 0;

    /**
     * Define la altura que ocupará el texto
     */
    private int height = 0;

    /**
     * Define si el texto ha sido modificado.
     * <p>
     * Esta bandera es útil para evitar volver a generar una
     * matriz de caracteres si previamente se ha generado
     * ya una para el mismo texto.
     * </p>
     * <p>
     * Solo cambiará a true si se ha cambiado el texto o la fuente
     * y cambiará a false después de la primera vez que se genere
     * la matriz (la matriz siempre se generará de nuevo si este
     * atributo es true).
     * </p>
     */
    private boolean isModified = false;

    /**
     * Representación en caché del texto una vez generada por primera vez
     * la matriz.
     * <p>
     * Útil para devolver en {@link #toStringRepresentation()} la
     * cadena si esta no ha cambiado o la fuente no ha cambiado,
     * en vez de generarla de nuevo.
     * </p>
     */
    private String representation;

    /**
     * Define el tipo de fondo a rellenar los caracteres.
     */
    private BackgroundType background = BackgroundType.BLANK;

    /**
     * Construye una nueva instancia con una fuente ASCII dada.
     *
     * @param font dicha fuente
     */
    public ASCIIText(ASCIIFont font) {

        this.font = font;

    }

    /**
     * Construye una nueva instancia con un texto y una fuente ASCII dada.
     *
     * @param text dicho texto
     * @param font dicha fuente
     */
    public ASCIIText(String text, ASCIIFont font) {

        this.font = font;
        this.setText(text);

    }

    /**
     * Forma optimizada que encontré para obtener las coincidencias de una letra en una palabra
     * sin iterar en <b>O(n)</b>. Disminuye la iteración a <b>O(a)</b> siendo <b>a</b> la cantidad de coincidencias.
     *
     * @param text  el texto donde buscar coincidencias
     * @param match el fragmento que se pretende encontrar en el texto.
     * @return la cantidad de coincidencias
     */
    private int getCoincidencesIn(String text, String match) {

        // Creo un contador de coincidencias
        int coincidences = 0;

        // Inicializo un índice
        int index = 0;

        // Mientras se encuentren coincidencias en el texto
        while (text.indexOf(match, index) != -1) {

            // El índice apuntará al siguiente índice después de la coincidencia
            index = text.indexOf(match, index) + 1;

            // Incremento la coincidencia
            coincidences++;

        }

        return coincidences;
    }

    /**
     * Convierte el texto en una matriz de caracteres correspondiente
     * a la fuente.
     */
    public void fillMatrix() {

        if (this.text == null || this.text.isEmpty()) return;


        // Creo un nuevo grid con el tamaño de alto y ancho pre-calculado
        this.matrix = new Character[this.getHeight()][this.getWidth()];

        String letters = this.text.toLowerCase();

        for (char character : letters.toCharArray()) {

            // Si es un salto de línea
            if (character == '\n') {

                // Incremento la posición de la línea
                this.linePosition++;

                // Reinicio la posición del cursor así comienza a la izquierda
                this.carriagePosition = 0;

                continue;
            }

            // Escribe el glifo
            write(font.getGlyph(character));
        }

        this.linePosition = 0;
        this.carriagePosition = 0;

    }

    /**
     * Escribe la representación de un glifo en la fuente ASCII
     * en el espacio definido dentro de la matriz de caracteres.
     *
     * @param glyph dicho glifo ASCII
     */
    private void write(ASCIIGlyph glyph) {

        // Obtiene la matriz de caracteres del glifo
        Character[][] characters = glyph.toArray();

        // Si la posición del cursor de texto no está dentro del espacio de la imagen...
        if (carriagePosition + glyph.width() > matrix[0].length) return;

        for (int rowIndex = 0; rowIndex < characters.length; rowIndex++) {
            for (int colIndex = 0; colIndex < characters[rowIndex].length; colIndex++) {

                // Comprobación de seguridad de nulidad, no es estrictamente necesaria
                // porque los Character son inmutables, pero si en un futuro
                // usaré objetos, es prudente dejarla.
                // Si es un espacio, dejar en nulo el carácter en la matrix (para procesamientos futuros de fondo)
                if (characters[rowIndex][colIndex] == null || characters[rowIndex][colIndex] == ' ') continue;

                // Calcula la posición de la línea en el arreglo
                int lineCalculated = this.linePosition * (this.font.getHeight() + this.lineHeight);

                // Escribe el carácter
                matrix[rowIndex + lineCalculated][colIndex + carriagePosition] = characters[rowIndex][colIndex];

            }

        }

        // Coloca el cursor en el espacio después de la letra y el kerning
        carriagePosition += glyph.width() + kerning;

    }

    /**
     * Obtiene el ancho que ocupa el texto
     *
     * @return dicho valor
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Obtiene el largo que ocupa el texto
     *
     * @return dicho valor
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Obtiene el valor de la separación entre las letras
     *
     * @return dicho valor
     */
    public int getKerning() {
        return kerning;
    }

    /**
     * Define el valor de separación entre las letras
     * <p>
     * Si el valor nuevo es distinto del almacenado previamente,
     * recalcula las dimensiones del texto y coloca la bandera
     * {@link #isModified} a <code>true</code> para requerir una
     * reescritura la próxima vez que se ejecute
     * {@link #toStringRepresentation()}.
     * </p>
     *
     * @param kerning dicho valor
     */
    public void setKerning(int kerning) {
        if (this.kerning == kerning) return;

        this.kerning = kerning;
        this.isModified = true;
        updateTextSize();

    }

    /**
     * Obtiene el tamaño de separación de línea
     *
     * @return dicho tamaño
     */
    public int getLineHeight() {
        return this.lineHeight;
    }

    /**
     * Asigna el tamaño de la separación de línea
     * <p>
     * Si el valor nuevo es distinto del almacenado previamente,
     * recalcula las dimensiones del texto y coloca la bandera
     * {@link #isModified} a <code>true</code> para requerir una
     * reescritura la próxima vez que se ejecute
     * {@link #toStringRepresentation()}.
     * </p>
     *
     * @param lineHeight dicho tamaño
     */
    public void setLineHeight(int lineHeight) {

        if (this.lineHeight == lineHeight) return;

        this.lineHeight = lineHeight;
        this.isModified = true;
        updateTextSize();

    }

    /**
     * Calcula el ancho del texto a escribir usando la fuente y el kerning
     * definido.
     * <p>
     * Si el texto tiene un salto de línea, devolvemos la línea con el
     * mayor ancho.
     * </p>
     *
     * @return dicho ancho
     */
    private int getTextWidth() {

        // Si el texto está vacío, tiene 0 unidades de ancho
        if (this.text == null || this.text.isEmpty()) return 0;

        int maxWidth = 0;

        // Por cada línea de texto
        String[] lines = this.text.split("\n");

        for (String line : lines) {

            int lineWidth = 0;
//            System.out.println(maxWidth);

            // Por cada carácter en la línea
            for (char character : line.toCharArray()) {

                ASCIIGlyph glyph = font.getGlyph(character);

                // Añado a count el ancho
                lineWidth += glyph.width();

            }

            // Añado los espacios definidos por el kerning entre las letras
            lineWidth += getKerning() * (line.length() - 1);

            // Almacenamos solo el mayor de las anchuras
            maxWidth = Math.max(maxWidth, lineWidth);

        }
        System.out.println(maxWidth);
        return maxWidth;
    }

    /**
     * Calcula la altura del texto a escribir incluyendo los
     * saltos de línea.
     *
     * @return dicha altura
     */
    private int getTextHeight() {

        // Si el texto está vacío, tiene 0 unidades de alto
        if (this.text == null || this.text.isEmpty()) return 0;

        // Obtengo la cantidad de saltos de línea que hay en el texto
        int breakLinesCount = getCoincidencesIn(this.text, "\n");

        // Obtendrá la cantidad de espacio vertical usará el texto
        // considerando los saltos de línea del texto y su tamaño
        return font.getHeight() * (breakLinesCount + 1) + (breakLinesCount * this.lineHeight);

    }

    /**
     * Actualiza el tamaño que ocupará el texto en pantalla.
     * <p>Es requerido actualizar el texto cuando se cambie la fuente o el texto.</p>
     */
    private void updateTextSize() {
        this.height = getTextHeight();
        this.width = getTextWidth();
    }

    /**
     * Obtiene el texto a convertir en ASCII
     *
     * @return dicho texto
     */
    public String getText() {
        return text;
    }

    /**
     * Actualiza el texto a representar con la fuente y recalcula
     * las dimensiones del texto si el texto insertado es distinto
     * del almacenado.
     * <p>
     * Además, si el texto insertado es distinto del almacenado
     * coloca la bandera {@link #isModified} a <code>true</code>
     * para requerir una reescritura la próxima vez que se ejecute
     * {@link #toStringRepresentation()}.
     * </p>
     *
     * @param text dicho texto
     */
    public void setText(String text) {

        if (getText() != null && getText().equals(text)) return;

        this.text = text;
        this.isModified = true;

        updateTextSize();
    }

    public String getOf(String text) {

        this.setText(text);
        return toStringRepresentation();

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
     * Asigna una nueva fuente a usar y recalcula las dimensiones del
     * texto.
     * <p>
     * Además, coloca la bandera {@link #isModified} a <code>true</code>
     * para requerir una reescritura la próxima vez que se ejecute
     * {@link #toStringRepresentation()}.
     * </p>
     *
     * @param newFont dicha fuente
     */
    public void setFont(ASCIIFont newFont) {
        this.font = newFont;
        this.isModified = true;
        updateTextSize();

    }

    /**
     * Obtiene el carácter del fondo seleccionado
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
     * Asigna el fondo del texto
     *
     * @param background dicho fondo
     */
    public void setBackground(BackgroundType background) {
        this.background = background;
    }

    /**
     * Devuelve la matriz de caracteres generada
     *
     * @return dicha matriz.
     */
    public Character[][] toCharMatrix() {

        // Lleno la matrix de caracteres si no está llena
        if (matrix == null || matrix.length == 0) fillMatrix();

        return matrix;
    }

    /**
     * Devuelve una cadena que representa al texto en ASCII
     *
     * @return dicha cadena
     */
    public String toStringRepresentation() {

        // Si el texto no ha sido modificado, ya esa cambiando el salto de línea
        // el kerning, el texto o la fuente.
        if (!isModified) return representation;

        // Lleno la matriz de caracteres
        fillMatrix();

        StringBuilder sb = new StringBuilder();

        // Por cada carácter en la matrix
        for (Character[] row : matrix) {

            for (Character character : row) {

                // Añado el carácter a la cadena, si es nulo, añado un espacio
                sb.append((character != null) ? character : getBackground());
            }

            // Salto una línea siempre y cuando no sea la última fila
            if (row != matrix[matrix.length - 1]) sb.append("\n");
        }

        // Almaceno la representación en caché
        this.representation = sb.toString();

        // Defino la bandera a false ya que el resultado se ha almacenado
        // en caché así la próxima vez que se requiera generar una representación
        // del texto, se evita hacer el procedimiento de nuevo (siempre y cuando
        // no se cambien las propiedades del kerning, salto de línea, texto o fuente.
        this.isModified = false;

        return this.representation;

    }

}
