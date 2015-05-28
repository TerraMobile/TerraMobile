package br.org.funcate.jgpkg.exception;

/**
 * Created by bogo on 08/04/15.
 */
public class QueryException extends Throwable {

    private static final long serialVersionUID = 146526524087765134L;

    public QueryException(final String message) {
        super(message);
    }

    public QueryException(final Throwable throwable) {
        super(throwable);
    }
    public QueryException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
