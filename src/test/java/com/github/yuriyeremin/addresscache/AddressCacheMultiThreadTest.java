package com.github.yuriyeremin.addresscache;

import com.github.yuriyeremin.addresscache.rules.Repeat;
import com.github.yuriyeremin.addresscache.util.AbstractAddressCacheTest;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.github.yuriyeremin.addresscache.AddressCacheMatchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests in multi threaded environment
 *
 * @author Yuriy Eremin
 */
public class AddressCacheMultiThreadTest extends AbstractAddressCacheTest {
    private static final Logger LOG = LoggerFactory.getLogger(AddressCacheMultiThreadTest.class);
    private static final int NUMBER_OF_THREADS = 2;

    private ExecutorService executorService;

    @Before
    public void setUp() {
        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        addressCache = new AddressCache(1, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        executorService.shutdown();
    }

    @Test
    @Repeat(times = 10)
    public void addAddsAddressesCorrectlyWhenMultiThreadsRun() throws InterruptedException {
        // given
        List<Callable<Void>> tasks = new ArrayList<>(NUMBER_OF_THREADS);

        tasks.add(() -> {
            addressCache.add(ADDRESSES[0]);
            addressCache.add(ADDRESSES[1]);
            addressCache.add(ADDRESSES[3]);

            return null;
        });

        tasks.add(() -> {
            addressCache.add(ADDRESSES[1]);
            addressCache.add(ADDRESSES[2]);

            TimeUnit.MILLISECONDS.sleep(100);

            addressCache.add(ADDRESSES[4]);
            addressCache.add(ADDRESSES[3]);

            return null;
        });

        // when
        executorService.invokeAll(tasks);

        // then
        assertThat(addressCache, hasSize(5));
    }

    @Test
    @Repeat(times = 10)
    public void removeRemovesAddressesCorrectlyWhenMultiThreadsRun() throws InterruptedException {
        // given
        addressCache.add(ADDRESSES[0]);
        addressCache.add(ADDRESSES[1]);
        addressCache.add(ADDRESSES[2]);
        addressCache.add(ADDRESSES[3]);
        addressCache.add(ADDRESSES[4]);

        List<Callable<Void>> tasks = new ArrayList<>(NUMBER_OF_THREADS);

        tasks.add(() -> {
            addressCache.remove(ADDRESSES[0]);
            addressCache.remove(ADDRESSES[1]);

            return null;
        });

        tasks.add(() -> {
            addressCache.remove(ADDRESSES[2]);

            TimeUnit.MILLISECONDS.sleep(100);

            addressCache.remove(ADDRESSES[1]);

            return null;
        });

        // when
        executorService.invokeAll(tasks);

        // then
        assertThat(addressCache, hasSize(2));
    }

    @Test
    @Repeat(times = 10)
    public void peekReturnsAddressesCorrectlyWhenMultiThreadsRun() throws InterruptedException {
        // given
        addressCache.add(ADDRESSES[0]);
        addressCache.add(ADDRESSES[1]);

        List<Callable<InetAddress>> tasks = new ArrayList<>(NUMBER_OF_THREADS);

        tasks.add(() -> addressCache.peek());

        tasks.add(() -> {
            TimeUnit.MILLISECONDS.sleep(100);

            return addressCache.peek();
        });

        // when
        final List<InetAddress> addresses = executorService.invokeAll(tasks).stream()
                .map(task -> {
                    try {
                        return task.get();
                    } catch (Exception ignore) {
                    }

                    return null;
                }).collect(Collectors.toList());

        // then
        assertThat(addresses, hasItem(ADDRESSES[1]));
        assertThat(addresses, Matchers.hasSize(2));
    }

    @Test
    @Repeat(times = 10)
    public void takeReturnsAddressCorrectlyWhenCacheIsEmptyAndWaitingAddFromAnotherThread() throws ExecutionException, InterruptedException {
        // given
        // empty cache

        // when
        final Future<InetAddress> address = executorService.submit(() -> {
            LOG.debug("Taking address...");
            return addressCache.take();
        });
        executorService.execute(() -> {
            LOG.debug("Adding address...");
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ignore) {
            }
            addressCache.add(ADDRESSES[0]);
        });

        // then
        assertThat(address.get(), is(ADDRESSES[0]));
    }
}
