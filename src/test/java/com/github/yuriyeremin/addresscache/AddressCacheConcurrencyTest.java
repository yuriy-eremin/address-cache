package com.github.yuriyeremin.addresscache;

import com.github.yuriyeremin.addresscache.util.AbstractAddressCacheTest;
import com.github.yuriyeremin.addresscache.util.Testing;
import net.jodah.concurrentunit.Waiter;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Concurrency operations stress test
 *
 * @author Yuriy Eremin
 */
public class AddressCacheConcurrencyTest extends AbstractAddressCacheTest {
    private static final Logger LOG = LoggerFactory.getLogger(AddressCacheConcurrencyTest.class);

    @Before
    public void setUp() {
        // intentionally small maxAge to be able observe expiration
        addressCache = new AddressCache(5, TimeUnit.MICROSECONDS);
    }

    /**
     * Should fail if a ConcurrentModificationException is thrown while performing add/remove operations.
     */
    @Test
    public void shouldSupportConcurrentAddRemove() throws Throwable {
        LOG.debug("Starting address cache concurrency stress test...");
        Waiter waiter = new Waiter();

        int threadCount = 50;
        Testing.threadedRun(threadCount, () -> {
            try {
                Random shouldAdd = new Random();
                Random index = new Random();
                for (int i = 0; i < 100 * 100; i++) {
                    InetAddress address = ADDRESSES[index.nextInt(ADDRESSES_COUNT)];
                    boolean result;
                    if (shouldAdd.nextBoolean()) {
                        result = addressCache.add(address);
                        LOG.debug("adding {} resulted in '{}'", address, result);
                    } else {
                        result = addressCache.remove(address);
                        LOG.debug("removing {} resulted in '{}'", address, result);
                    }
                }
            } catch (Exception e) {
                waiter.fail(e);
            }

            waiter.resume();
        });

        waiter.await(10000, threadCount);
        LOG.debug("Address cache concurrency stress test is finished");
    }
}