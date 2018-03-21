package com.codingchili.logging.configuration;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.AsyncStorage;
import com.codingchili.core.storage.JsonItem;
import com.codingchili.core.storage.StorageLoader;
import io.vertx.core.Future;

import static com.codingchili.logging.configuration.LogServerSettings.PATH_LOGSERVER;

/**
 * @author Robin Duda
 * <p>
 * Context used by logging handlers.
 */
public class LogContext extends SystemContext implements ServiceContext {
    private TokenFactory factory = new TokenFactory(service().getSecret());
    private AsyncStorage<JsonItem> storage;

    public LogContext(CoreContext context, Future<Void> future) {
        super(context);

        new StorageLoader<JsonItem>(context)
                .withPlugin(service().getPlugin())
                .withValue(JsonItem.class)
                .withDB(service().getDb())
                .withCollection(service().getCollection())
                .build(result -> {
                    storage = result.result();
                    future.complete();
                });
    }

    public AsyncStorage<JsonItem> storage() {
        return storage;
    }

    public LogServerSettings service() {
        return Configurations.get(PATH_LOGSERVER, LogServerSettings.class);
    }

    public boolean consoleEnabled() {
        return service().getConsole();
    }

    public boolean verifyToken(Token token) {
        return factory.verifyToken(token);
    }
}
