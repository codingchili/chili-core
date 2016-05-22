import Configuration.AuthServerSettings;
import Configuration.RealmSettings;
import Protocol.RealmRegister;
import Utilities.Serializer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;

import java.io.IOException;

/**
 * @author Robin Duda
 *         tests the API from realm->authentication server.
 */
class RealmAuthenticationTest {

    static HttpClient registerRealm(AuthServerSettings authserver, RealmSettings realm, Future<Object> future) throws IOException {
        realm.load();

        return Vertx.vertx().createHttpClient().websocket(authserver.getRealmPort(), "localhost", "/", socket -> {
            socket.handler(data -> future.complete());
            socket.write(Buffer.buffer(Serializer.pack(new RealmRegister(realm))));
        });
    }
}
