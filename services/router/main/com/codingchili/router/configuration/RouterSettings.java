package com.codingchili.router.configuration;

import java.util.ArrayList;
import java.util.HashSet;

import com.codingchili.core.configuration.ServiceConfigurable;

import com.codingchili.router.model.WireType;

import static com.codingchili.core.configuration.CoreStrings.getService;

/**
 * @author Robin Duda
 */
public class RouterSettings extends ServiceConfigurable {
    public static final String PATH_ROUTING = getService("routingserver");
    private ArrayList<ListenerSettings> transport = defaultTransport();
    private HashSet<String> hidden = new HashSet<>();

    public ArrayList<ListenerSettings> getTransport() {
        return transport;
    }

    public void setTransport(ArrayList<ListenerSettings> transport) {
        this.transport = transport;
    }

    public HashSet<String> getHidden() {
        return hidden;
    }

    public void setHidden(HashSet<String> hidden) {
        this.hidden = hidden;
    }

    public ListenerSettings getListener(WireType type) {
        for (ListenerSettings listener : transport) {
            if (listener.getType().equals(type)) {
                return listener;
            }
        }
        throw new RuntimeException("No listener configured for " + type.name());
    }

    private ArrayList<ListenerSettings> defaultTransport() {
        ArrayList<ListenerSettings> transports = new ArrayList<>();
        transports.add(new ListenerSettings());
        return transports;
    }
}
