package br.org.funcate.terramobile.model.exception;

/**
 * Created by bogo on 08/04/15.
 */
public class DAOException extends Throwable {

    private static final long serialVersionUID = 146526524087765134L;

    public DAOException(final String message) {
        super(message);
    }

    public DAOException(final Throwable throwable) {
        super(throwable);
    }
    public DAOException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
