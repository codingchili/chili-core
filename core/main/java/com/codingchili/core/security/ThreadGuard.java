package com.codingchili.core.security;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.codingchili.core.security.exception.ThreadGuardException;

/**
 * Utility class that can be used to guard an object from being accessed concurrently
 * or accessed from different contexts/threads. The thread guard operates in two exclusive
 * modes. The guard may be applied to multiple code blocks, in separate methods and classes.
 * <p>
 * concurrency, invokes the error handler if multiple threads is accessing
 * a guarded block of code concurrently. Separate threads may access the guarded
 * blocks at different times.
 * <p>
 * watcher, invokes the error handler when limit amount of threads has been seen during
 * the guards lifetime. A single thread may access the guarded object at any time.
 * <p>
 * If no handlers are set then the ThreadGuard will throw a ThreadGuardException.
 * The guard must be reset after it has been triggered.
 * <p>
 * Note that the guard incurs a performance overhead and should only be
 * used for development/debugging purposes as each guarded block captures the
 * current stack trace to get an accurate trace.
 */
public class ThreadGuard {
    private static final Logger logger = Logger.getLogger(ThreadGuard.class.getName());
    private final Set<GuardedThread> threads = new HashSet<>();
    private final Set<Consumer<Set<GuardedThread>>> handlers = new HashSet<>();
    private final GuardMode mode;
    private final int limit;
    private Boolean active = true;

    /**
     * @return a thread guard that guards against concurrent access.
     */
    public static ThreadGuard concurrency() {
        return new ThreadGuard(GuardMode.concurrency, 1);
    }

    /**
     * @return a thread guard that watches for limit number of different threads.
     * @param limit the maximum number of threads observed before guard is considered violated.
     */
    public static ThreadGuard watcher(int limit) {
        return new ThreadGuard(GuardMode.watcher, limit);
    }

    /**
     * Convenience method for logging a thread vilation error.
     *
     * @param threads a set of threads reported by the guard.
     */
    public static void log(Set<GuardedThread> threads) {
        logger.severe(format(threads));
    }

    public static String format(Set<GuardedThread> threads) {
        return String.format("Multiple threads (%d) accessed guarded block\n%s",
                threads.size(),
                threads.stream().map(item -> String.format("\n == == == == == \nthread - %s - group %s\n",
                                item.thread().getName(),
                                item.thread().getThreadGroup().getName()
                        ) + item.frames().stream()
                                .map(frame -> "\t" + frame.toString())
                                .collect(Collectors.joining("\n")))
                        .collect(Collectors.joining("\n")));

    }

    /**
     * @param block executes the given code block under the guard.
     */
    public void protect(Runnable block) {
        enter(thread -> {
            block.run();
            leave(thread);
        });
    }

    /**
     * Resets the error status and starts recording threads again.
     */
    public void reset() {
        synchronized (this) {
            threads.clear();
            active = true;
        }
    }

    /**
     * @param handler invoked when the guard is violated.
     * @return fluent.
     */
    public ThreadGuard error(Consumer<Set<GuardedThread>> handler) {
        synchronized (this) {
            this.handlers.add(handler);
        }
        return this;
    }

    private ThreadGuard(GuardMode mode, int limit) {
        this.limit = limit;
        this.mode = mode;
    }

    private void enter(Consumer<GuardedThread> consumer) {
        var thread = new GuardedThread();
        synchronized (this) {
            if (active) {
                threads.add(thread);
            }
        }
        consumer.accept(thread);
    }

    private void leave(GuardedThread thread) {
        synchronized (this) {
            if (active) {
                if (threads.size() > limit) {
                    active = false;

                    if (handlers.size() > 0) {
                        handlers.forEach(handler -> handler.accept(threads));
                    } else {
                        throw new ThreadGuardException(threads, mode);
                    }
                }
                if (GuardMode.concurrency.equals(mode)) {
                    threads.remove(thread);
                }
            }
        }
    }
}
