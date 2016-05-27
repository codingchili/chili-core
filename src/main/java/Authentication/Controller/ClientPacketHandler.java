package Authentication.Controller;

/**
 * @author Robin Duda
 */

@FunctionalInterface
public interface ClientPacketHandler {
    void handle(ClientRequest request);

    String x = "";
}
