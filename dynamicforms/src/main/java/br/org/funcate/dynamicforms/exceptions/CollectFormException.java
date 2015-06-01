package br.org.funcate.dynamicforms.exceptions;

/**
 * Created by Andre Carvalho on 01/06/15.
 */
public class CollectFormException extends Exception {
    private static final long serialVersionUID = 646526524984565134L;

    public CollectFormException(final String pDetailMessage) {
        super(pDetailMessage);
    }
    public CollectFormException(final Throwable pThrowable) {
        super(pThrowable);
    }
    // Refactoring code call from MainActivity to FragmentDetailActivity
}
