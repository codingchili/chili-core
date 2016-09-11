package Authentication;

import Authentication.Configuration.AuthProvider;
import Authentication.Configuration.AuthServerSettings;
import Authentication.Model.AsyncAccountStore;
import Authentication.Model.AsyncRealmStore;
import Configuration.ConfigMock;
import Logging.LoggerMock;
import Logging.Model.Logger;

/**
 * @author Robin Duda
 */
public class ProviderMock extends AuthProvider {
    private AsyncRealmStore realms = new RealmStoreMock();
    private AsyncAccountStore accounts = new AccountStoreMock();

    public ProviderMock() {
        super();
    }

    @Override
    public AsyncRealmStore getRealmStore() {
        return realms;
    }

    @Override
    public AsyncAccountStore getAccountStore() {
        return accounts;
    }

    @Override
    public Logger getLogger() {
        return new LoggerMock();
    }

    @Override
    public AuthServerSettings getAuthserverSettings() {
        return new ConfigMock().getAuthSettings();
    }
}
