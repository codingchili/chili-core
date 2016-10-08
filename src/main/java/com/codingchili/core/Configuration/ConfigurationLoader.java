package com.codingchili.core.Configuration;

import com.codingchili.core.Authentication.Configuration.AuthServerSettings;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Logging.Configuration.LogServerSettings;
import com.codingchili.core.Patching.Configuration.PatchServerSettings;
import com.codingchili.core.Routing.Configuration.RoutingSettings;
import com.codingchili.core.Website.Configuration.WebserverSettings;

/**
 * @author Robin Duda
 */
public interface ConfigurationLoader {
    VertxSettings getVertxSettings();

    PatchServerSettings getPatchServerSettings();

    RealmServerSettings getGameServerSettings();

    LogServerSettings getLogSettings();

    AuthServerSettings getAuthSettings();

    WebserverSettings getWebsiteSettings();

    RoutingSettings getRoutingSettings();
}
