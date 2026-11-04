package io.github.blacknoize404.asciiutils;

import java.util.Arrays;

/**
 * Representa un glifo como un arreglo bidimensional de caracteres.
 * <p>Puede ser una letra, un número, un carácter especial.</p>
 *
 * @author Barnés Inside
 * @version 0.2
 */
public final class ASCIIGlyph {

    /**
     * Los datos del carácter (filas × columnas).
     */
    private final char[][] data;

    /**
     * Construye un nuevo glifo usando una matriz de caracteres.
     * <p>Las filas pueden tener anchos distintos: se rellenan con espacios
     * hasta el ancho de la más larga.</p>
     *
     * @param data dicha matriz
     */
    public ASCIIGlyph(char[][] data) {
        this.data = normalize(data);
    }

    /**
     * Parser privado de una cadena de texto a una matriz bidimensional de caracteres.
     *
     * @param data la cadena a parsear
     * @return dicha matriz
     */
    private static char[][] parse(String data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("La cadena del glifo no puede ser vacía");
        }
        String[] rows = data.split("-", -1);
        char[][] letterData = new char[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            letterData[i] = rows[i].toCharArray();
        }
        return normalize(letterData);
    }

    /**
     * Rellena las filas con espacios hasta el ancho de la más larga y valida
     * que el glifo no sea vacío.
     *
     * @param data la matriz original
     * @return la matriz normalizada (nueva instancia)
     */
    private static char[][] normalize(char[][] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("El glifo necesita al menos una fila");
        }
        int width = 0;
        for (char[] row : data) {
            width = Math.max(width, row == null ? 0 : row.length);
        }
        if (width == 0) {
            throw new IllegalArgumentException("El glifo necesita al menos una columna");
        }
        char[][] normalized = new char[data.length][width];
        for (int i = 0; i < data.length; i++) {
            Arrays.fill(normalized[i], ' ');
            if (data[i] != null) {
                System.arraycopy(data[i], 0, normalized[i], 0, data[i].length);
            }
        }
        return normalized;
    }

    /**
     * Devuelve el glifo como una matriz bidimensional de caracteres (una copia).
     *
     * @return dicha matriz
     */
    public char[][] toArray() {
        char[][] copy = new char[data.length][];
        for (int i = 0; i < data.length; i++) {
            copy[i] = Arrays.copyOf(data[i], data[i].length);
        }
        return copy;
    }

    /**
     * Construye un nuevo glifo usando una cadena como cuerpo.
     * <p>
     * La cadena construirá el glifo usando el {@code -} como separador
     * de fila. Las filas cortas se rellenan con espacios hasta el ancho
     * de la más larga.
     * </p>
     * <p><b>Por ejemplo:</b></p>
     * <p>{@code ▓▓▓-▓ ▓-▓ ▓-▓▓▓-▓ ▓} produce la matriz:</p>
     * <pre>
     * ▓▓▓
     * ▓ ▓
     * ▓ ▓
     * ▓▓▓
     * ▓ ▓
     * </pre>
     *
     * @param partials la cadena de construcción
     * @return el glifo construido
     */
    public static ASCIIGlyph from(String partials) {
        return new ASCIIGlyph(parse(partials));
    }

    /**
     * Devuelve el ancho del glifo.
     *
     * @return dicho valor
     */
    public int width() {
        return data[0].length;
    }

    /**
     * Devuelve la altura del glifo.
     *
     * @return dicho valor
     */
    public int height() {
        return data.length;
    }

    /**
     * Devuelve una representación como string del glifo.
     *
     * @return dicha representación
     */
    public String toStringRepresentation() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < height(); row++) {
            sb.append(data[row]);
            if (row < height() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ASCIIGlyph{" + width() + "x" + height() + '}';
    }
}
