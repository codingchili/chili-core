package Authentication.Controller;

import Authentication.Model.AuthorizationHandler.Access;

/**
 * @author Robin Duda
 */

public interface ClientProtocol {
    ClientProtocol use(String action, ClientPacketHandler handler);

    ClientProtocol use(String action, ClientPacketHandler handler, Access access);

    void handle(String action, ClientRequest request);

    String CHARACTERLIST = "character-list";
    String CHARACTERCREATE = "character-create";
    String CHARACTERREMOVE = "character-remove";
    String AUTHENTICATE = "authenticate";
    String REGISTER = "register";
    String REALMTOKEN = "realmtoken";
    String REALMLIST = "realmlist";
}
