package br.org.funcate.terramobile.model.exception;

/**
 * Created by bogo on 08/04/15.
 */
public class ProjectException extends Throwable {

    private static final long serialVersionUID = 146526524087765134L;

    public ProjectException(final String message) {
        super(message);
    }

    public ProjectException(final Throwable throwable) {
        super(throwable);
    }
    public ProjectException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
