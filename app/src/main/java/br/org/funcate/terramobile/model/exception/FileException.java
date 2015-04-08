package br.org.funcate.terramobile.model.exception;

/**
 * Created by bogo on 08/04/15.
 */
public class FileException extends Throwable {

    private static final long serialVersionUID = 146526524087765134L;

    public FileException(final String message) {
        super(message);
    }

    public FileException(final Throwable throwable) {
        super(throwable);
    }
    public FileException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
