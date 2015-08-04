package br.org.funcate.terramobile.model.exception;

/**
 * Created by bogo on 08/04/15.
 */
public class InvalidAppConfigException extends Throwable {

    private static final long serialVersionUID = 146526524087765134L;

    public InvalidAppConfigException(final String message) {
        super(message);
    }

    public InvalidAppConfigException(final Throwable throwable) {
        super(throwable);
    }
    public InvalidAppConfigException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
