package org.moper.cap.web;

import org.junit.jupiter.api.Test;
import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.handler.HandlerMapping;
import org.moper.cap.web.handler.parameter.ParameterMetadata;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpMethodAndHandlerMappingTest {

    // ──────────────── HttpMethod ────────────────

    @Test
    void fromString_convertsLowercase() {
        assertEquals(HttpMethod.GET, HttpMethod.fromString("get"));
        assertEquals(HttpMethod.POST, HttpMethod.fromString("post"));
        assertEquals(HttpMethod.DELETE, HttpMethod.fromString("delete"));
    }

    @Test
    void fromString_convertsUppercase() {
        for (HttpMethod method : HttpMethod.values()) {
            assertEquals(method, HttpMethod.fromString(method.name()));
        }
    }

    @Test
    void fromString_convertsMixedCase() {
        assertEquals(HttpMethod.PATCH, HttpMethod.fromString("Patch"));
        assertEquals(HttpMethod.OPTIONS, HttpMethod.fromString("Options"));
    }

    @Test
    void fromString_throwsOnUnknown() {
        assertThrows(IllegalArgumentException.class, () -> HttpMethod.fromString("UNKNOWN"));
    }

    @Test
    void fromString_throwsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> HttpMethod.fromString(null));
    }

    @Test
    void httpMethod_allNineValuesExist() {
        HttpMethod[] values = HttpMethod.values();
        assertEquals(9, values.length);
    }

    // ──────────────── HandlerMapping ────────────────

    private static Object dummyHandler() {
        return new Object();
    }

    private static Method dummyMethod() throws NoSuchMethodException {
        return Object.class.getMethod("toString");
    }

    @Test
    void handlerMapping_matchesExactPath() throws Exception {
        HandlerMapping mapping = new HandlerMapping(
                "/api/users", HttpMethod.GET, null, null,
                dummyHandler(), dummyMethod(), List.of(), List.of());

        assertTrue(mapping.matches("/api/users", HttpMethod.GET));
        assertFalse(mapping.matches("/api/users/42", HttpMethod.GET));
        assertFalse(mapping.matches("/api/users", HttpMethod.POST));
    }

    @Test
    void handlerMapping_matchesPathWithVariable() throws Exception {
        HandlerMapping mapping = new HandlerMapping(
                "/api/users/{id}", HttpMethod.GET, "application/json", null,
                dummyHandler(), dummyMethod(), List.of(), List.of("id"));

        assertTrue(mapping.matches("/api/users/42", HttpMethod.GET));
        assertTrue(mapping.matches("/api/users/abc", HttpMethod.GET));
        assertFalse(mapping.matches("/api/users", HttpMethod.GET));
        assertFalse(mapping.matches("/api/users/42", HttpMethod.POST));
    }

    @Test
    void handlerMapping_matchesPathWithMultipleVariables() throws Exception {
        HandlerMapping mapping = new HandlerMapping(
                "/api/{type}/{id}", HttpMethod.DELETE, null, null,
                dummyHandler(), dummyMethod(), List.of(), List.of("type", "id"));

        assertTrue(mapping.matches("/api/users/42", HttpMethod.DELETE));
        assertFalse(mapping.matches("/api/users/42/extra", HttpMethod.DELETE));
    }

    @Test
    void handlerMapping_extractPathVariables_singleVariable() throws Exception {
        HandlerMapping mapping = new HandlerMapping(
                "/api/users/{id}", HttpMethod.GET, null, null,
                dummyHandler(), dummyMethod(), List.of(), List.of("id"));

        Map<String, String> vars = mapping.extractPathVariables("/api/users/42");
        assertEquals(1, vars.size());
        assertEquals("42", vars.get("id"));
    }

    @Test
    void handlerMapping_extractPathVariables_multipleVariables() throws Exception {
        HandlerMapping mapping = new HandlerMapping(
                "/api/{type}/{id}", HttpMethod.GET, null, null,
                dummyHandler(), dummyMethod(), List.of(), List.of("type", "id"));

        Map<String, String> vars = mapping.extractPathVariables("/api/users/99");
        assertEquals(2, vars.size());
        assertEquals("users", vars.get("type"));
        assertEquals("99", vars.get("id"));
    }

    @Test
    void handlerMapping_extractPathVariables_noMatch_returnsEmpty() throws Exception {
        HandlerMapping mapping = new HandlerMapping(
                "/api/users/{id}", HttpMethod.GET, null, null,
                dummyHandler(), dummyMethod(), List.of(), List.of("id"));

        Map<String, String> vars = mapping.extractPathVariables("/other/path");
        assertTrue(vars.isEmpty());
    }

    @Test
    void handlerMapping_rejectsNullPath() throws Exception {
        assertThrows(IllegalArgumentException.class, () ->
                new HandlerMapping(null, HttpMethod.GET, null, null,
                        dummyHandler(), dummyMethod(), List.of(), List.of()));
    }

    @Test
    void handlerMapping_rejectsNullHttpMethod() throws Exception {
        assertThrows(IllegalArgumentException.class, () ->
                new HandlerMapping("/api", null, null, null,
                        dummyHandler(), dummyMethod(), List.of(), List.of()));
    }

    @Test
    void handlerMapping_matchesReturnsFalseOnNullArgs() throws Exception {
        HandlerMapping mapping = new HandlerMapping(
                "/api/users", HttpMethod.GET, null, null,
                dummyHandler(), dummyMethod(), List.of(), List.of());

        assertFalse(mapping.matches(null, HttpMethod.GET));
        assertFalse(mapping.matches("/api/users", null));
    }

    // ──────────────── ParameterMetadata ────────────────

    @Test
    void parameterMetadata_storesFields() throws Exception {
        Method method = String.class.getMethod("indexOf", String.class);
        Parameter param = method.getParameters()[0];

        ParameterMetadata meta = new ParameterMetadata(0, "str", String.class, param);

        assertEquals(0, meta.index());
        assertEquals("str", meta.name());
        assertEquals(String.class, meta.type());
        assertSame(param, meta.parameter());
    }

    @Test
    void parameterMetadata_rejectsNullParameter() {
        assertThrows(IllegalArgumentException.class, () ->
                new ParameterMetadata(0, "x", String.class, null));
    }
}
