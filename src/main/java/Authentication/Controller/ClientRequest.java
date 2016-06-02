package Authentication.Controller;

import Authentication.Model.Account;
import Protocols.Authentication.ClientAuthentication;
import Protocols.Authentication.RealmMetaData;
import Protocols.Request;
import Utilities.Token;

/**
 * @author Robin Duda
 */


public interface ClientRequest extends Request {
    String realmName();

    String account();

    String character();

    String className();

    String sender();

    Token token();

    Account getAccount();

    void authenticate(ClientAuthentication authentication);

    public static final String CLOSE = "connection.close";
    public static final String CHARACTERLIST = "character-list";
    public static final String CHARACTERCREATE = "character-create";
    public static final String CHARACTERREMOVE = "character-remove";
    public static final String AUTHENTICATE = "authenticate";
    public static final String REGISTER = "register";
    public static final String REALMTOKEN = "realmtoken";
    public static final String REALMLIST = "realmlist";
}
