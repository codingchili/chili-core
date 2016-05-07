package Game.Controller;

import Game.Model.Connection;
import io.vertx.core.json.JsonObject;

/**
 * Created by Robin on 2016-05-07.
 */
public interface ClientPacketHandler {
    public void handle(Connection connection, String packet);
}
