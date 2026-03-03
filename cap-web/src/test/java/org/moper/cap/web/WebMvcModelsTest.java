package org.moper.cap.web;

import org.junit.jupiter.api.Test;
import org.moper.cap.web.model.ErrorResponse;
import org.moper.cap.web.model.ExceptionHandlerInfo;
import org.moper.cap.web.model.ResponseEntity;
import org.moper.cap.web.util.TypeConverter;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for data models, TypeConverter, and ResponseEntity factory methods.
 */
class WebMvcModelsTest {

    // ──────────────── TypeConverter ────────────────

    @Test
    void typeConverter_convertString() {
        assertEquals("hello", TypeConverter.convert("hello", String.class));
    }

    @Test
    void typeConverter_convertInt() {
        assertEquals(42, TypeConverter.convert("42", int.class));
        assertEquals(42, TypeConverter.convert("42", Integer.class));
    }

    @Test
    void typeConverter_convertLong() {
        assertEquals(100L, TypeConverter.convert("100", long.class));
        assertEquals(100L, TypeConverter.convert("100", Long.class));
    }

    @Test
    void typeConverter_convertDouble() {
        assertEquals(3.14, TypeConverter.convert("3.14", double.class), 0.001);
        assertEquals(3.14, TypeConverter.convert("3.14", Double.class), 0.001);
    }

    @Test
    void typeConverter_convertFloat() {
        assertEquals(1.5f, TypeConverter.convert("1.5", float.class), 0.001f);
        assertEquals(1.5f, TypeConverter.convert("1.5", Float.class), 0.001f);
    }

    @Test
    void typeConverter_convertLocalDateTime() {
        String dateStr = "2024-01-15 10:30:00";
        java.time.LocalDateTime result = TypeConverter.convert(dateStr, java.time.LocalDateTime.class);
        assertNotNull(result);
        assertEquals(2024, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(30, result.getMinute());
    }

    @Test
    void typeConverter_invalidLocalDateTime_throwsException() {
        assertThrows(Exception.class,
                () -> TypeConverter.convert("not-a-date", java.time.LocalDateTime.class));
    }

    @Test
    void typeConverter_convertBoolean() {
        assertTrue(TypeConverter.convert("true", boolean.class));
        assertFalse(TypeConverter.convert("false", Boolean.class));
    }

    @Test
    void typeConverter_convertUUID() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, TypeConverter.convert(uuid.toString(), UUID.class));
    }

    @Test
    void typeConverter_nullReturnsDefaultForPrimitive() {
        assertEquals(0, TypeConverter.convert(null, int.class));
        assertEquals(0L, TypeConverter.convert(null, long.class));
        assertEquals(0.0, TypeConverter.convert(null, double.class), 0.001);
        assertFalse(TypeConverter.convert(null, boolean.class));
    }

    @Test
    void typeConverter_nullReturnsNullForObject() {
        assertNull(TypeConverter.convert(null, String.class));
    }

    @Test
    void typeConverter_throwsOnUnsupportedType() {
        assertThrows(IllegalArgumentException.class,
                () -> TypeConverter.convert("x", Object.class));
    }

    @Test
    void typeConverter_throwsOnNullType() {
        assertThrows(IllegalArgumentException.class,
                () -> TypeConverter.convert("x", null));
    }

    @Test
    void typeConverter_supports_returnsTrue() {
        assertTrue(TypeConverter.supports(String.class));
        assertTrue(TypeConverter.supports(int.class));
        assertTrue(TypeConverter.supports(Integer.class));
        assertTrue(TypeConverter.supports(boolean.class));
        assertTrue(TypeConverter.supports(UUID.class));
        assertTrue(TypeConverter.supports(LocalDateTime.class));
    }

    @Test
    void typeConverter_supports_returnsFalse() {
        assertFalse(TypeConverter.supports(Object.class));
    }

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
