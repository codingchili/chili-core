package Realm.Controller;

/**
 * @author Robin Duda
 * Template for handling packets from the authentication server.
 */
interface AuthPacketHandler {
    /**
     * @param connection connection id for writing to.
     * @param packet raw message data.
     */
    void handle(String connection, String packet);
}
