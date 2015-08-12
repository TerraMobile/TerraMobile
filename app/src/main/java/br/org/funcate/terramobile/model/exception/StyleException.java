package br.org.funcate.terramobile.model.exception;

/**
 * Created by bogo on 08/04/15.
 */
public class StyleException extends Throwable {

    private static final long serialVersionUID = 146526524087765134L;

    public StyleException(final String message) {
        super(message);
    }

    public StyleException(final Throwable throwable) {
        super(throwable);
    }
    public StyleException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
