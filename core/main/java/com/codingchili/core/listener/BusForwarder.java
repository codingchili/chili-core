package com.codingchili.core.listener;

import com.codingchili.core.context.CoreContext;

/**
 * @author Robin Duda
 *         <p>
 *         Simple handler that forwards messages on the event bus.
 */
public class BusForwarder implements CoreHandler {
    private CoreContext core;
    private String address;

    public BusForwarder(CoreContext core, String address) {
        this.address = address;
        this.core = core;
    }

    @Override
    public void handle(Request request) {
        core.bus().send(address, request.data());
    }

    @Override
    public String address() {
        return address;
    }
}
