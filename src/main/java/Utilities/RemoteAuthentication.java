package Utilities;

/**
 * Created by Robin on 2016-04-29.
 */
public class RemoteAuthentication {
    private String system;
    private String host;
    private String remote;
    private int port;
    private Token token;

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Token getToken() {
        return token;
    }

    public RemoteAuthentication setToken(Token token) {
        this.token = token;
        return this;
    }
}
