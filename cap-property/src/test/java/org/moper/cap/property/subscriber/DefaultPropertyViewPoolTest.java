package org.moper.cap.property.subscriber;

import org.junit.jupiter.api.*;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.subscriber.view.DefaultPropertyViewPool;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultPropertyViewPool 测试")
class DefaultPropertyViewPoolTest {

    private DefaultPropertyViewPool viewPool;

    @BeforeEach
    void setUp() {
        viewPool = new DefaultPropertyViewPool("test-pool");
    }

    @AfterEach
    void tearDown() {
        if (!viewPool.isClosed()) {
            viewPool.close();
        }
    }

    // ===================== 基本属性 =====================

    @Test
    @DisplayName("正确返回 name")
    void testName() {
        assertEquals("test-pool", viewPool.name());
    }

    @Test
    @DisplayName("初始未关闭")
    void testInitialNotClosed() {
        assertFalse(viewPool.isClosed());
    }

    @Test
    @DisplayName("selector 返回 AnyPropertySelector")
    void testSelectorIsAny() {
        assertTrue(viewPool.selector().matches("any.key"));
        assertTrue(viewPool.selector().matches(""));
    }

    @Test
    @DisplayName("初始 keySet 为空")
    void testInitialKeySetEmpty() {
        assertTrue(viewPool.keySet().isEmpty());
    }

    // ===================== dispatch / 属性接收 =====================

    @Test
    @DisplayName("dispatch set 操作后属性可查询")
    void testDispatchSetOperation() {
        viewPool.dispatch(new PropertySetOperation("db.host", "localhost"));

        assertTrue(viewPool.containsProperty("db.host"));
        assertEquals("localhost", viewPool.getRawPropertyValue("db.host"));
    }

    @Test
    @DisplayName("dispatch remove 操作后属性被删除")
    void testDispatchRemoveOperation() {
        viewPool.dispatch(new PropertySetOperation("key", "value"));
        viewPool.dispatch(new PropertyRemoveOperation("key"));

        assertFalse(viewPool.containsProperty("key"));
        assertNull(viewPool.getRawPropertyValue("key"));
    }

    @Test
    @DisplayName("dispatch 多个 set 操作")
    void testDispatchMultipleSetOperations() {
        viewPool.dispatch(
                new PropertySetOperation("k1", "v1"),
                new PropertySetOperation("k2", "v2"),
                new PropertySetOperation("k3", "v3")
        );

        assertEquals(3, viewPool.keySet().size());
        assertEquals("v1", viewPool.getRawPropertyValue("k1"));
        assertEquals("v2", viewPool.getRawPropertyValue("k2"));
        assertEquals("v3", viewPool.getRawPropertyValue("k3"));
    }

    @Test
    @DisplayName("dispatch 更新已有属性值")
    void testDispatchUpdateExistingProperty() {
        viewPool.dispatch(new PropertySetOperation("key", "old"));
        viewPool.dispatch(new PropertySetOperation("key", "new"));

        assertEquals("new", viewPool.getRawPropertyValue("key"));
    }

    @Test
    @DisplayName("dispatch null 值属性时抛出 NullPointerException（ConcurrentHashMap 不允许 null 值）")
    void testDispatchNullValueThrowsNPE() {
        // ConcurrentHashMap 不允许 null 值，dispatch null 值时会抛出 NullPointerException
        assertThrows(NullPointerException.class,
                () -> viewPool.dispatch(new PropertySetOperation("key", null)));
    }

    // ===================== getRawPropertyValue =====================

    @Test
    @DisplayName("getRawPropertyValue 不存在的键返回 null")
    void testGetRawPropertyValueNotFound() {
        assertNull(viewPool.getRawPropertyValue("nonexistent"));
    }

    @Test
    @DisplayName("getRawPropertyValue key 为 null 抛出 IllegalArgumentException")
    void testGetRawPropertyValueNullKeyThrows() {
        assertThrows(IllegalArgumentException.class, () -> viewPool.getRawPropertyValue(null));
    }

    @Test
    @DisplayName("getRawPropertyValue key 为空白字符串抛出 IllegalArgumentException")
    void testGetRawPropertyValueBlankKeyThrows() {
        assertThrows(IllegalArgumentException.class, () -> viewPool.getRawPropertyValue("  "));
    }

    // ===================== getPropertyValue =====================

    @Test
    @DisplayName("getPropertyValue 返回正确类型")
    void testGetPropertyValueString() {
        viewPool.dispatch(new PropertySetOperation("key", "hello"));
        assertEquals("hello", viewPool.getPropertyValue("key", String.class));
    }

