package Authentication.Controller;

/**
 * @author Robin Duda
 *         Template pattern method for handling realm packets.
 */

@FunctionalInterface
public interface RealmPacketHandler {
    void handle(RealmRequest request);
}
