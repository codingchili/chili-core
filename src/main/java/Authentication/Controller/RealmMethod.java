package Authentication.Controller;

import io.vertx.core.json.JsonObject;

/**
 * Created by Robin on 2016-04-27.
 */
public interface RealmMethod {
    void handle(JsonObject data, String connectionId);
}
