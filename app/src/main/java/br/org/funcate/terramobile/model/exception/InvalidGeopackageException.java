package br.org.funcate.terramobile.model.exception;

/**
 * Created by bogo on 08/04/15.
 */
public class InvalidGeopackageException extends Throwable {

    private static final long serialVersionUID = 146526524087765134L;

    public InvalidGeopackageException(final String message) {
        super(message);
    }

    public InvalidGeopackageException(final Throwable throwable) {
        super(throwable);
    }
    public InvalidGeopackageException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
