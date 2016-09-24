package Protocols;

/**
 * @author Robin Duda
 */
@FunctionalInterface
public interface AuthenticationMethod {
    Access access(Request request);
}
