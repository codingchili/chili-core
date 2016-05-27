package Authentication.Controller;

import Authentication.Model.AuthorizationHandler.Access;

/**
 * @author Robin Duda
 */
public interface RealmProtocol {
    RealmProtocol use(String action, RealmPacketHandler handler);

    RealmProtocol use(String action, RealmPacketHandler handler, Access access);

    void handle(String action, RealmRequest request);

    String AUTHENTICATE = "realm.register";
    String CLOSE = "connection.close";
}