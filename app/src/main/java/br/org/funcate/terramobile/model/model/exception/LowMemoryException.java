package br.org.funcate.terramobile.model.model.exception;

/**
 * Created by bogo on 01/04/15.
 */
public final class LowMemoryException extends Exception {
    private static final long serialVersionUID = 146526524087765134L;

    public LowMemoryException(final String pDetailMessage) {
        super(pDetailMessage);
    }

    public LowMemoryException(final Throwable pThrowable) {
        super(pThrowable);
    }
}