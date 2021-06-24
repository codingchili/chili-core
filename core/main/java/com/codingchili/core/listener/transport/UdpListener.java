package com.codingchili.core.listener.transport;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.DeploymentAware;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.ListenerSettings;
import static com.codingchili.core.configuration.CoreStrings.LOG_AT;
import static com.codingchili.core.configuration.CoreStrings.getBindAddress;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.datagram.DatagramPacket;

/**
 * UDP transport listener.
 */
public class UdpListener implements CoreListener, DeploymentAware {
    private ListenerSettings settings = ListenerSettings.getDefaultSettings();
    private CoreHandler handler;
    private CoreContext core;

    @Override
    public void init(CoreContext core) {
        this.core = core;
        handler.init(core);
    }

    @Override
    public CoreListener settings(ListenerSettings settings) {
        this.settings = settings;
        return this;
    }

    @Override
    public CoreListener handler(CoreHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public void start(Promise<Void> start) {
        core.vertx().createDatagramSocket().listen(settings.getPort(), getBindAddress(), listen -> {
            if (listen.succeeded()) {
                settings.addListenPort(listen.result().localAddress().port());
                listen.result().handler(this::handle);
                handler.start(start);
            } else {
                start.fail(listen.cause());
            }
        });
    }

    @Override
    public void stop(Promise<Void> stop) {
        handler.stop(stop);
    }

    private void handle(DatagramPacket connection) {
        handler.handle(new UdpRequest(core, settings, connection));
    }

    @Override
    public int instances() {
        return 1;
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + LOG_AT + handler.address() + " port :" +
                settings.getPort();
    }
}
