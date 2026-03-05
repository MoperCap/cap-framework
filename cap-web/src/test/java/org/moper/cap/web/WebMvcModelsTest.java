package org.moper.cap.web;

import org.junit.jupiter.api.Test;
import org.moper.cap.web.model.ErrorResponse;
import org.moper.cap.web.exception.ExceptionHandlerInfo;
import org.moper.cap.web.result.ResponseEntity;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for data models and ResponseEntity factory methods.
 */
class WebMvcModelsTest {

    // ──────────────── ResponseEntity ────────────────

    @Test
    void responseEntity_ok_withBody() {
        ResponseEntity<String> entity = ResponseEntity.ok("hello");
        assertEquals(200, entity.status());
        assertEquals("hello", entity.body());
        assertTrue(entity.headers().isEmpty());
    }

    @Test
    void responseEntity_ok_noBody() {
        ResponseEntity<Void> entity = ResponseEntity.ok();
        assertEquals(200, entity.status());
        assertNull(entity.body());
    }

    @Test
    void responseEntity_created_withLocation() {
        ResponseEntity<String> entity = ResponseEntity.created("data", "/api/items/1");
        assertEquals(201, entity.status());
        assertEquals("data", entity.body());
        assertEquals("/api/items/1", entity.headers().get("Location"));
    }

    @Test
    void responseEntity_notFound() {
        ResponseEntity<Object> entity = ResponseEntity.notFound();
        assertEquals(404, entity.status());
        assertNull(entity.body());
    }

    @Test
    void responseEntity_badRequest_withBody() {
        ResponseEntity<String> entity = ResponseEntity.badRequest("invalid input");
        assertEquals(400, entity.status());
        assertEquals("invalid input", entity.body());
    }

    @Test
    void responseEntity_internalServerError() {
        ResponseEntity<Object> entity = ResponseEntity.internalServerError();
        assertEquals(500, entity.status());
        assertNull(entity.body());
    }

    @Test
    void responseEntity_headers_areImmutable() {
        ResponseEntity<String> entity = ResponseEntity.ok("test");
        assertThrows(UnsupportedOperationException.class,
                () -> entity.headers().put("X-Test", "value"));
    }

    // ──────────────── ErrorResponse ────────────────

    @Test
    void errorResponse_of_withCodeAndMessage() {
        ErrorResponse err = ErrorResponse.of(400, "Bad input");
        assertEquals(400, err.code());
        assertEquals("Bad input", err.message());
        assertNotNull(err.timestamp());
        assertNull(err.data());
    }

    @Test
    void errorResponse_of_withData() {
        ErrorResponse err = ErrorResponse.of(500, "Error", Map.of("key", "value"));
        assertEquals(500, err.code());
        assertNotNull(err.data());
    }

    // ──────────────── ExceptionHandlerInfo ────────────────

    @Test
    void exceptionHandlerInfo_storesFields() throws Exception {
        Object handler = new Object();
        Method method = Object.class.getMethod("toString");
        ExceptionHandlerInfo info = new ExceptionHandlerInfo(handler, method,
                IllegalArgumentException.class, 0);

        assertSame(handler, info.handler());
        assertSame(method, info.method());
        assertEquals(IllegalArgumentException.class, info.exceptionType());
        assertEquals(0, info.order());
    }

    @Test
    void exceptionHandlerInfo_rejectsNullHandler() throws Exception {
        Method method = Object.class.getMethod("toString");
        assertThrows(IllegalArgumentException.class,
                () -> new ExceptionHandlerInfo(null, method, RuntimeException.class, 0));
    }

    @Test
    void exceptionHandlerInfo_rejectsNullMethod() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExceptionHandlerInfo(new Object(), null, RuntimeException.class, 0));
    }

    @Test
    void exceptionHandlerInfo_rejectsNullExceptionType() throws Exception {
        Method method = Object.class.getMethod("toString");
        assertThrows(IllegalArgumentException.class,
                () -> new ExceptionHandlerInfo(new Object(), method, null, 0));
    }
}
