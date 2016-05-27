package Authentication.Controller;

import Configuration.RealmSettings;

/**
 * @author Robin Duda
 */
public interface RealmRequest {

    RealmSettings realm();

    boolean authorized();

    void write(Object object);

    void error();

    String realmName();

    String sender();

    String account();

    String name();

    RealmConnection connection();
}
