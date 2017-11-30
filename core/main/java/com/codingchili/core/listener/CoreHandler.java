package com.codingchili.core.listener;

import com.codingchili.core.protocol.Address;

/**
 * @author Robin Duda
 * <p>
 * A simplified handler that may be deployed directly.
 *
 * Supports DeploymentAware and ListenerAware.
 */
public interface CoreHandler extends CoreDeployment {

    /**
     * Handles an incoming request without exception handling.
     *
     * @param request the request to be handled.
     */
    void handle(Request request);

    /**
     * @return the address of the handler. If not implemented the @Address
     * annotation will be used, if missing an error is thrown.
     * <p>
     * Could potentially lead to Runtime errors but is allowed here as
     * this is called during deployment. Reconsider this decision later.
     */
    default String address() {
        Address annotation = getClass().getAnnotation(Address.class);
        if (annotation != null) {
            return annotation.value();
        } else {
            throw new RuntimeException("Class " + getClass().getName() + " does not" +
                    " implement CoreHandler::address nor is annotated with @Address.");
        }
    }
}