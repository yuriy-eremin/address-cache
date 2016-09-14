package com.github.yuriyeremin.addresscache.util;

import com.github.yuriyeremin.addresscache.AddressCache;
import com.github.yuriyeremin.addresscache.rules.RepeatRule;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Helper class for AddressCache testing with initialized InetAddresses
 *
 * @author Yuriy Eremin
 */
public abstract class AbstractAddressCacheTest {
    protected static final int ADDRESSES_COUNT = 10;
    protected static InetAddress[] ADDRESSES;
    protected static final int MILLISECONDS_EXPIRATION = 500;
    protected static final int MILLISECONDS_EXPIRED_DELAY = 600;
    private static final String ADDRESSES_FORMAT = "192.168.1.%d";

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    protected AddressCache addressCache;

    @BeforeClass
    public static void setUpBeforeClass() throws UnknownHostException {
        ADDRESSES = new InetAddress[ADDRESSES_COUNT];

        for (int i = 0; i < ADDRESSES_COUNT; i++) {
            ADDRESSES[i] = InetAddress.getByName(String.format(ADDRESSES_FORMAT, i + 1));
        }
    }

}
