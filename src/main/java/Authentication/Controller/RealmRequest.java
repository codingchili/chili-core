package Authentication.Controller;

import Authentication.Controller.Transport.RealmConnection;
import Configuration.RealmSettings;
import Protocol.RealmUpdate;
import Utilities.Token;

/**
 * @author Robin Duda
 */
public interface RealmRequest {

    RealmSettings realm();

    RealmUpdate update();

    boolean authorized();

    void write(Object object);

    void error();

    String realmName();

    String sender();

    RealmConnection connection();

    void accept();

    Token token();

    String account();

    String name();

    void unauthorized();
}
