package com.codingchili.realm.instance.configuration;

import com.codingchili.core.context.Delay;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.security.Token;
import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.configuration.RealmServerSettings;
import com.codingchili.realm.configuration.RealmSettings;
import io.vertx.core.Future;

import static com.codingchili.common.Strings.*;

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
