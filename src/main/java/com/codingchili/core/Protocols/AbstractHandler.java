package com.codingchili.core.Protocols;


/**
 * @author Robin Duda
 */
public abstract class AbstractHandler {
    private String address;

    protected AbstractHandler(String address) {
        this.address = address;
    }

    public abstract void handle(Request request);

    /**
     * Get the address of which the handler is providing handlers for.
     * @return the address as a string representation.
     */
    String getAdddress() {
        return address;
    }
}
