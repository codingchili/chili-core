package com.codingchili.core.Realm;

import com.codingchili.core.Configuration.RemoteAuthentication;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.RealmProvider;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Model.PlayerClass;
import com.codingchili.core.Realm.Model.AsyncCharacterStore;
import com.codingchili.core.Realm.Model.HazelCharacterDB;
import com.codingchili.core.Shared.AsyncMapMock;

/**
 * @author Robin Duda
 */
public class ProviderMock extends RealmProvider {
    private RealmServerSettings server = new RealmServerSettings();
    private RealmSettings realm = new RealmSettings();
    private AsyncCharacterStore characters;

    public ProviderMock() {
        super();

        server = new RealmServerSettings();
        realm = new RealmSettings()
                .setAuthentication(new RemoteAuthentication()
                        .setToken(new Token(new TokenFactory("s".getBytes()), "realmName")))
                .setName("realmName");

        realm.getClasses().add(new PlayerClass().setName("class.name"));

        characters = new HazelCharacterDB(new AsyncMapMock<>());
    }

    @Override
    public AsyncCharacterStore getCharacterStore() {
        return characters;
    }

    @Override
    public RealmSettings getRealm() {
        return realm;
    }

    @Override
    public RealmServerSettings getServer() {
        return server;
    }

    public TokenFactory getClientFactory() {
        return new TokenFactory(realm.getAuthentication());
    }
}
