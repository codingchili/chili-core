package com.codingchili.core.listener;

import com.codingchili.core.context.CoreContext;

/**
 * A bus routerhandler with a static delivery address.
 */
public class BusForwarder extends BusRouter {
    private CoreContext core;
    private String address;

    public BusForwarder(String address) {
        this.address = address;
    }

    @Override
    public void init(CoreContext context) {
        this.core = core;
    }

    @Override
    public void handle(Request request) {
        super.send(request, address);
    }

    @Override
    public String address() {
        return address;
    }
}
