package com.codingchili.core.Routing.Controller.Transport;

import com.codingchili.core.Protocols.Util.Serializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
public class RealmConnection {
    private ServerWebSocket socket;
    private String realm;
    private Boolean authenticated = false;

    RealmConnection(ServerWebSocket socket) {
        this.socket = socket;
        this.realm = null;
        this.authenticated = false;
    }

    public void write(Object object) {
        socket.write(Buffer.buffer(Serializer.pack(object)));
    }

    public void write(JsonObject json) {
        socket.write(Buffer.buffer(json.encode()));
    }

    public String id() {
        return socket.textHandlerID();
    }

    ServerWebSocket socket() {
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