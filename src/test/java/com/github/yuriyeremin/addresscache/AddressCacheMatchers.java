package com.github.yuriyeremin.addresscache;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom Hamcrest matchers
 *
 * @author Yuriy Eremin
 */
/*package*/ final class AddressCacheMatchers {
    private AddressCacheMatchers() {

    }

    public static Matcher<AddressCache> hasAddress(final InetAddress address) {
        return new TypeSafeMatcher<AddressCache>() {
            @Override
            protected boolean matchesSafely(AddressCache addressCache) {
                final List<InetAddress> addresses = addressCache.elements().stream()
                        .map(Expirable::get)
                        .collect(Collectors.toList());
                return addresses.contains(address);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("AddressCache with address=")
                        .appendValue(address);
            }
        };
    }

    public static Matcher<AddressCache> hasNotAddress(final InetAddress address) {
        return new TypeSafeMatcher<AddressCache>() {
            @Override
            protected boolean matchesSafely(AddressCache addressCache) {
                final List<InetAddress> addresses = addressCache.elements().stream()
                        .map(Expirable::get)
                        .collect(Collectors.toList());
                return !addresses.contains(address);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("AddressCache without address=")
                        .appendValue(address);
            }
        };
    }

    public static Matcher<AddressCache> hasSize(final int expectedSize) {
        return new TypeSafeMatcher<AddressCache>() {
            @Override
            protected boolean matchesSafely(AddressCache addressCache) {
                return addressCache.elements().size() == expectedSize;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("AddressCache of size=")
                        .appendValue(expectedSize);
            }
        };
    }

    public static Matcher<AddressCache> empty() {
        return new TypeSafeMatcher<AddressCache>() {
            @Override
            protected boolean matchesSafely(AddressCache addressCache) {
                return addressCache.elements().isEmpty();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("AddressCache is empty");
            }
        };
    }
}
