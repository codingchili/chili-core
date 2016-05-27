package Authentication;

import Authentication.Controller.Transport.ClientRestProtocol;
import Authentication.Controller.Transport.RealmWebsocketProtocol;
import Authentication.Model.AuthorizationHandler.Access;
import Authentication.Controller.*;
import Authentication.Model.AsyncAccountStore;
import Authentication.Model.Provider;
import Configuration.AuthServerSettings;
import Configuration.ConfigurationLoader;
import Utilities.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
class ProviderMock implements  Provider {
    private Vertx vertx;
    private AsyncAccountStore accounts;

    ProviderMock(Vertx vertx) {
        this.vertx = vertx;
        this.accounts = new AccountStoreMock();
    }

    @Override
    public AsyncAccountStore getAccountStore() {
        return accounts;
    }

    @Override
    public ClientProtocol clientProtocol(Access access) {
        return new ClientRestProtocol(this, access);
    }

    @Override
    public RealmProtocol realmProtocol(Access access) {
        return new RealmWebsocketProtocol(this, access);
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
