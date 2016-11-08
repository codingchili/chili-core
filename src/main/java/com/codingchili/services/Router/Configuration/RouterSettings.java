package com.codingchili.services.Router.Configuration;

import java.util.ArrayList;
import java.util.HashSet;

import com.codingchili.core.Configuration.ServiceConfigurable;
import com.codingchili.services.Shared.Strings;

import com.codingchili.services.Router.Model.WireType;

/**
 * @author Robin Duda
 */
public class RouterSettings extends ServiceConfigurable {
    public static final String PATH_ROUTING = Strings.getService("routingserver");
    private ArrayList<ListenerSettings> transport;
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
}