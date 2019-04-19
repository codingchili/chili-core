package com.codingchili.core.listener;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.Address;

/**
 * A simplified handler that may be deployed directly.
 * <p>
 * Supports DeploymentAware and ListenerAware.
 */
public interface CoreHandler extends Receiver<Request>, CoreDeployment {

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
            throw new CoreRuntimeException("Class " + getClass().getName() + " does not" +
                    " implement CoreHandler::address nor is annotated with @Address.");
        }
    }
}