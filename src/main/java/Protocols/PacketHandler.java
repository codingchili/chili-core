package Protocols;

/**
 * @author Robin Duda
 */

@FunctionalInterface
public interface PacketHandler {
    void handle(Request request) throws Exception;
}
