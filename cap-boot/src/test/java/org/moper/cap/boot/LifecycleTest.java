package org.moper.cap.boot;

import org.junit.jupiter.api.*;
import org.moper.cap.boot.context.DefaultBootstrapContext;
import org.moper.cap.boot.context.DefaultApplicationContextFactory;
import org.moper.cap.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleTest {
    public static class Config {}

    @Test
    void testRunAndCloseIdempotence() {
        ApplicationContext ctx = new DefaultBootstrapContext(Config.class).build(DefaultApplicationContextFactory.INSTANCE);
        assertDoesNotThrow(ctx::run);
        assertDoesNotThrow(ctx::run);    // 幂等
        assertDoesNotThrow(ctx::close);
        assertDoesNotThrow(ctx::close);  // 幂等
    }
}