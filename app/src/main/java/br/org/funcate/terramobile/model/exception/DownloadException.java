package br.org.funcate.terramobile.model.exception;

/**
 * Created by bogo on 08/04/15.
 */
public class DownloadException extends Throwable {

    private static final long serialVersionUID = 146526524087765134L;

    public DownloadException(final String message) {
        super(message);
    }

    public DownloadException(final Throwable throwable) {
        super(throwable);
    }
    public DownloadException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
