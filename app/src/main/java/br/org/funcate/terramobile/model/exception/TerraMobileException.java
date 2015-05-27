package br.org.funcate.terramobile.model.exception;

/**
 * Created by bogo on 08/04/15.
 */
public class TerraMobileException extends Throwable {

    private static final long serialVersionUID = 146526524087765134L;

    public TerraMobileException(final String message) {
        super(message);
    }

    public TerraMobileException(final Throwable throwable) {
        super(throwable);
    }
    public TerraMobileException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
