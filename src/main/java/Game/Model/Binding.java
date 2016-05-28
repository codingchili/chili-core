package Game.Model;

/**
 * @author Robin Duda
 *         Provides configuration options to allow a server to listen on another port
 *         that it is broadcasting on the realmName-list.
 */
public class Binding {
    private String remote;
    private Integer proxy;
    private Integer port;

    public Binding() {}

    public Binding(String remote, int port, int proxy) {
        this.remote = remote;
        this.port = port;
        this.proxy = proxy;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public Integer getProxy() {
        return proxy;
    }

    public void setProxy(Integer proxy) {
        this.proxy = proxy;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
