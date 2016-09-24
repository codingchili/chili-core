package Routing.Configuration;

import Configuration.Configurable;
import Configuration.RemoteAuthentication;
import Configuration.Strings;
import Routing.Model.ListenerSettings;

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
}
