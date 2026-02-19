package org.moper.cap.property.subscriber;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.moper.cap.property.subscriber.selector.AnyPropertySelector;
import org.moper.cap.property.subscriber.selector.ExactPropertySelector;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PropertySelector 测试")
class PropertySelectorTest {

    @Nested
    @DisplayName("AnyPropertySelector")
    class AnyPropertySelectorTests {

        @Test
        @DisplayName("对任意非空键返回 true")
        void testMatchesAnyKey() {
            AnyPropertySelector selector = new AnyPropertySelector();
            assertTrue(selector.matches("db.host"));
            assertTrue(selector.matches("app.name"));
            assertTrue(selector.matches("x"));
            assertTrue(selector.matches(""));
        }

        @Test
        @DisplayName("对 null 键也返回 true（不做空检查）")
        void testMatchesNull() {
            AnyPropertySelector selector = new AnyPropertySelector();
            assertTrue(selector.matches(null));
        }
    }

    @Nested
    @DisplayName("ExactPropertySelector")
    class ExactPropertySelectorTests {

        @Test
        @DisplayName("匹配已注册的精确键")
        void testMatchesRegisteredKey() {
            ExactPropertySelector selector = new ExactPropertySelector(Set.of("db.host", "db.port"));
            assertTrue(selector.matches("db.host"));
            assertTrue(selector.matches("db.port"));
        }

        @Test
        @DisplayName("不匹配未注册的键")
        void testDoesNotMatchUnregisteredKey() {
            ExactPropertySelector selector = new ExactPropertySelector(Set.of("db.host"));
            assertFalse(selector.matches("db.port"));
            assertFalse(selector.matches("db.host.extra"));
        }

        @Test
        @DisplayName("空键集合时不匹配任何键")
        void testEmptyKeySet() {
            ExactPropertySelector selector = new ExactPropertySelector(Set.of());
            assertFalse(selector.matches("any.key"));
        }

        @Test
        @DisplayName("大小写敏感匹配")
        void testCaseSensitive() {
            ExactPropertySelector selector = new ExactPropertySelector(Set.of("DB.HOST"));
            assertFalse(selector.matches("db.host"));
            assertTrue(selector.matches("DB.HOST"));
        }
    }
}
