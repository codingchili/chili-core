package com.codingchili.core.Protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Duda
 *
 * Contains listings for available routes and their respective
 * access level. Used to create a service description from a
 * protocol instance.
 */
class ProtocolMapping {
    private List<ProtocolEntry> routes = new ArrayList<>();

    public void add(String route, Access access) {
        routes.add(new ProtocolEntry(route, access));
    }

    public List<ProtocolEntry> getRoutes() {
        return routes;
    }

    public void setRoutes(List<ProtocolEntry> routes) {
        this.routes = routes;
    }

    public class ProtocolEntry {
        private String route;
        private Access access;

        ProtocolEntry(String route, Access access) {
            this.route = route;
            this.access = access;
        }

        public String getRoute() {
            return route;
        }

        public void setRoute(String route) {
            this.route = route;
        }

        public Access getAccess() {
            return access;
        }

        public void setAccess(Access access) {
            this.access = access;
        }
    }
}
