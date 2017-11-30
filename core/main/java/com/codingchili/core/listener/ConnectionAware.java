package com.codingchili.core.listener;

/**
 * @author Robin Duda
 */
public interface ConnectionAware {

    /**
     *
     * @param client
     */
    void onConnect(Client client);

    /**
     *
     * @param client
     */
    void onDisconnect(Client client);

}
