package com.codingchili.services.Realm.Instance.Configuration;

import io.vertx.core.Future;

import com.codingchili.core.Context.Delay;
import com.codingchili.core.Context.ServiceContext;
import com.codingchili.core.Files.Configurations;
import com.codingchili.core.Logging.Level;
import com.codingchili.core.Security.Token;

import com.codingchili.services.Realm.Configuration.*;

import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 */
public class InstanceContext extends ServiceContext {
    private final String settings;
    private final RealmContext context;

    public InstanceContext(RealmContext context, InstanceSettings instance) {
        super(context);
        this.context = context;
        this.settings = instance.getPath();
    }

    public String address() {
        return instance().getName() + "." + context.address();
    }

    public InstanceSettings instance() {
        return Configurations.get(settings, InstanceSettings.class);
    }

    public RealmSettings realm() {
        return context.realm();
    }

    public RealmServerSettings service() {
        return context.service();
    }

    public boolean verifyToken(Token token) {
        return context.verifyToken(token);
    }

    public void onInstanceStarted(String realm, String instance) {
        log(event(LOG_INSTANCE_START, Level.STARTUP)
                .put(LOG_INSTANCE, instance)
                .put(ID_REALM, realm));
    }

    public void onInstanceStopped(Future<Void> future, String realm, String instance) {
        log(event(LOG_INSTANCE_STOP, Level.SEVERE)
                .put(LOG_INSTANCE, instance)
                .put(ID_REALM, realm));

        Delay.forShutdown(future);
    }
}
