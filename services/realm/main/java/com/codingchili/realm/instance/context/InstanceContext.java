package com.codingchili.realm.instance.context;

import com.codingchili.core.context.Delay;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.Token;
import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.configuration.RealmServerSettings;
import com.codingchili.realm.configuration.RealmSettings;
import io.vertx.core.Future;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 */
public class InstanceContext extends SystemContext implements ServiceContext {
    private static final String LOG_INSTANCE_SKIPTICKS = "skippedTicks";
    private static final String COUNT = "count";
    private Logger logger;
    private final String settings;
    private final RealmContext context;

    public InstanceContext(RealmContext context, InstanceSettings instance) {
        super(context);
        this.context = context;
        this.logger = context.logger(getClass())
            .setMetadata("instance", instance::getName);
        this.settings = instance.getPath();
    }

    public String address() {
        return settings().getName();
    }

    public InstanceSettings settings() {
        return Configurations.get(settings, InstanceSettings.class);
    }

    public RealmSettings realm() {
        return context.realm();
    }

    public RealmServerSettings service() {
        return context.service();
    }

    @Override
    public Logger logger(Class aClass) {
        return logger;
    }

    public boolean verifyToken(Token token) {
        return context.verifyToken(token);
    }

    public void onInstanceStarted(String realm, String instance) {
        logger.event(LOG_INSTANCE_START, Level.STARTUP)
                .put(LOG_INSTANCE, instance)
                .put(ID_REALM, realm).send();
    }

    public void onInstanceStopped(Future<Void> future, String realm, String instance) {
        logger.event(LOG_INSTANCE_STOP, Level.ERROR)
                .put(LOG_INSTANCE, instance)
                .put(ID_REALM, realm).send();

        Delay.forShutdown(future);
    }

    public void skippedTicks(int ticks) {
        logger.event(LOG_INSTANCE_SKIPTICKS, Level.WARNING)
                .put(COUNT, ticks);
    }
}
