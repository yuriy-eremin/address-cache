package com.github.yuriyeremin.addresscache;

import com.github.yuriyeremin.addresscache.rules.Repeat;
import com.github.yuriyeremin.addresscache.util.AbstractAddressCacheTest;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.github.yuriyeremin.addresscache.AddressCacheMatchers.empty;
import static com.github.yuriyeremin.addresscache.AddressCacheMatchers.hasAddress;
import static com.github.yuriyeremin.addresscache.AddressCacheMatchers.hasNotAddress;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Expiration behavior test
 *
 * @author Yuriy Eremin
 */
public class AddressCacheExpirationTest extends AbstractAddressCacheTest {
    @Before
    public void setUp() {
        addressCache = new AddressCache(100, TimeUnit.MILLISECONDS);
    }

    @Test
    @Repeat(times = 10)
    public void shouldExpireAddressesInOrder() throws Exception {
        // given
        // empty cache

        // When
        addressCache.add(ADDRESSES[0]);
        TimeUnit.MILLISECONDS.sleep(50);
        assertThat(addressCache, hasAddress(ADDRESSES[0]));

        addressCache.add(ADDRESSES[1]);
        TimeUnit.MILLISECONDS.sleep(70);
        assertThat(addressCache, hasNotAddress(ADDRESSES[0]));
        assertThat(addressCache, hasAddress(ADDRESSES[1]));

        addressCache.add(ADDRESSES[0]);
        TimeUnit.MILLISECONDS.sleep(50);
        assertThat(addressCache, hasNotAddress(ADDRESSES[1]));
        assertThat(addressCache, hasAddress(ADDRESSES[0]));

        TimeUnit.MILLISECONDS.sleep(60);

        // Then
        assertThat(addressCache, empty());
    }
}
