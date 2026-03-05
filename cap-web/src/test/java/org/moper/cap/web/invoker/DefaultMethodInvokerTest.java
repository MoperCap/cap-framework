package org.moper.cap.web.invoker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moper.cap.web.binder.ParameterBinderRegistry;
import org.moper.cap.web.binder.ParameterMetadata;
import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.invoker.factory.MethodInvokerFactory;
import org.moper.cap.web.invoker.impl.DefaultMethodInvoker;
import org.moper.cap.web.router.RouteDefinition;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMethodInvokerTest {

    // Simple controller for testing
    static class TestController {
        public String noArgs() {
            return "hello";
        }

        public String withArgs(String name, int age) {
            return name + ":" + age;
        }

        public void throwsException() throws Exception {
            throw new IllegalStateException("controller error");
        }

        public String returnsNull() {
            return null;
        }
    }

    private TestController controller;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        controller = new TestController();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    // ---- constructor ----

    @Test
    void constructor_nullRegistry_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultMethodInvoker(null));
    }

    // ---- invoke: null checks ----

    @Test
    void invoke_nullMapping_throwsIllegalArgument() {
        MethodInvoker invoker = new DefaultMethodInvoker(emptyRegistry());
        assertThrows(IllegalArgumentException.class,
                () -> invoker.invoke(null, request, response, Map.of()));
    }

    @Test
    void invoke_nullRequest_throwsIllegalArgument() throws Exception {
        MethodInvoker invoker = new DefaultMethodInvoker(emptyRegistry());
        RouteDefinition mapping = noArgsRoute();
        assertThrows(IllegalArgumentException.class,
                () -> invoker.invoke(mapping, null, response, Map.of()));
    }

    @Test
    void invoke_nullResponse_throwsIllegalArgument() throws Exception {
        MethodInvoker invoker = new DefaultMethodInvoker(emptyRegistry());
        RouteDefinition mapping = noArgsRoute();
        assertThrows(IllegalArgumentException.class,
                () -> invoker.invoke(mapping, request, null, Map.of()));
    }

    // ---- invoke: no-args method ----

    @Test
    void invoke_noArgsMethod_returnsResult() throws Exception {
        MethodInvoker invoker = new DefaultMethodInvoker(emptyRegistry());
        RouteDefinition mapping = noArgsRoute();

        Object result = invoker.invoke(mapping, request, response, Map.of());

        assertEquals("hello", result);
    }

    @Test
    void invoke_noArgsMethod_nullPathVariables_usesEmptyMap() throws Exception {
        MethodInvoker invoker = new DefaultMethodInvoker(emptyRegistry());
        RouteDefinition mapping = noArgsRoute();

        Object result = invoker.invoke(mapping, request, response, null);

        assertEquals("hello", result);
    }

    // ---- invoke: with-args method ----

    @Test
    void invoke_withArgsMethod_bindsParametersAndReturnsResult() throws Exception {
        // Registry that returns "Alice" for first param and 30 for second
        ParameterBinderRegistry registry = (metadata, req, res, pathVars) -> {
            if (metadata.name().equals("name")) return "Alice";
            if (metadata.name().equals("age")) return 30;
            return null;
        };

        MethodInvoker invoker = new DefaultMethodInvoker(registry);
        RouteDefinition mapping = withArgsRoute();

        Object result = invoker.invoke(mapping, request, response, Map.of());

        assertEquals("Alice:30", result);
    }

    // ---- invoke: exception propagation ----

    @Test
    void invoke_controllerThrowsException_exceptionPropagated() throws NoSuchMethodException {
        MethodInvoker invoker = new DefaultMethodInvoker(emptyRegistry());
        RouteDefinition mapping = throwsExceptionRoute();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> invoker.invoke(mapping, request, response, Map.of()));

        assertEquals("controller error", ex.getMessage());
    }

    @Test
    void invoke_parameterBindingFails_wrapsAndThrows() throws NoSuchMethodException {
        ParameterBinderRegistry failingRegistry = (metadata, req, res, pathVars) -> {
            throw new RuntimeException("binding failed");
        };

        MethodInvoker invoker = new DefaultMethodInvoker(failingRegistry);
        RouteDefinition mapping = withArgsRoute();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> invoker.invoke(mapping, request, response, Map.of()));

        assertTrue(ex.getMessage().contains("Failed to bind parameter"));
    }

    // ---- invoke: path variables ----

    @Test
    void invoke_pathVariablesPassedToRegistry() throws Exception {
        Map<String, String> pathVars = Map.of("id", "42");

        ParameterBinderRegistry registry = (metadata, req, res, pv) -> {
            assertEquals(pathVars, pv);
            return "Alice";
        };

        // Use withArgs route but only resolve first param
        MethodInvoker invoker = new DefaultMethodInvoker((metadata, req, res, pv) -> {
            if (metadata.name().equals("name")) {
                assertEquals("42", pv.get("id"));
                return "Alice";
            }
            return 0;
        });

        RouteDefinition mapping = withArgsRoute();
        invoker.invoke(mapping, request, response, pathVars);
    }

    // ---- invoke: returns null ----

    @Test
    void invoke_methodReturnsNull_returnsNull() throws Exception {
        MethodInvoker invoker = new DefaultMethodInvoker(emptyRegistry());
        RouteDefinition mapping = returnsNullRoute();

        Object result = invoker.invoke(mapping, request, response, Map.of());

        assertNull(result);
    }

    // ---- MethodInvokerFactory ----

    @Test
    void factory_createDefault_returnsDefaultMethodInvoker() {
        MethodInvoker invoker = MethodInvokerFactory.createDefault(emptyRegistry());
        assertInstanceOf(DefaultMethodInvoker.class, invoker);
    }

    @Test
    void factory_createDefault_nullRegistry_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> MethodInvokerFactory.createDefault(null));
    }

    // ---- helpers ----

    private ParameterBinderRegistry emptyRegistry() {
        return (metadata, req, res, pathVars) -> null;
    }

    private RouteDefinition noArgsRoute() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("noArgs");
        return new RouteDefinition("/test", HttpMethod.GET, controller, method, Collections.emptyList());
    }

    private RouteDefinition withArgsRoute() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("withArgs", String.class, int.class);
        Parameter[] params = method.getParameters();

        List<ParameterMetadata> paramMetadata = List.of(
                new ParameterMetadata(params[0], "name", String.class),
                new ParameterMetadata(params[1], "age", int.class)
        );

        return new RouteDefinition("/test", HttpMethod.GET, controller, method, paramMetadata);
    }

    private RouteDefinition throwsExceptionRoute() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("throwsException");
        return new RouteDefinition("/test", HttpMethod.GET, controller, method, Collections.emptyList());
    }

    private RouteDefinition returnsNullRoute() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("returnsNull");
        return new RouteDefinition("/test", HttpMethod.GET, controller, method, Collections.emptyList());
    }

    // ---- minimal mock implementations ----

    static class MockHttpServletRequest implements HttpServletRequest {
        @Override public String getAuthType() { return null; }
        @Override public jakarta.servlet.http.Cookie[] getCookies() { return new jakarta.servlet.http.Cookie[0]; }
        @Override public long getDateHeader(String name) { return 0; }
        @Override public String getHeader(String name) { return null; }
        @Override public java.util.Enumeration<String> getHeaders(String name) { return Collections.emptyEnumeration(); }
        @Override public java.util.Enumeration<String> getHeaderNames() { return Collections.emptyEnumeration(); }
        @Override public int getIntHeader(String name) { return 0; }
        @Override public String getMethod() { return "GET"; }
        @Override public String getPathInfo() { return null; }
        @Override public String getPathTranslated() { return null; }
        @Override public String getContextPath() { return ""; }
        @Override public String getQueryString() { return null; }
        @Override public String getRemoteUser() { return null; }
        @Override public boolean isUserInRole(String role) { return false; }
        @Override public java.security.Principal getUserPrincipal() { return null; }
        @Override public String getRequestedSessionId() { return null; }
        @Override public String getRequestURI() { return "/test"; }
        @Override public StringBuffer getRequestURL() { return new StringBuffer("/test"); }
        @Override public String getServletPath() { return "/test"; }
        @Override public jakarta.servlet.http.HttpSession getSession(boolean create) { return null; }
        @Override public jakarta.servlet.http.HttpSession getSession() { return null; }
        @Override public String changeSessionId() { return null; }
        @Override public boolean isRequestedSessionIdValid() { return false; }
        @Override public boolean isRequestedSessionIdFromCookie() { return false; }
        @Override public boolean isRequestedSessionIdFromURL() { return false; }
        @Override public boolean authenticate(HttpServletResponse response) { return false; }
        @Override public void login(String username, String password) {}
        @Override public void logout() {}
        @Override public java.util.Collection<jakarta.servlet.http.Part> getParts() { return Collections.emptyList(); }
        @Override public jakarta.servlet.http.Part getPart(String name) { return null; }
        @Override public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public java.util.Enumeration<String> getAttributeNames() { return Collections.emptyEnumeration(); }
        @Override public String getCharacterEncoding() { return "UTF-8"; }
        @Override public void setCharacterEncoding(String env) {}
        @Override public int getContentLength() { return 0; }
        @Override public long getContentLengthLong() { return 0; }
        @Override public String getContentType() { return null; }
        @Override public jakarta.servlet.ServletInputStream getInputStream() { return null; }
        @Override public String getParameter(String name) { return null; }
        @Override public java.util.Enumeration<String> getParameterNames() { return Collections.emptyEnumeration(); }
        @Override public String[] getParameterValues(String name) { return new String[0]; }
        @Override public java.util.Map<String, String[]> getParameterMap() { return Collections.emptyMap(); }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public String getScheme() { return "http"; }
        @Override public String getServerName() { return "localhost"; }
        @Override public int getServerPort() { return 8080; }
        @Override public java.io.BufferedReader getReader() { return null; }
        @Override public String getRemoteAddr() { return "127.0.0.1"; }
        @Override public String getRemoteHost() { return "localhost"; }
        @Override public void setAttribute(String name, Object o) {}
        @Override public void removeAttribute(String name) {}
        @Override public java.util.Locale getLocale() { return java.util.Locale.getDefault(); }
        @Override public java.util.Enumeration<java.util.Locale> getLocales() { return Collections.enumeration(List.of(java.util.Locale.getDefault())); }
        @Override public boolean isSecure() { return false; }
        @Override public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) { return null; }
        @Override public int getRemotePort() { return 0; }
        @Override public String getLocalName() { return "localhost"; }
        @Override public String getLocalAddr() { return "127.0.0.1"; }
        @Override public int getLocalPort() { return 8080; }
        @Override public jakarta.servlet.ServletContext getServletContext() { return null; }
        @Override public jakarta.servlet.AsyncContext startAsync() { return null; }
        @Override public jakarta.servlet.AsyncContext startAsync(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) { return null; }
        @Override public boolean isAsyncStarted() { return false; }
        @Override public boolean isAsyncSupported() { return false; }
        @Override public jakarta.servlet.AsyncContext getAsyncContext() { return null; }
        @Override public jakarta.servlet.DispatcherType getDispatcherType() { return null; }
        @Override public String getRequestId() { return null; }
        @Override public String getProtocolRequestId() { return null; }
        @Override public jakarta.servlet.ServletConnection getServletConnection() { return null; }
    }

    static class MockHttpServletResponse implements HttpServletResponse {
        @Override public void addCookie(jakarta.servlet.http.Cookie cookie) {}
        @Override public boolean containsHeader(String name) { return false; }
        @Override public String encodeURL(String url) { return url; }
        @Override public String encodeRedirectURL(String url) { return url; }
        @Override public void sendError(int sc, String msg) {}
        @Override public void sendError(int sc) {}
        @Override public void sendRedirect(String location) {}
        @Override public void setDateHeader(String name, long date) {}
        @Override public void addDateHeader(String name, long date) {}
        @Override public void setHeader(String name, String value) {}
        @Override public void addHeader(String name, String value) {}
        @Override public void setIntHeader(String name, int value) {}
        @Override public void addIntHeader(String name, int value) {}
        @Override public void setStatus(int sc) {}
        @Override public int getStatus() { return 200; }
        @Override public String getHeader(String name) { return null; }
        @Override public java.util.Collection<String> getHeaders(String name) { return Collections.emptyList(); }
        @Override public java.util.Collection<String> getHeaderNames() { return Collections.emptyList(); }
        @Override public String getCharacterEncoding() { return "UTF-8"; }
        @Override public String getContentType() { return null; }
        @Override public jakarta.servlet.ServletOutputStream getOutputStream() { return null; }
        @Override public java.io.PrintWriter getWriter() { return null; }
        @Override public void setCharacterEncoding(String charset) {}
        @Override public void setContentLength(int len) {}
        @Override public void setContentLengthLong(long len) {}
        @Override public void setContentType(String type) {}
        @Override public void setBufferSize(int size) {}
        @Override public int getBufferSize() { return 0; }
        @Override public void flushBuffer() {}
        @Override public void resetBuffer() {}
        @Override public boolean isCommitted() { return false; }
        @Override public void reset() {}
        @Override public void setLocale(java.util.Locale loc) {}
        @Override public java.util.Locale getLocale() { return java.util.Locale.getDefault(); }
    }
}
