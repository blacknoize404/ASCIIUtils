package io.github.blacknoize404.asciiutils;

/**
 * Códigos de escape ANSI para colorear la salida en terminal.
 * <p>
 * Constantes con los colores básicos de primer plano y métodos para
 * generar colores de 256 colores y truecolor (RGB).
 * </p>
 *
 * @author Barnés Inside
 * @version 0.3
 */
public final class Ansi {

    private static final String ESC = "";

    /** Resetea todos los atributos. */
    public static final String RESET = ESC + "[0m";

    public static final String BLACK = ESC + "[30m";
    public static final String RED = ESC + "[31m";
    public static final String GREEN = ESC + "[32m";
    public static final String YELLOW = ESC + "[33m";
    public static final String BLUE = ESC + "[34m";
    public static final String MAGENTA = ESC + "[35m";
    public static final String CYAN = ESC + "[36m";
    public static final String WHITE = ESC + "[37m";
    public static final String GRAY = ESC + "[90m";

    public static final String BOLD = ESC + "[1m";
    public static final String DIM = ESC + "[2m";

    private Ansi() {
    }

    /**
     * Color de primer plano de la paleta de 256 colores.
     *
     * @param n el índice de color {@code 0-255}
     * @return el código de escape
     */
    public static String fg(int n) {
        return ESC + "[38;5;" + n + "m";
    }

    /**
     * Color de fondo de la paleta de 256 colores.
     *
     * @param n el índice de color {@code 0-255}
     * @return el código de escape
     */
    public static String bg(int n) {
        return ESC + "[48;5;" + n + "m";
    }

    /**
     * Color de primer plano truecolor (RGB).
     *
     * @param r rojo  {@code 0-255}
     * @param g verde {@code 0-255}
     * @param b azul  {@code 0-255}
     * @return el código de escape
     */
    public static String rgb(int r, int g, int b) {
        return ESC + "[38;2;" + r + ";" + g + ";" + b + "m";
    }

    /**
     * Resuelve un nombre de color básico o un hex {@code #rrggbb} a su código.
     *
     * @param name el nombre ({@code red}, {@code cyan"}…) o {@code #rrggbb}
     * @return el código de escape, o {@code null} si no se reconoce
     */
    public static String parse(String name) {
        return switch (name.toLowerCase()) {
            case "black" -> BLACK;
            case "red" -> RED;
            case "green" -> GREEN;
            case "yellow" -> YELLOW;
            case "blue" -> BLUE;
            case "magenta" -> MAGENTA;
            case "cyan" -> CYAN;
            case "white" -> WHITE;
            case "gray", "grey" -> GRAY;
            default -> name.matches("#[0-9a-fA-F]{6}")
                    ? rgb(Integer.parseInt(name.substring(1, 3), 16),
                          Integer.parseInt(name.substring(3, 5), 16),
                          Integer.parseInt(name.substring(5, 7), 16))
                    : null;
        };
    }

    /**
     * Resuelve un nombre de color o un hex {@code #rrggbb} a sus componentes RGB.
     *
     * @param name el nombre ({@code red}, {@code cyan}…) o {@code #rrggbb}
     * @return {@code {r, g, b}} con valores {@code 0-255}, o {@code null} si no se reconoce
     */
    public static int[] parseRgb(String name) {
        return switch (name.toLowerCase()) {
            case "black" -> new int[]{0, 0, 0};
            case "red" -> new int[]{255, 0, 0};
            case "green" -> new int[]{0, 255, 0};
            case "yellow" -> new int[]{255, 255, 0};
            case "blue" -> new int[]{0, 0, 255};
            case "magenta" -> new int[]{255, 0, 255};
            case "cyan" -> new int[]{0, 255, 255};
            case "white" -> new int[]{255, 255, 255};
            case "gray", "grey" -> new int[]{128, 128, 128};
            default -> name.matches("#[0-9a-fA-F]{6}")
                    ? new int[]{
                        Integer.parseInt(name.substring(1, 3), 16),
                        Integer.parseInt(name.substring(3, 5), 16),
                        Integer.parseInt(name.substring(5, 7), 16)}
                    : null;
        };
    }
}
