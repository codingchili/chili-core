package Authentication.Controller;

import Authentication.Controller.Transport.RealmConnection;
import Configuration.Gameserver.RealmSettings;
import Protocols.Authentication.RealmUpdate;
import Protocols.Request;
import Utilities.Token;

/**
 * @author Robin Duda
 */
public interface RealmRequest extends Request {

    RealmSettings realm();

    RealmUpdate update();

    String realmName();

    String sender();

    RealmConnection connection();

    Token token();

    String account();

    String name();
}
