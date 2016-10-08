package com.codingchili.core.Routing.Configuration;

import com.codingchili.core.Configuration.Configurable;
import com.codingchili.core.Configuration.RemoteAuthentication;
import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Routing.Model.ListenerSettings;
import com.codingchili.core.Routing.Model.WireType;

import java.util.ArrayList;

/**
 * @author Robin Duda
 */
public class RoutingSettings implements Configurable {
    private RemoteAuthentication logserver;
    private ArrayList<ListenerSettings> transport;

    public ArrayList<ListenerSettings> getTransport() {
        return transport;
    }

    public void setTransport(ArrayList<ListenerSettings> transport) {
        this.transport = transport;
    }

    public void setLogserver(RemoteAuthentication logserver) {
        this.logserver = logserver;
    }

    @Override
    public RemoteAuthentication getLogserver() {
        return logserver;
    }

    @Override
    public String getPath() {
        return Strings.PATH_ROUTING;
    }

    @Override
    public String getName() {
        return logserver.getSystem();
    }

    public ListenerSettings getListener(WireType type) {
        for (ListenerSettings listener : transport) {
            if (listener.getType().equals(type)) {
                return listener;
            }
        }
        return null;
    }
}
