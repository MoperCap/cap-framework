package org.moper.cap.common.exception;

import org.junit.jupiter.api.Test;
import org.moper.cap.common.priority.Priority;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionResolverRegistryTest {

    // ---- test exception types ----

    static class TestException extends Exception {
        public TestException(String msg) { super(msg); }
    }

    static class AnotherException extends Exception {
        public AnotherException(String msg) { super(msg); }
    }

    // ---- test handlers ----

    @Priority(100)
    static class TestExceptionHandler extends AbstractExceptionHandler<TestException> {
        final List<String> handled = new ArrayList<>();

        public TestExceptionHandler() { super(TestException.class); }

        @Override
        public void handle(TestException exception) {
            handled.add(exception.getMessage());
        }
    }

    @Priority(50)
    static class HighPriorityTestExceptionHandler extends AbstractExceptionHandler<TestException> {
        final List<String> handled = new ArrayList<>();

        public HighPriorityTestExceptionHandler() { super(TestException.class); }

        @Override
        public void handle(TestException exception) {
            handled.add("high:" + exception.getMessage());
        }
    }

    // ---- registry with programmatic registration ----

    /**
     * Test-only subclass that accepts pre-registered handlers without using ServiceLoader.
     */
    static class ProgrammaticRegistry extends ExceptionResolverRegistry {
        private final java.util.Map<Class<?>, ExceptionHandler<?>> testHandlers;

        ProgrammaticRegistry(ExceptionHandler<?>... handlers) {
            // the base constructor runs ServiceLoader (finds nothing in test classpath)
            testHandlers = new java.util.HashMap<>();
            for (ExceptionHandler<?> h : handlers) {
                Class<?> key = h.getExceptionType();
                ExceptionHandler<?> prev = testHandlers.get(key);
                if (prev == null) {
                    testHandlers.put(key, h);
                } else if (hasHigherPriority(h, prev)) {
                    testHandlers.put(key, h);
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E extends Throwable> void resolve(E exception) throws E {
            if (exception == null) return;
            ExceptionHandler<E> handler = (ExceptionHandler<E>) testHandlers.get(exception.getClass());
            if (handler != null) {
                handler.handle(exception);
            } else {
                throw exception;
            }
        }

        @Override
        public boolean hasHandler(Class<? extends Throwable> exceptionType) {
            return testHandlers.containsKey(exceptionType);
        }
    }

    // ---- tests ----

    @Test
    void testHandlerIsInvoked() throws Exception {
        TestExceptionHandler handler = new TestExceptionHandler();
        ProgrammaticRegistry registry = new ProgrammaticRegistry(handler);

        assertTrue(registry.hasHandler(TestException.class));

        registry.resolve(new TestException("hello"));
        assertEquals(List.of("hello"), handler.handled);
    }

    @Test
    void testUnhandledExceptionIsRethrown() {
        ProgrammaticRegistry registry = new ProgrammaticRegistry();

        assertFalse(registry.hasHandler(AnotherException.class));
        assertThrows(AnotherException.class, () -> registry.resolve(new AnotherException("rethrown")));
    }

    @Test
    void testHigherPriorityHandlerWins() throws Exception {
        TestExceptionHandler low = new TestExceptionHandler();             // priority 100
        HighPriorityTestExceptionHandler high = new HighPriorityTestExceptionHandler(); // priority 50

        ProgrammaticRegistry registry = new ProgrammaticRegistry(low, high);

        registry.resolve(new TestException("msg"));
        // high priority (lower value) should win
        assertEquals(List.of("high:msg"), high.handled);
        assertTrue(low.handled.isEmpty());
    }

    @Test
    void testResolveNullDoesNotThrow() throws Exception {
        ProgrammaticRegistry registry = new ProgrammaticRegistry();
        registry.resolve(null); // should not throw
    }

    @Test
    void testAbstractHandlerReturnsCorrectType() {
        TestExceptionHandler handler = new TestExceptionHandler();
        assertEquals(TestException.class, handler.getExceptionType());
    }

    @Test
    void testDefaultHandlersAreLoaded() {
        ExceptionResolverRegistry registry = new ExceptionResolverRegistry();
        // Default SPI handlers are loaded, so common types are covered
        assertTrue(registry.hasHandler(NullPointerException.class));
        // Recursive lookup: TestException -> Exception -> ExceptionHandler_Impl
        assertTrue(registry.hasHandler(TestException.class));
    }
}
