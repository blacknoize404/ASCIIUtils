package ASCIIUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Representa un glifo como un arreglo bidimensional de caracteres
 * <p>Puede ser una letra, un número, un carácter especial</p>
 *
 * @author Barnés Inside
 * @version 0.1
 */
public final class ASCIIGlyph {

    /**
     * Los datos del carácter
     */
    private final Character[][] data;

    /**
     * Construye un nuevo glifo usando una cadena como cuerpo
     * <p>
     * La cadena construirá el glifo usando el (-) como separador
     * de fila.
     * </p>
     * <p><b>Por ejemplo:</b></p>
     * <p>▓▓▓-▓ ▓-▓ ▓-▓▓▓-▓ ▓</p>
     * <p>Devolverá una matriz bidimensional</p>
     * <p>▓▓▓</p>
     * <p>▓ ▓</p>
     * <p>▓ ▓</p>
     * <p>▓▓▓</p>
     * <p>▓ ▓</p>
     *
     * @param partials la cadena de construcción
     */
    public ASCIIGlyph(String partials) {
        this.data = parse(partials);
    }

    /**
     * Constructor privado usado por el método from estático
     * para construir un carácter directamente sin escribir:
     * <code>new ASCII.ASCIIGlyph(partials);</code>
     *
     * @param data una matriz bidimensional de los caracteres
     */
    private ASCIIGlyph(Character[][] data) {
        this.data = data;
    }

    /**
     * Devuelve el glifo como una matriz bidimensional de caracteres.
     *
     * @return dicha matriz
     */
    public Character[][] toArray() {
        return data;
    }

    /**
     * Parser privado de una cadena de texto a una matriz bidimensional de caracteres.
     *
     * @param data la cadena a parsear
     * @return dicha matriz
     */
    private static Character[][] parse(String data) {

        String[] dataSplit = data.split("-");

        Character[][] letterData = new Character[dataSplit.length][dataSplit[0].length()];

        // Relleno secuencialmente cada fila
        for (int i = 0; i < letterData.length; i++) {

            // Con cada dato en cada columna de esa fila
            for (int j = 0; j < dataSplit[0].length(); j++) {

                char dataCharacter = dataSplit[i].charAt(j);

                letterData[i][j] = dataCharacter;

            }
        }

        return letterData;

    }

    /**
     * Construye un nuevo glifo usando una cadena como cuerpo
     * <p>
     * La cadena construirá el glifo usando el (-) como separador
     * de fila.
     * </p>
     * <p><b>Por ejemplo:</b></p>
     * <p>▓▓▓-▓ ▓-▓ ▓-▓▓▓-▓ ▓</p>
     * <p>Devolverá una matriz bidimensional</p>
     * <p>▓▓▓</p>
     * <p>▓ ▓</p>
     * <p>▓ ▓</p>
     * <p>▓▓▓</p>
     * <p>▓ ▓</p>
     *
     * @param partials la cadena de construcción
     */
    public static ASCIIGlyph from(String partials) {
        return new ASCIIGlyph(parse(partials));
    }

    /**
     * Devuelve el ancho del glifo
     *
     * @return dicho valor
     */
    public int width() {
        return data[0].length;
    }

    /**
     * Devuelve la altura del glifo
     *
     * @return dicho valor
     */
    public int height() {
        return data.length;
    }

    /**
     * Devuelve una representación como string del glifo
     *
     * @return dicha representación
     */
    @NotNull
    public String toStringRepresentation() {

        StringBuilder sb = new StringBuilder();

        // Por cada fila y columna de la matriz
        for (int row = 0; row < height(); row++) {
            for (int column = 0; column < width(); column++) {

                // Añado a la cadena carácter
                sb.append(data[row][column]);
            }

            // Salto una línea siempre y cuando no sea la última fila
            if (row < height() - 1) sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "ASCII.ASCIIGlyph{" +
                "data=" + Arrays.toString(data) +
                '}';
    }
}
