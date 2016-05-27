package Authentication;

import Authentication.Controller.ClientPacketHandler;
import Authentication.Controller.ClientProtocol;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;

/**
 * @author Robin Duda
 */
public class ClientProtocolMock implements ClientProtocol {
    private HashMap<String, ClientPacketHandler> handlers = new HashMap<>();

    @Override
    public ClientProtocol use(String action, ClientPacketHandler handler) {
        handlers.put(action, handler);
        return this;
    }

    public void send(String action, ResponseListener listener, JsonObject data) {
        handlers.get(action).handle(new ClientRequestMock(data, listener));
    }
}
