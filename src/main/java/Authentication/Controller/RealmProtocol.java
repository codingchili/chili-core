package Authentication.Controller;

/**
 * @author Robin Duda
 */
public interface RealmProtocol {
    RealmProtocol use(String action, RealmPacketHandler handler);

    RealmProtocol use(String action, RealmPacketHandler handler, Access access);

    String AUTHENTICATE = "realm.register";
    String CLOSE = "connection.close";
}