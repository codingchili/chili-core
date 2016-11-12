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

    public void add(String action, Access access) {
        routes.add(new ProtocolEntry(action, access));
    }

    public List<ProtocolEntry> getRoutes() {
        return routes;
    }

    public void setRoutes(List<ProtocolEntry> routes) {
        this.routes = routes;
    }

    public class ProtocolEntry {
        private String action;
        private Access access;

        ProtocolEntry(String action, Access access) {
            this.action = action;
            this.access = access;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Access getAccess() {
            return access;
        }

        public void setAccess(Access access) {
            this.access = access;
        }
    }
}
