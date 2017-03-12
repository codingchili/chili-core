package com.codingchili.logging.configuration;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.*;

import static com.codingchili.logging.configuration.LogServerSettings.PATH_LOGSERVER;

/**
 * @author Robin Duda
 *         <p>
 *         Context used by logging handlers.
 */
public class LogContext extends ServiceContext {
    private AsyncStorage<JsonItem> storage;

    public LogContext(CoreContext context) {
        super(context);

        new StorageLoader<JsonItem>(context)
                .withPlugin(service().getPlugin())
                .withClass(JsonItem.class)
                .withDB(service().getDb())
                .withCollection(service().getCollection())
                .build(result -> storage = result.result());
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
        return new TokenFactory(service().getSecret()).verifyToken(token);
    }
}
