package io.github.rainyaphthyl.elytradashboard.core.record;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockRunner {
    private final ReadWriteLock mainLock;

    public LockRunner(boolean fair) {
        mainLock = new ReentrantReadWriteLock(fair);
    }

    /**
     * Lock the read-lock or the write-lock, run the task, and unlock the lock.
     *
     * @param type   {@link EnumRW#READ} or {@link EnumRW#WRITE}
     * @param runner The task
     */
    public void runSyncTask(EnumRW type, Runnable runner) {
        if (runner == null) return;
        Lock lock;
        switch (type) {
            case READ:
                lock = mainLock.readLock();
                break;
            case WRITE:
                lock = mainLock.writeLock();
                break;
            default:
                return;
        }
        lock.lock();
        try {
            runner.run();
        } finally {
            lock.unlock();
        }
    }
}
