package Authentication;

import io.vertx.core.json.JsonObject;
import Authentication.ClientRequestMock.ResponseStatus;

/**
 * @author Robin Duda
 */

@FunctionalInterface
public interface ResponseListener {
    void handle(JsonObject response, ResponseStatus status);
}
