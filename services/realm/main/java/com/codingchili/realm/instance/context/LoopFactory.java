package com.codingchili.realm.instance.context;

/**
 * @author Robin Duda
 */
public class LoopFactory implements Runnable {
    public final LockType lock;
    private Runnable runnable;

    private LoopFactory(Runnable runnable, LockType lock) {
        this.runnable = runnable;
        this.lock = lock;
    }

    public static LoopFactory writeLocked(Runnable runnable) {
        return new LoopFactory(runnable, LockType.WRITE);
    }

    public static LoopFactory readLocked(Runnable runnable) {
        return new LoopFactory(runnable, LockType.READ);
    }

    @Override
    public void run() {
        runnable.run();
    }

    enum LockType {READ, WRITE}
}
