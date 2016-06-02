package Game.Model;

import Protocols.Game.AuthenticationResult;
import Protocols.Game.CharacterResponse;
import Protocols.Serializer;
import io.vertx.core.http.ServerWebSocket;

/**
 * @author Robin Duda
 *         Provides helper methods for writing and identifying websockets.
 */
public class Connection {
    private ServerWebSocket connection;
    private String address;
    private Boolean authenticated = false;

    public void sendAuthenticationSuccess() {
        send(new AuthenticationResult(true));
    }

    public void sendAuthenticationError() {
        send(new AuthenticationResult(false));
    }

    public Connection(ServerWebSocket connection) {
        this.connection = connection;
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
