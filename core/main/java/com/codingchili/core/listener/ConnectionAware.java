package com.codingchili.core.listener;

/**
 * @author Robin Duda
 */
public interface ConnectionAware {

    /**
     *
     * @param client client
     */
    void onConnect(Client client);

    /**
     *
     * @param client client
     */
    void onDisconnect(Client client);

}
