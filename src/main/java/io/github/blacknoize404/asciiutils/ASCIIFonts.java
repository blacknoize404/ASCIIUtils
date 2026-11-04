package io.github.blacknoize404.asciiutils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Fuentes ASCII incluidas en la librería.
 * <p>
 * Las fuentes viven como recursos de texto en el classpath
 * ({@code /fonts/<nombre>.txt}), en el formato propio de
 * {@link ASCIIFont#load(Reader)}.
 * </p>
 *
 * @author Barnés Inside
 * @version 0.3
 */
public final class ASCIIFonts {

    /**
     * Fuente mínima (altura 6), pensada para nicks y textos cortos.
     */
    public static final ASCIIFont MINIMAL = loadResource("/fonts/minimal.txt");

    /**
     * Fuente moderna (altura 6), más ancha y detallada.
     */
    public static final ASCIIFont MODERN = loadResource("/fonts/modern.txt");

    private ASCIIFonts() {
    }

    /**
     * Carga una fuente desde un recurso del classpath.
     *
     * @param resource la ruta del recurso (p. ej. {@code "/fonts/modern.txt"})
     * @return la fuente cargada
     * @throws UncheckedIOException si el recurso no existe o no puede parsearse
     */
    public static ASCIIFont loadResource(String resource) {
        try (InputStream in = ASCIIFonts.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Recurso no encontrado: " + resource);
            }
            return ASCIIFont.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar la fuente " + resource, e);
        }
    }
}
