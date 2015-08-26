package br.org.funcate.terramobile.util;

/**
 * Created by Andre Carvalho on 26/08/15.
 */
public interface CallbackConfirmMessage {
    void confirmResponse(boolean response);
    void setWhoCall(int whoCall);
}
