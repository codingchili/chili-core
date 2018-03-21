package com.codingchili.realm.configuration;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;

import static com.codingchili.realm.configuration.RealmServerSettings.PATH_REALMSERVER;

/**
 * Context for the service that deploys realms.
 */
public class RealmServerContext extends SystemContext implements ServiceContext {

    public RealmServerContext(CoreContext context) {
        super(context);
    }

    @Override
    public RealmServerSettings service() {
        return Configurations.get(PATH_REALMSERVER, RealmServerSettings.class);
    }
}