    @Test
    @DisplayName("getPropertyValue 将 Integer 转换为 String")
    void testGetPropertyValueNumberToString() {
        viewPool.dispatch(new PropertySetOperation("port", 3306));
        assertEquals("3306", viewPool.getPropertyValue("port", String.class));
    }

    @Test
    @DisplayName("getPropertyValue 将 Number 转换为 Integer")
    void testGetPropertyValueToInteger() {
        viewPool.dispatch(new PropertySetOperation("port", 3306));
        assertEquals(3306, viewPool.getPropertyValue("port", Integer.class));
    }

    @Test
    @DisplayName("getPropertyValue 将 Number 转换为 Long")
    void testGetPropertyValueToLong() {
        viewPool.dispatch(new PropertySetOperation("timeout", 5000L));
        assertEquals(5000L, viewPool.getPropertyValue("timeout", Long.class));
    }

    @Test
    @DisplayName("getPropertyValue 将 Number 转换为 Double")
    void testGetPropertyValueToDouble() {
        viewPool.dispatch(new PropertySetOperation("ratio", 1.5));
        assertEquals(1.5, viewPool.getPropertyValue("ratio", Double.class));
    }

    @Test
    @DisplayName("getPropertyValue 将 Number 转换为 Float")
    void testGetPropertyValueToFloat() {
        viewPool.dispatch(new PropertySetOperation("ratio", 1.5f));
        assertEquals(1.5f, viewPool.getPropertyValue("ratio", Float.class));
    }

    @Test
    @DisplayName("getPropertyValue 将 String 转换为 Boolean")
    void testGetPropertyValueStringToBoolean() {
        viewPool.dispatch(new PropertySetOperation("enabled", "true"));
        assertEquals(Boolean.TRUE, viewPool.getPropertyValue("enabled", Boolean.class));
    }

    @Test
    @DisplayName("getPropertyValue 不存在的键返回 null")
    void testGetPropertyValueNotFound() {
        assertNull(viewPool.getPropertyValue("nonexistent", String.class));
    }

    @Test
    @DisplayName("getPropertyValue 无法转换时返回 null")
    void testGetPropertyValueCannotConvert() {
        viewPool.dispatch(new PropertySetOperation("key", new Object()));
        assertNull(viewPool.getPropertyValue("key", Integer.class));
    }

    @Test
    @DisplayName("getPropertyValue key 为 null 抛出 IllegalArgumentException")
    void testGetPropertyValueNullKeyThrows() {
        assertThrows(IllegalArgumentException.class, () -> viewPool.getPropertyValue(null, String.class));
    }

    @Test
    @DisplayName("getPropertyValue type 为 null 抛出 IllegalArgumentException")
    void testGetPropertyValueNullTypeThrows() {
        viewPool.dispatch(new PropertySetOperation("key", "value"));
        assertThrows(IllegalArgumentException.class, () -> viewPool.getPropertyValue("key", null));
    }

    // ===================== getPropertyValueOrDefault =====================

    @Test
    @DisplayName("getPropertyValueOrDefault 属性存在时返回属性值")
    void testGetPropertyValueOrDefaultExists() {
        viewPool.dispatch(new PropertySetOperation("key", "value"));
        assertEquals("value", viewPool.getPropertyValueOrDefault("key", String.class, "default"));
    }

    @Test
    @DisplayName("getPropertyValueOrDefault 属性不存在时返回默认值")
    void testGetPropertyValueOrDefaultNotExists() {
        assertEquals("default", viewPool.getPropertyValueOrDefault("nonexistent", String.class, "default"));
    }

    @Test
    @DisplayName("getPropertyValueOrDefault 类型转换失败时返回默认值")
    void testGetPropertyValueOrDefaultTypeMismatch() {
        viewPool.dispatch(new PropertySetOperation("key", new Object()));
        assertEquals(42, viewPool.getPropertyValueOrDefault("key", Integer.class, 42));
    }

