package Protocols;

/**
 * @author Robin Duda
 */
@FunctionalInterface
public interface Authenticator {
    Access access(Request request);
}
