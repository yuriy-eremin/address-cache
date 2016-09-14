package com.github.yuriyeremin.addresscache;

/**
 * @author Yuriy Eremin
 */
/*package*/ interface Expirable<E> {
    E get();

    boolean isExpired();
}
