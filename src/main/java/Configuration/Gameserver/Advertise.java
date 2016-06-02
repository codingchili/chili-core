package Configuration.Gameserver;

/**
 * @author Robin Duda
 *         Provides configuration options to allow a server to listen on another port
 *         that it is broadcasting on the realmName-list.
 */
public class Advertise {
    private String remote;
    private Integer proxy;

    public Advertise() {
    }

    public Advertise(String remote, int port) {
        this.remote = remote;
        this.proxy = port;
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
}
