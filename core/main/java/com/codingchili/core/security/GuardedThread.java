package com.codingchili.core.security;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents one instance of a thread that the guard is observing.
 */
public class GuardedThread {
    private static final StackWalker stack = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final Set<String> excludedFrames = new HashSet<>();
    private final List<StackWalker.StackFrame> frames;
    private final Thread thread;

    static {
        excludedFrames.add(ThreadGuard.class.getName());
        excludedFrames.add(GuardedThread.class.getName());
    }

    /**
     * Records the current thread and stacktrace when initialized.
     */
    public GuardedThread() {
        this.thread = Thread.currentThread();
        this.frames = stack.walk(stream -> stream
                // filter out guard internals to clean up the stack trace.
                .filter(frame -> !excludedFrames.contains(frame.getClassName()))
                .collect(Collectors.toList()));
    }

    /**
     * @return the recorded thread.
     */
    public Thread thread() {
        return thread;
    }

    /**
     * @return the recorded frames.
     */
    public List<StackWalker.StackFrame> frames() {
        return this.frames;
    }

    @Override
    public int hashCode() {
        return thread.getName().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof GuardedThread) {
            return ((GuardedThread) other).thread.getName().equals(thread.getName());
        } else {
            return false;
        }
    }

    public String getName() {
        return thread.getName();
    }
}
