package com.codingchili.core.logging;

import io.vertx.core.json.JsonObject;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.codingchili.core.context.CoreContext;

import static com.codingchili.core.configuration.CoreStrings.*;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Implementation of a console logger, filters some key/value combinations to better display the messages.
 */
public class ConsoleLogger extends AbstractLogger implements StringLogger {
    private static final int FLUSH_TIMEOUT_MS = 16;
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private static final Set<String> filtered = new HashSet<>(Arrays.asList(
            ID_TOKEN, LOG_EVENT, LOG_APPLICATION, LOG_CONTEXT, LOG_HOST, LOG_VERSION
    ));

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName(getClass().getName());
            thread.setDaemon(true);
            return thread;
        }
    });

    @Override
    public void close() throws SecurityException {
        executor.shutdownNow().forEach(Runnable::run);
        try {
            // ensures that pending tasks are completed before any synchronous events
            // are submitted after the executor has shut down.
            Thread.sleep(FLUSH_TIMEOUT_MS);
        } catch (InterruptedException e) {
            onError(e);
        }
    }

    /**
     * Specifies its own class name as the logging class.
     * prefer {@link #ConsoleLogger(Class)}
     */
    public ConsoleLogger() {
        super(ConsoleLogger.class);
    }

    /**
     * @param aClass the class that uses this logger.
     */
    public ConsoleLogger(Class aClass) {
        this(null, aClass);
    }

    /**
     * @param context the context the logger is used on.
     * @param aClass  the class that uses this logger.
     */
    public ConsoleLogger(CoreContext context, Class aClass) {
        super(context, aClass);
        logger = this;
        AnsiConsole.systemInstall();
    }

    /**
     * @param enabled disable/enable output of the logger.
     * @return fluent
     */
    public Logger setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        return this;
    }

    private Consumer<JsonObject> log = (json) -> {
        write(parseJsonLog(json, consume(json, LOG_EVENT)));
    };

    /**
     * @return a set of keys that will not be printed to the console.
     */
    public Set<String> getFilteredKeys() {
        return filtered;
    }

    @Override
    public Logger log(JsonObject data) {
        if (enabled.get()) {
            if (executor.isShutdown()) {
                log.accept(data);
            } else {
                executor.submit(() -> log.accept(data));
            }
        }
        return this;
    }

    private void write(String line) {
        line = replaceTags(line, LOG_HIDDEN_TAGS);
        AnsiConsole.out().println(line);
        AnsiConsole.out().flush();
    }

    private static final int SPACES = 15;
    protected String parseJsonLog(JsonObject data, String event) {
        LogLevel level = consumeLevel(data);
        String message = consume(data, LOG_MESSAGE);

        Ansi ansi = ansi().reset();

        level.apply(ansi)
                .a(level.getName())
                .reset()
                .a("\t[")
                .fgBright(Ansi.Color.MAGENTA)
                .a(consumeTimestamp(data))
                .reset()
                .a("] ")
                .a((hasValue(event)) ? pad(event, SPACES) : "")
                .a(" [");

        level.apply(ansi)
                .a(pad(consume(data, LOG_SOURCE), SPACES))
                .reset()
                .a("]");

        if (hasValue(message)) {
            ansi.a(" ").a(message);
        }

        data.forEach(entry -> {
            if (entry.getValue() != null && !filtered.contains(entry.getKey())) {
                level.apply(ansi)
                        .a(" ")
                        .a(entry.getKey())
                        .reset()
                        .a("=")
                        .a(entry.getValue().toString());
            }
        });
        return ansi.reset().toString();
    }

    private static boolean hasValue(String text) {
        return (text != null && !text.equals(""));
    }

    protected LogLevel consumeLevel(JsonObject data) {
        String name = (String) data.remove(LOG_LEVEL);

        return Optional.ofNullable(LogLevel.registered.get(name))
                .orElseGet(() -> new LogLevel() {
                    {
                        // add to the registered to prevent creating new instances every time
                        // an unregistered logging level is used.
                        LogLevel.register(this);
                    }

                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public Ansi apply(Ansi ansi) {
                        // default to info color.
                        return Level.INFO.apply(ansi);
                    }

                    @Override
                    public int hashCode() {
                        return name.hashCode();
                    }
                });
    }

    private String consume(JsonObject data, String key) {
        if (data.containsKey(key)) {
            return (String) data.remove(key);
        } else {
            return "";
        }
    }

    private String consumeTimestamp(JsonObject data) {
        if (data.containsKey(LOG_TIME)) {
            return timestamp(Long.parseLong(data.remove(LOG_TIME).toString()));
        } else {
            return timestamp(Instant.now().toEpochMilli()) + "";
        }
    }

    private String compactPath(String path) {
        StringBuilder text = new StringBuilder();

        if (path != null) {
            String[] folders = path.split(DIR_SEPARATOR);

            for (int i = 0; i < folders.length; i++) {
                if (i == 0) {
                    text.append(folders[i]);

                    if (folders.length > 1) {
                        text.append(DIR_SEPARATOR);
                    }
                } else if (i == folders.length - 1) {
                    text.append(folders[i]);
                } else {
                    text.append(DIR_UP);
                }
            }
        }
        return text.toString();
    }
}
