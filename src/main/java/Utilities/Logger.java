package Utilities;

import Authentication.Model.Account;
import Game.Model.InstanceSettings;
import Game.Model.RealmSettings;

/**
 * Created by Robin on 2016-04-07.
 */
public interface Logger {

    void onServerStarted();

    void onServerStopped();

    void onInstanceStarted(InstanceSettings instance);

    void onRealmStarted(RealmSettings realm);

    void onAuthenticationFailure(Account account, String host);

    void onAuthenticated(Account account, String host);

    void onRegistered(Account account, String host);

    void onRealmRegistered(RealmSettings realm);

    void onRealmDeregistered(RealmSettings realm);

    void onRealmUpdated(RealmSettings realm);

    void onRealmRejected(RealmSettings realm);
}
