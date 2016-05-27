package Authentication;

import Authentication.Controller.ClientProtocol;
import Authentication.Model.AsyncAccountStore;
import Authentication.Model.Provider;
import Configuration.AuthServerSettings;
import Configuration.ConfigurationLoader;
import Utilities.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
class ProviderMock implements Provider {
    private Vertx vertx;
    private AsyncAccountStore accounts;
    private ClientProtocol protocol;

    ProviderMock(Vertx vertx) {
        this.vertx = vertx;
        this.accounts = new AccountStoreMock();
        this.protocol = new ClientProtocolMock();
    }

    @Override
    public AsyncAccountStore getAccountStore() {
        return accounts;
    }

    @Override
    public ClientProtocol clientProtocol() {
        return protocol;
    }

    @Override
    public Logger getLogger() {
        return new LoggerMock();
    }

    @Override
    public ConfigurationLoader getConfig() {
        return new ConfigMock();
    }

    @Override
    public AuthServerSettings getAuthserverSettings() {
        return new ConfigMock().getAuthSettings();
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }
}
