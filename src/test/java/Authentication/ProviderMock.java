package Authentication;

import Authentication.Configuration.AuthProvider;
import Authentication.Configuration.AuthServerSettings;
import Authentication.Model.AsyncAccountStore;
import Configuration.ConfigMock;
import Configuration.ConfigurationLoader;
import Logging.LoggerMock;
import Logging.Model.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
class ProviderMock extends AuthProvider {
    private AsyncAccountStore accounts;

    ProviderMock(Vertx vertx) {
        super(vertx);
        this.accounts = super.getAccountStore();
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
    public ConfigurationLoader getConfig() {
        return new ConfigMock();
    }

    @Override
    public AuthServerSettings getAuthserverSettings() {
        return new ConfigMock().getAuthSettings();
    }
}
