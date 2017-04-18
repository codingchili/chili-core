package com.codingchili.realm.configuration;

import com.codingchili.core.context.*;
import com.codingchili.core.files.*;

import static com.codingchili.realm.configuration.RealmServerSettings.*;

/**
 * Context for the service that deploys realms.
 */
public class RealmServerContext extends ServiceContext {

    public RealmServerContext(CoreContext context) {
        super(context);
    }

    @Override
    public RealmServerSettings service() {
        return Configurations.get(PATH_REALMSERVER, RealmServerSettings.class);
    }
}
