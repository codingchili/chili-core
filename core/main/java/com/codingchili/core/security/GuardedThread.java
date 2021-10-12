package com.codingchili.core.security;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents one instance of a thread that the guard is observing.
 */
public class GuardedThread {
    private static final StackWalker stack = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private final List<StackWalker.StackFrame> frames;
    private final Thread thread;

    /**
     * Records the current thread and stacktrace when initialized.
     */
    public GuardedThread() {
        this.thread = Thread.currentThread();
        this.frames = stack.walk(stream -> stream.collect(Collectors.toList()));
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
        return frames;
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
}
