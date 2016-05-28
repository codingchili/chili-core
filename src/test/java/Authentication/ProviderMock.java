package Authentication;

import Authentication.Controller.ClientRequest;
import Authentication.Controller.PacketHandler;
import Authentication.Controller.Protocol;
import Authentication.Controller.RealmRequest;
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
    private Protocol<PacketHandler<ClientRequest>> clientProtocol = new Protocol<>();
    private Protocol<PacketHandler<RealmRequest>> realmProtocol = new Protocol<>();

    ProviderMock(Vertx vertx) {
        this.vertx = vertx;
        this.accounts = new AccountStoreMock();

    }

    @Override
    public AsyncAccountStore getAccountStore() {
        return accounts;
    }

    @Override
    public Protocol<PacketHandler<ClientRequest>> clientProtocol() {
        return clientProtocol;
    }

    @Override
    public Protocol<PacketHandler<RealmRequest>> realmProtocol() {
        return realmProtocol;
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
