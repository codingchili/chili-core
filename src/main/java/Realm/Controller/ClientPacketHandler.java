package Realm.Controller;

import Realm.Model.Connection;

/**
 * @author Robin Duda
 *         Handles packets from clients.
 */
interface ClientPacketHandler {
    /**
     * @param connection the connection id for writing to.
     * @param packet the raw data.
     */
    void handle(Connection connection, String packet);
}
