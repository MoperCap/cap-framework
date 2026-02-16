package org.moper.cap.core.context;

public non-sealed interface ResourceContext extends Context, AutoCloseable {

    @Override
    default void close() {
        // do nothing
    }
}
