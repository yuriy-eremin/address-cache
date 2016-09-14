package com.github.yuriyeremin.addresscache;

import com.github.yuriyeremin.addresscache.util.Assert;

import java.net.InetAddress;
import java.util.Objects;

/**
 * Wrapper around InetAddress that implements Expirable for expiration mechanism in AddressCache
 * Immutable class for thread safety
 *
 * @author Yuriy Eremin
 */
/*package*/ final class ExpirableInetAddress implements Expirable<InetAddress> {
    private final InetAddress inetAddress;
    private final long expectedExpirationNanos;

    private ExpirableInetAddress(final InetAddress inetAddress, final long expectedExpirationNanos) {
        this.inetAddress = inetAddress;
        this.expectedExpirationNanos = expectedExpirationNanos;
    }

    public static ExpirableInetAddress from(final InetAddress inetAddress, final long expirationNanos) {
        Assert.notNull(inetAddress, "inetAddress");
        Assert.state(expirationNanos > 0, "expirationNanos cannot be negative");

        return new ExpirableInetAddress(inetAddress, System.nanoTime() + expirationNanos);
    }

    @Override
    public InetAddress get() {
        return inetAddress;
    }

    @Override
    public boolean isExpired() {
        return System.nanoTime() - expectedExpirationNanos > 0;
    }

    /**
     * expectedExpirationNanos doesn't matter for equals for correct usage in collections
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpirableInetAddress that = (ExpirableInetAddress) o;
        return Objects.equals(inetAddress, that.inetAddress);
    }

    /**
     * expectedExpirationNanos doesn't matter for hashCode for correct usage in collections
     */
    @Override
    public int hashCode() {
        return inetAddress.hashCode();
    }
}
