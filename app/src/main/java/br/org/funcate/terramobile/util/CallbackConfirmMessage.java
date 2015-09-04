package br.org.funcate.terramobile.util;

/**
 * Use this interface when using the confirm UI implemented on Message.showConfirmMessage.
 * Implement this to manipulate the response of the confirm UI message.
 *
 * Created by Andre Carvalho on 26/08/15.
 */
public interface CallbackConfirmMessage {
    /**
     * This method is calling when one choice is selected.
     * @param response, if user choice is yes, response is true. Otherwise the response is false.
     */
    void confirmResponse(boolean response);

    /**
     * Provide one code to determine who called.
     * @param whoCall, a code of the caller
     */
    void setWhoCall(int whoCall);
}
