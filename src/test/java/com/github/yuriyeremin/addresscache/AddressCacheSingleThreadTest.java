package com.github.yuriyeremin.addresscache;

import com.github.yuriyeremin.addresscache.rules.Repeat;
import com.github.yuriyeremin.addresscache.util.AbstractAddressCacheTest;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static com.github.yuriyeremin.addresscache.AddressCacheMatchers.empty;
import static com.github.yuriyeremin.addresscache.AddressCacheMatchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unit tests in single threaded environment
 *
 * @author Yuriy Eremin
 */
public class AddressCacheSingleThreadTest extends AbstractAddressCacheTest {
    @Before
    public void setUp() {
        addressCache = new AddressCache(MILLISECONDS_EXPIRATION, TimeUnit.MILLISECONDS);
    }

    @Test
    public void addressCacheConstructorWillThrowIllegalStateExceptionWhenMaxAgeIsNegative() {
        // given

        // when
        IllegalStateException exception = null;
        try {
            addressCache = new AddressCache(-1, TimeUnit.MILLISECONDS);
        } catch (IllegalStateException e) {
            exception = e;
        }

        // then
        assertThat(exception, notNullValue());
    }

    @Test
    public void addressCacheConstructorWillThrowNullPointerExceptionWhenUnitIsNull() {
        // given

        // when
        NullPointerException exception = null;
        try {
            addressCache = new AddressCache(10, null);
        } catch (NullPointerException e) {
            exception = e;
        }

        // then
        assertThat(exception, notNullValue());
    }

    @Test
    @Repeat(times = 10)
    public void addReturnsTrueWhenAddressNotExistInCache() {
        // given
        addressCache.add(ADDRESSES[1]);

        // when
        final boolean result = addressCache.add(ADDRESSES[0]);

        // then
        assertThat(result, is(true));
        assertThat(addressCache, hasSize(2));
    }

    @Test
    @Repeat(times = 10)
    public void addReturnsFalseWhenAddressExistsInCache() {
        // given
        addressCache.add(ADDRESSES[0]);

        // when
        final boolean result = addressCache.add(ADDRESSES[0]);

        // then
        assertThat(result, is(false));
        assertThat(addressCache, hasSize(1));
    }

    @Test
    @Repeat(times = 10)
    public void addReturnsTrueWhenAddressWasAddedInCacheButAlreadyExpired() throws InterruptedException {
        // given
        addressCache.add(ADDRESSES[0]);
        TimeUnit.MILLISECONDS.sleep(MILLISECONDS_EXPIRED_DELAY);

        // when
        final boolean result = addressCache.add(ADDRESSES[0]);

        // then
        assertThat(result, is(true));
        assertThat(addressCache, hasSize(1));
    }

    @Test
    @Repeat(times = 10)
    public void removeReturnsTrueWhenAddressExistsInCache() {
        // given
        addressCache.add(ADDRESSES[1]);

        // when
        final boolean result = addressCache.remove(ADDRESSES[1]);

        // then
        assertThat(result, is(true));
        assertThat(addressCache, empty());
    }

    @Test
    @Repeat(times = 10)
    public void removeReturnsFalseWhenAddressNotExistInCache() {
        // given
        addressCache.add(ADDRESSES[1]);

        // when
        final boolean result = addressCache.remove(ADDRESSES[0]);

        // then
        assertThat(result, is(false));
        assertThat(addressCache, hasSize(1));
    }

    @Test
    @Repeat(times = 10)
    public void removeReturnsFalseWhenAddressWasAddedInCacheButAlreadyExpired() throws InterruptedException {
        // given
        addressCache.add(ADDRESSES[1]);
        TimeUnit.MILLISECONDS.sleep(MILLISECONDS_EXPIRED_DELAY);

        // when
        final boolean result = addressCache.remove(ADDRESSES[1]);

        // then
        assertThat(result, is(false));
        assertThat(addressCache, empty());
    }

    @Test
    @Repeat(times = 10)
    public void peekReturnsLastAddedAddressWhenSeveralAddressesWereAdded() {
        // given
        addressCache.add(ADDRESSES[0]);
        addressCache.add(ADDRESSES[1]);
        addressCache.add(ADDRESSES[2]);

        // when
        final InetAddress address = addressCache.peek();

        // then
        assertThat(address, is(ADDRESSES[2]));
        assertThat(addressCache, hasSize(3));
    }

    @Test
    @Repeat(times = 10)
    public void peekReturnsNullWhenNoAddressesExistInCache() {
        // given
        // empty cache

        // when
        final InetAddress address = addressCache.peek();

        // then
        assertThat(address, nullValue());
        assertThat(addressCache, empty());
    }

    @Test
    @Repeat(times = 10)
    public void peekReturnsNullWhenAllAddressesAreExpired() throws InterruptedException {
        // given
        addressCache.add(ADDRESSES[0]);
        addressCache.add(ADDRESSES[1]);
        addressCache.add(ADDRESSES[2]);
        TimeUnit.MILLISECONDS.sleep(MILLISECONDS_EXPIRED_DELAY);

        // when
        final InetAddress address = addressCache.peek();

        // then
        assertThat(address, nullValue());
        assertThat(addressCache, empty());
    }

    @Test
    @Repeat(times = 10)
    public void takeReturnsLastAddedAddressAndRemoveItFromCacheWhenSeveralAddressesAreAdded() {
        // given
        addressCache.add(ADDRESSES[0]);
        addressCache.add(ADDRESSES[1]);
        addressCache.add(ADDRESSES[2]);

        // when
        final InetAddress address = addressCache.take();

        // then
        assertThat(address, is(ADDRESSES[2]));
        assertThat(addressCache, hasSize(2));
    }
}
