package Routing.Configuration;

import Configuration.Configurable;
import Configuration.RemoteAuthentication;
import Configuration.Strings;
import Routing.Model.WireListener;

import java.util.ArrayList;

/**
 * @author Robin Duda
 */
public class RoutingSettings implements Configurable {
    private RemoteAuthentication logserver;
    private ArrayList<WireListener> transport;

    public ArrayList<WireListener> getTransport() {
        return transport;
    }

    public void setTransport(ArrayList<WireListener> transport) {
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
}
