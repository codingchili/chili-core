package com.codingchili.router.configuration;

import static com.codingchili.core.configuration.CoreStrings.getService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingchili.core.configuration.ServiceConfigurable;

import com.codingchili.router.model.WireType;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Robin Duda
 *         <p>
 *         Settings for the router identity.
 */
public class RouterSettings extends ServiceConfigurable {
    public static final String PATH_ROUTING = getService("routingserver");
    private List<ListenerSettings> transport = new ArrayList<>();
    private Set<String> hidden = new HashSet<>();

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
    public Set<String> getHidden() {
        return hidden;
    }

    public void setHidden(Set<String> hidden) {
        this.hidden = hidden.stream().map(String::toLowerCase).collect(Collectors.toSet());
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
     * @param route the route to 'hide' requests for
     * @return fluent
     */
    public RouterSettings addHidden(String route) {
        hidden.add(route.toLowerCase());
        return this;
    }

    @JsonIgnore
    public boolean isHidden(String target) {
        return hidden.contains(target.toLowerCase());
    }
}
