package io.github.blacknoize404.asciiutils;

/**
 * Motor de smushing estilo FIGlet: decide cuánto se solapan dos filas
 * consecutivas de glifos y qué carácter resulta de la colisión.
 * <p>
 * Los modos corresponden a los layouts de FIGlet: {@link Mode#FULL_WIDTH}
 * (sin solape), {@link Mode#KERNING} (solape mientras choquen espacios),
 * {@link Mode#UNIVERSAL} (el carácter de la derecha gana siempre, salvo que
 * el izquierdo sea el hardblank, que gana el derecho) y
 * {@link Mode#CONTROLLED} (aplica las reglas activas de la máscara
 * {@code rules} igual que el {@code fullLayout} de un fichero {@code .flf}).
 * </p>
 *
 * @author Barnés Inside
 * @version 0.3
 */
final class Smusher {

    /** Modos de composición de los glifos en una línea. */
    enum Mode {
        /** Sin solape: los glifos se concatenan enteros más el kerning. */
        FULL_WIDTH,
        /** Solapa mientras los caracteres en colisión sean espacios. */
        KERNING,
        /** Cualquier par no-espacio colisiona; gana el de la derecha. */
        UNIVERSAL,
        /** Aplica la máscara de reglas FIGlet (bits 1–64). */
        CONTROLLED,
    }

    /** Reglas FIGlet (máscara de bits). */
    static final int RULE_EQUAL = 1;
    static final int RULE_UNDERSCORE = 2;
    static final int RULE_HIERARCHY = 4;
    static final int RULE_OPPOSITE = 8;
    static final int RULE_BIGX = 16;
    static final int RULE_HARDBLANK = 32;
    static final int RULE_ALL = RULE_EQUAL | RULE_UNDERSCORE | RULE_HIERARCHY | RULE_OPPOSITE | RULE_BIGX | RULE_HARDBLANK;

    private Smusher() {
    }

    /**
     * Decide si dos caracteres pueden fusionarse y con qué resultado.
     *
     * @param l         el carácter de la izquierda ({@code '\0'} = vacío)
     * @param r         el carácter de la derecha
     * @param mode      el modo de smushing
     * @param rules     máscara de reglas (solo para {@link Mode#CONTROLLED})
     * @param hardBlank el carácter hardblank de la fuente
     * @return el carácter resultante, o {@code '\0'} si no pueden fusionarse
     */
    static char smush(char l, char r, Mode mode, int rules, char hardBlank) {
        if (mode == Mode.FULL_WIDTH) return '\0';
        if (l == ' ' || l == '\0') return r;
        if (r == ' ' || r == '\0') return l;
        if (mode == Mode.KERNING) return '\0';

        if (mode == Mode.UNIVERSAL) {
            return l == hardBlank ? hardBlank : r;
        }

        // CONTROLLED: los hardblanks solo se fusionan entre sí (regla 6)
        if (l == hardBlank || r == hardBlank) {
            return (rules & RULE_HARDBLANK) != 0 && l == hardBlank && r == hardBlank ? hardBlank : '\0';
        }
        if ((rules & RULE_EQUAL) != 0 && l == r) return l;
        if ((rules & RULE_UNDERSCORE) != 0 && l == '_' && "|/\\[]{}()<>".indexOf(r) >= 0) return r;
        if ((rules & RULE_UNDERSCORE) != 0 && r == '_' && "|/\\[]{}()<>".indexOf(l) >= 0) return l;
        if ((rules & RULE_HIERARCHY) != 0) {
            int hl = hierarchy(l), hr = hierarchy(r);
            if (hl > 0 && hr > 0 && hl != hr) return hl > hr ? l : r;
        }
        if ((rules & RULE_OPPOSITE) != 0) {
            if (l == '[' && r == ']' || l == ']' && r == '['
                    || l == '{' && r == '}' || l == '}' && r == '{'
                    || l == '(' && r == ')' || l == ')' && r == '(') return '|';
        }
        if ((rules & RULE_BIGX) != 0) {
            if (l == '/' && r == '\\' || l == '\\' && r == '/') return l == '/' ? '|' : 'Y';
            if (l == '>' && r == '<') return 'X';
        }
        return '\0';
    }

    /**
     * Jerarquía de la regla 3 de FIGlet: {@code |} < {@code /\} <
     * {@code []} < {@code {}} < {@code ()} < {@code <>}.
     *
     * @param c el carácter
     * @return su nivel (0 si no es de la jerarquía)
     */
    private static int hierarchy(char c) {
        return switch (c) {
            case '|' -> 1;
            case '/', '\\' -> 2;
            case '[', ']' -> 3;
            case '{', '}' -> 4;
            case '(', ')' -> 5;
            case '<', '>' -> 6;
            default -> 0;
        };
    }

    /**
     * Cuántas columnas puede solaparse {@code add} sobre el final de
     * {@code base} en esta fila: los espacios finales de {@code base} más los
     * iniciales de {@code add}, más 1 si el par de frontera puede fusionarse.
     *
     * @param base      la fila ya escrita
     * @param baseLen   longitud usada de {@code base}
     * @param add       la fila del glifo a añadir
     * @param mode      modo de smushing
     * @param rules     máscara de reglas
     * @param hardBlank el hardblank de la fuente
     * @return las columnas de solape posibles
     */
    static int smushAmount(char[] base, int baseLen, char[] add, Mode mode, int rules, char hardBlank) {
        if (mode == Mode.FULL_WIDTH || baseLen == 0) return 0;
        int trailing = 0;
        for (int i = baseLen - 1; i >= 0 && base[i] == ' '; i--) trailing++;
        int leading = 0;
        while (leading < add.length && add[leading] == ' ') leading++;
        int overlap = Math.min(trailing + leading, Math.min(add.length, baseLen));
        // Un carácter extra si el par de frontera puede fusionarse
        if (overlap < add.length && trailing < baseLen && leading < add.length) {
            char l = base[baseLen - trailing - 1];
            char r = add[leading];
            if (smush(l, r, mode, rules, hardBlank) != '\0') overlap++;
        }
        return overlap;
    }

    /**
     * Escribe una fila de glifo sobre {@code base} aplicando el solape dado:
     * las celdas solapadas se resuelven con {@link #smush}.
     *
     * @param base      la fila destino
     * @param baseLen   longitud usada antes de escribir
     * @param add       la fila del glifo
     * @param overlap   columnas de solape
     * @param mode      modo de smushing
     * @param rules     máscara de reglas
     * @param hardBlank el hardblank de la fuente
     * @return la nueva longitud usada de {@code base}
     */
    static int merge(char[] base, int baseLen, char[] add, int overlap, Mode mode, int rules, char hardBlank) {
        int start = baseLen - overlap;
        int len = Math.max(baseLen, start + add.length);
        for (int i = 0; i < add.length && start + i < base.length; i++) {
            char c = add[i];
            if (c == '\0') continue;
            int pos = start + i;
            if (pos >= baseLen) {
                base[pos] = c;
            } else if (base[pos] == '\0') {
                base[pos] = c; // cubre el hueco: un espacio del glifo borra el fondo
            } else if (base[pos] == ' ') {
                if (c != ' ') base[pos] = c;
            } else if (c != ' ') {
                char s = smush(base[pos], c, mode, rules, hardBlank);
                if (s != '\0') base[pos] = s;
            }
        }
        return len;
    }
}
