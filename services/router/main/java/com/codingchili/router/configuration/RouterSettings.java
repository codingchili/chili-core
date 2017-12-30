package com.codingchili.router.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.WireType;

import static com.codingchili.core.configuration.CoreStrings.getService;

/**
 * @author Robin Duda
 * <p>
 * Settings for the router identity.
 */
public class RouterSettings extends ServiceConfigurable {
    public static final String PATH_ROUTING = getService("routingserver");
    private List<ListenerSettings> transport = new ArrayList<>();
    private Map<String, String> external = new HashMap<>();

    public RouterSettings() {
    }

    public RouterSettings(String address) {
        super.setNode(address);
    }

    /**
     * @return a list of configured transports.
     */
    public List<ListenerSettings> getTransport() {
        return transport;
    }

    /**
     * @param transport sets a list of configured transports.
     */
    public void setTransport(List<ListenerSettings> transport) {
        this.transport = transport;
    }

    /**
     * Adds a new transport configuration to the list of existing.
     *
     * @param listener the listener to add
     * @return fluent
     */
    public RouterSettings addTransport(ListenerSettings listener) {
        transport.add(listener);
        return this;
    }

    /**
     * @return returns a set of hidden nodes, the router must not route
     * messages to these nodes.
     */
    public Map<String, String> getExternal() {
        return external;
    }

    public void setHidden(Map<String, String> external) {
        this.external = external;
    }

    /**
     * Get configuration for the specified listener.
     *
     * @param type the type of transport to get configuration for.
     * @return the listener setting.
     */
    public ListenerSettings getListener(WireType type) {
        for (ListenerSettings listener : transport) {
            if (listener.getType().equals(type)) {
                return listener;
            }
        }
        throw new RuntimeException("No listener configured for " + type.name());
    }

    /**
     * Sets a route to hidden in the router. Any requests for this route will return
     * with an error and the unauthorized code.
     *
     * @param target     the route to 'hide' requests for
     * @param routeRegex the regex to match routes against.
     * @return fluent
     */
    public RouterSettings addExternal(String target, String routeRegex) {
        if (external.containsKey(target)) {
            throw new IllegalArgumentException(String.format("External target '%s' is already added.", target));
        }
        external.put(target, routeRegex);
        return this;
    }

    @JsonIgnore
    public boolean isExternal(String target, String route) {
        target = target.toLowerCase();
        route = route.toLowerCase();
        return external.containsKey(target) && route.matches(external.get(target));
    }
}
