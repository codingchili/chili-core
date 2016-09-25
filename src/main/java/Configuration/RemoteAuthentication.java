package Configuration;

import Protocols.Authorization.Token;

import java.io.Serializable;

/**
 * @author Robin Duda
 *         contains settings required to authenticate with a remote component.
 */
public class RemoteAuthentication implements Serializable {
    private Token token = null;
    private String system;
    private String host;

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

    public Token getToken() {
        return token;
    }

    public RemoteAuthentication setToken(Token token) {
        this.token = token;
        return this;
    }
}
