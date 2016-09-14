package com.github.yuriyeremin.addresscache;

import com.github.yuriyeremin.addresscache.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * The AddressCache has a max age for the elements it's storing, an add method
 * for adding elements, a remove method for removing, a peek method which
 * returns the most recently added element, and a take method which removes
 * and returns the most recently added element.
 *
 * @author Yuriy Eremin
 */
public class AddressCache {
    private static final Logger LOG = LoggerFactory.getLogger(AddressCache.class);
    private static volatile ScheduledExecutorService EXPIRER;

    private final long maxAgeNanos;
    private final BlockingDeque<Expirable<InetAddress>> addressExpirableDeque;

    public AddressCache(long maxAge, TimeUnit unit) {
        Assert.state(maxAge > 0, "maxAge cannot be negative");
        Assert.notNull(unit, "unit");

        // static singleton lazy initialization
        if (EXPIRER == null) {
            synchronized (AddressCache.class) {
                if (EXPIRER == null) {
                    EXPIRER = Executors.newSingleThreadScheduledExecutor();
                }
            }
        }

        maxAgeNanos = unit.toNanos(maxAge);
        addressExpirableDeque = new LinkedBlockingDeque<>();
    }

    /**
     * add() method must store unique elements only (existing elements must be ignored).
     *
     * @param address that will be added to address cache
     * @return true if address was successfully added, false if address is already in cache
     */
    public synchronized boolean add(InetAddress address) {
        final Expirable<InetAddress> expirableAddress = ExpirableInetAddress.from(address, maxAgeNanos);

        if (!addressExpirableDeque.contains(expirableAddress)) {
            // Possible place for improvement of expiration mechanism
            // For simplicity of exercise schedule expiration when new address added to deque
            // with delay equivalents of address expiration
            scheduleExpiration(maxAgeNanos, TimeUnit.NANOSECONDS);

            return addressExpirableDeque.offerLast(expirableAddress);
        }

        return false;
    }

    /**
     * @param address for removal from cache
     * @return true if the address was successfully removed, otherwise returns false
     */
    public boolean remove(InetAddress address) {
        return addressExpirableDeque.remove(ExpirableInetAddress.from(address, maxAgeNanos));
    }

    /**
     * @return the most recently added element, null if no element exists.
     */
    public InetAddress peek() {
        return Optional.ofNullable(addressExpirableDeque.peekLast())
                .map(Expirable::get)
                .orElse(null);
    }

    /**
     * take() method retrieves and removes the most recently added element
     * from the cache and waits if necessary until an element becomes available.
     *
     * @return the most recently added element from cache
     */
    public InetAddress take() {
        try {
            return addressExpirableDeque.takeLast().get();
        } catch (InterruptedException e) {
            LOG.error("Error occurred while taking address from cache", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method with package access for testing purpose
     *
     * @return unmodifiable collection of elements in cache
     */
    /*package*/ Collection<Expirable<InetAddress>> elements() {
        return Collections.unmodifiableCollection(addressExpirableDeque);
    }

    /**
     * Schedule expiration of addresses
     * Since oldest addresses was inserted first in deque we can iterate them from head until they are expired
     */
    private void scheduleExpiration(long delay, TimeUnit timeUnit) {
        EXPIRER.schedule(() -> {
            synchronized (this) {
                while (Optional.ofNullable(addressExpirableDeque.peekFirst())
                        .map(Expirable::isExpired)
                        .orElse(false)) {
                    final Expirable<InetAddress> expiredAddress = addressExpirableDeque.removeFirst();
                    LOG.debug("{} is expired after {} nanoseconds", expiredAddress.get(), maxAgeNanos);
                }
            }
        }, delay, timeUnit);
    }
}
