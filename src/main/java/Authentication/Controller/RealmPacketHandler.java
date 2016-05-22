package Authentication.Controller;

/**
 * @author Robin Duda
 *         Template pattern method for handling realm packets.
 */

@FunctionalInterface
interface RealmPacketHandler {
    void handle(RealmConnection connection, String data);
}
