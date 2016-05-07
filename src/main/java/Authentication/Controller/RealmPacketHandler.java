package Authentication.Controller;

import io.vertx.core.http.ServerWebSocket;

/**
 * Created by Robin on 2016-04-27.
 */
public interface RealmPacketHandler {
    void handle(ServerWebSocket connection, String data);
}
