package Authentication.Controller;

import Routing.Controller.Transport.RealmConnection;
import Realm.Configuration.RealmSettings;
import Protocols.Request;
import Protocols.Util.Token;

/**
 * @author Robin Duda
 */
public interface RealmRequest extends Request {

    RealmSettings realm();

    int players();

    String realmName();

    String sender();

    RealmConnection connection();

    Token token();

    String account();

    String name();
}
