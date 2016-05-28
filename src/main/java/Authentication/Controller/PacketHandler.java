package Authentication.Controller;

/**
 * @author Robin Duda
 */

@FunctionalInterface
public interface PacketHandler<Request> {
    void handle(Request request);
}
