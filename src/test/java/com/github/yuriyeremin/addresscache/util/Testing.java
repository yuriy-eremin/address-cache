package com.github.yuriyeremin.addresscache.util;

/**
 * @author Yuriy Eremin
 */
public final class Testing {
    /**
     * Runs the {@code runnable} across {@code threadCount} threads.
     */
    public static void threadedRun(int threadCount, Runnable runnable) {
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(runnable);
        }

        for (int i = 0; i < threadCount; i++) {
            threads[i].start();
        }
    }
}