    @Test
    @DisplayName("getPropertyValueOrDefault key 为 null 抛出 IllegalArgumentException")
    void testGetPropertyValueOrDefaultNullKeyThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> viewPool.getPropertyValueOrDefault(null, String.class, "default"));
    }

    @Test
    @DisplayName("getPropertyValueOrDefault type 为 null 抛出 IllegalArgumentException")
    void testGetPropertyValueOrDefaultNullTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> viewPool.getPropertyValueOrDefault("key", null, "default"));
    }

    @Test
    @DisplayName("getPropertyValueOrDefault defaultValue 为 null 抛出 IllegalArgumentException")
    void testGetPropertyValueOrDefaultNullDefaultThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> viewPool.getPropertyValueOrDefault("key", String.class, null));
    }

    // ===================== getPropertyValueOptional =====================

    @Test
    @DisplayName("getPropertyValueOptional 属性存在时返回非空 Optional")
    void testGetPropertyValueOptionalExists() {
        viewPool.dispatch(new PropertySetOperation("key", "value"));
        Optional<String> opt = viewPool.getPropertyValueOptional("key", String.class);

        assertTrue(opt.isPresent());
        assertEquals("value", opt.get());
    }

    @Test
    @DisplayName("getPropertyValueOptional 属性不存在时返回空 Optional")
    void testGetPropertyValueOptionalNotExists() {
        Optional<String> opt = viewPool.getPropertyValueOptional("nonexistent", String.class);
        assertFalse(opt.isPresent());
    }

    @Test
    @DisplayName("getPropertyValueOptional 类型转换失败时返回空 Optional")
    void testGetPropertyValueOptionalTypeMismatch() {
        viewPool.dispatch(new PropertySetOperation("key", new Object()));
        Optional<Integer> opt = viewPool.getPropertyValueOptional("key", Integer.class);
        assertFalse(opt.isPresent());
    }

    @Test
    @DisplayName("getPropertyValueOptional key 为 null 抛出 IllegalArgumentException")
    void testGetPropertyValueOptionalNullKeyThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> viewPool.getPropertyValueOptional(null, String.class));
    }

    // ===================== containsProperty =====================

    @Test
    @DisplayName("containsProperty 属性存在时返回 true")
    void testContainsPropertyTrue() {
        viewPool.dispatch(new PropertySetOperation("key", "value"));
        assertTrue(viewPool.containsProperty("key"));
    }

    @Test
    @DisplayName("containsProperty 属性不存在时返回 false")
    void testContainsPropertyFalse() {
        assertFalse(viewPool.containsProperty("nonexistent"));
    }

    @Test
    @DisplayName("containsProperty key 为 null 抛出 IllegalArgumentException")
    void testContainsPropertyNullKeyThrows() {
        assertThrows(IllegalArgumentException.class, () -> viewPool.containsProperty(null));
    }

    // ===================== keySet =====================

    @Test
    @DisplayName("keySet 返回所有属性键")
    void testKeySet() {
        viewPool.dispatch(
                new PropertySetOperation("k1", "v1"),
                new PropertySetOperation("k2", "v2")
        );

        Set<String> keys = viewPool.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("k1"));
        assertTrue(keys.contains("k2"));
    }

    @Test
    @DisplayName("keySet 返回不可修改的集合")
    void testKeySetIsUnmodifiable() {
        viewPool.dispatch(new PropertySetOperation("k1", "v1"));
        assertThrows(UnsupportedOperationException.class,
                () -> viewPool.keySet().clear());
    }

    // ===================== 关闭行为 =====================

    @Test
    @DisplayName("close 后 isClosed 返回 true")
    void testCloseSetsClosed() {
        viewPool.close();
        assertTrue(viewPool.isClosed());
    }

    @Test
    @DisplayName("close 幂等")
    void testCloseIdempotent() {
        assertDoesNotThrow(() -> {
            viewPool.close();
            viewPool.close();
        });
    }

    @Test
    @DisplayName("close 后 dispatch 被忽略")
    void testDispatchAfterClosed() {
        viewPool.close();
        viewPool.dispatch(new PropertySetOperation("key", "value"));
        assertFalse(viewPool.containsProperty("key"));
    }

    @Test
    @DisplayName("close 后已缓存的属性值仍可查询")
    void testCachedValuesAvailableAfterClose() {
        viewPool.dispatch(new PropertySetOperation("key", "value"));
        viewPool.close();

        assertEquals("value", viewPool.getRawPropertyValue("key"));
    }

    // ===================== offOfficer =====================

    @Test
    @DisplayName("offOfficer 清空所有属性")
    void testOffOfficerClearsProperties() {
        viewPool.dispatch(
                new PropertySetOperation("k1", "v1"),
                new PropertySetOperation("k2", "v2")
        );

        viewPool.offOfficer(null);

        assertTrue(viewPool.keySet().isEmpty());
    }

    @Test
    @DisplayName("offOfficer 关闭后静默忽略")
    void testOffOfficerAfterClosed() {
        viewPool.dispatch(new PropertySetOperation("key", "value"));
        viewPool.close();

        assertDoesNotThrow(() -> viewPool.offOfficer(null));
        // 关闭后 offOfficer 不清空属性
        assertTrue(viewPool.containsProperty("key"));
    }
}
