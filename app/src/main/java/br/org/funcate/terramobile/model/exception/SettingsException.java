package br.org.funcate.terramobile.model.exception;

/**
 * Created by bogo on 08/04/15.
 */
public class SettingsException extends Throwable {

    private static final long serialVersionUID = 146526524087765134L;

    public SettingsException(final String message) {
        super(message);
    }

    public SettingsException(final Throwable throwable) {
        super(throwable);
    }
    public SettingsException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
