package com.casino.common.lock;

import java.util.concurrent.locks.ReentrantLock;

public interface Lockable {
    void tryLock();

    void releaseLock();

    ReentrantLock getLock();

    void takeLock();
}