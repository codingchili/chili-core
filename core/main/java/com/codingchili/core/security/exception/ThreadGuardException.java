package com.codingchili.core.security.exception;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingchili.core.security.*;

/**
 * Thrown by the thread guard when the guard has been violated.
 */
public final class ThreadGuardException extends RuntimeException {
    private final Set<GuardedThread> threads = new HashSet<>();

    /**
     * @param threads a set of threads that has violated the guard.
     * @param mode the mode that the thread guard was operating in.
     */
    public ThreadGuardException(Set<GuardedThread> threads, GuardMode mode) {
        super(String.format("Multiple threads (%d) accessed %s guarded block %s.",
                threads.size(),
                mode.name(),
                threads.stream().map(thread -> thread.thread().getName()).collect(Collectors.toList()))
        );
        this.threads.addAll(threads);
    }

    /**
     * @return a set of threads that has violated the guard.
     */
    public Set<GuardedThread> threads() {
        return threads;
    }
}
