package Authentication.Controller.Transport;

import Utilities.Serializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

/**
 * @author Robin Duda
 */
public class RealmConnection {
    private ServerWebSocket socket;
    private String realm;
    private Boolean authenticated;

    RealmConnection(ServerWebSocket socket) {
        this.socket = socket;
        this.realm = null;
        this.authenticated = false;
    }

    public void write(Object object) {
        socket.write(Buffer.buffer(Serializer.pack(object)));
    }

    public String id() {
        return socket.textHandlerID();
    }

    public ServerWebSocket socket() {
        return socket;
    }

    public String realm() {
        return realm;
    }

    public Boolean authenticated() {
        return authenticated;
    }

    public void authenticate(String realm) {
        this.authenticated = true;
        this.realm = realm;
    }
}
