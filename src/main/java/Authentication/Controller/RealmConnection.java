package Authentication.Controller;

import Utilities.Serializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

/**
 * @author Robin Duda
 */
class RealmConnection {
    private ServerWebSocket socket;
    public final String realm;
    public final String id;

    RealmConnection(ServerWebSocket socket) {
        this.socket = socket;
        this.id = socket.textHandlerID();
        this.realm = null;
    }

    RealmConnection(RealmConnection connection, String realm) {
        this.socket = connection.socket;
        this.id = socket.textHandlerID();
        this.realm = realm;
    }

    public void write(Object object) {
        socket.write(Buffer.buffer(Serializer.pack(object)));
    }

    public String getId() {
        return socket.textHandlerID();
    }
}
