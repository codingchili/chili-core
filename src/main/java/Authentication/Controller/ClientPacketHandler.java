package Authentication.Controller;

/**
 * @author Robin Duda
 */

@FunctionalInterface
interface ClientPacketHandler {
    void handle(ClientRequest request);
}
