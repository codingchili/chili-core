package Game.Model;

import Protocol.Game.AuthenticationResult;
import Protocol.Game.CharacterResponse;
import Utilities.Serializer;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;

/**
 * Created by Robin on 2016-05-07.
 */
public class Connection {
    private Vertx vertx;
    private ServerWebSocket connection;
    private String address;
    private Boolean authenticated = false;

    public void sendAuthenticationSuccess() {
        send(new AuthenticationResult(true));
    }

    public void sendAuthenticationError() {
        send(new AuthenticationResult(false));
    }

    public Connection(Vertx vertx, ServerWebSocket connection) {
        this.connection = connection;
        this.vertx = vertx;
        this.address = connection.textHandlerID();
    }

    public Boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    public ServerWebSocket getConnection() {
        return connection;
    }

    public String getAddress() {
        return address;
    }

    public void sendCharacterResponse(CharacterResponse response) {
        send(response);
    }

    private void send(Object message) {
        connection.writeFinalTextFrame(Serializer.pack(message));
    }
}
