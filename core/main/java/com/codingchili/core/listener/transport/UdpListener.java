package com.codingchili.core.listener.transport;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.DeploymentAware;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.ListenerSettings;
import io.vertx.core.Future;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocketOptions;

import java.util.function.Supplier;

import static com.codingchili.core.configuration.CoreStrings.LOG_AT;
import static com.codingchili.core.configuration.CoreStrings.getBindAddress;

/**
 * @author Robin Duda
 * <p>
 * UDP transport listener.
 */
public class UdpListener implements CoreListener, DeploymentAware {
    private Supplier<ListenerSettings> settings = ListenerSettings::getDefaultSettings;
    private CoreHandler handler;
    private CoreContext core;

    @Override
    public void init(CoreContext core) {
        this.core = core;
        handler.init(core);
    }

    @Override
    public CoreListener settings(Supplier<ListenerSettings> settings) {
        this.settings = settings;
        return this;
    }

    @Override
    public CoreListener handler(CoreHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public void start(Future<Void> start) {
        core.vertx().createDatagramSocket().listen(settings.get().getPort(), getBindAddress(), listen -> {
            if (listen.succeeded()) {
                settings.get().addListenPort(listen.result().localAddress().port());
                listen.result().handler(this::handle);
                handler.start(start);
            } else {
                start.fail(listen.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> stop) {
        handler.stop(stop);
    }

    private void handle(DatagramPacket connection) {
        handler.handle(new UdpRequest(core, settings.get(), connection));
    }

    @Override
    public int instances() {
        return 1;
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + LOG_AT + handler.address() + " port :" +
                settings.get().getPort();
    }
}
