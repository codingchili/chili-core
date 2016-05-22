package Authentication.Controller;

/**
 * @author Robin Duda
 */

interface ClientProtocol {
    ClientProtocol use(String action, ClientPacketHandler handler);

    String CHARACTERLIST = "character-list";
    String CHARACTERCREATE = "character-create";
    String CHARACTERREMOVE = "character-remove";
    String AUTHENTICATE = "authenticate";
    String REGISTER = "register";
    String REALMTOKEN = "realmtoken";
    String REALMLIST = "realmlist";
}
