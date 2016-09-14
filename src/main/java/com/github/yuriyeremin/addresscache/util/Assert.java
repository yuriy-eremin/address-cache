package com.github.yuriyeremin.addresscache.util;

/**
 * @author Yuriy Eremin
 */
public final class Assert {
    private Assert() {
    }

    public static <T> void notNull(T reference, String parameterName) {
        if (reference == null) {
            throw new NullPointerException(parameterName + " cannot be null");
        }
    }

    public static void state(boolean expression, String errorMessageFormat, Object... args) {
        if (!expression) {
            throw new IllegalStateException(String.format(errorMessageFormat, args));
        }
    }
}
