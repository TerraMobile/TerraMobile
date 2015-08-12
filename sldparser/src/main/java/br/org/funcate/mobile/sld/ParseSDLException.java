package br.org.funcate.mobile.sld;

/**
 * Created by bogo on 08/04/15.
 */
public class ParseSDLException extends Throwable {

    private static final long serialVersionUID = 146526524087765134L;

    public ParseSDLException(final String message) {
        super(message);
    }

    public ParseSDLException(final Throwable throwable) {
        super(throwable);
    }
    public ParseSDLException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
