package org.moper.cap.property;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moper.cap.property.publisher.impl.DefaultPropertyPublisher;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PropertyDefinition 测试")
class PropertyDefinitionTest {

    private DefaultPropertyPublisher buildPublisher(String name) {
        return DefaultPropertyPublisher.builder().name(name).build();
    }

    @Test
    @DisplayName("正确构造并访问字段")
    void testFieldAccess() {
        DefaultPropertyPublisher publisher = buildPublisher("p1");
        Instant now = Instant.now();
        PropertyDefinition def = new PropertyDefinition("key1", "value1", publisher, now);

        assertEquals("key1", def.name());
        assertEquals("value1", def.value());
        assertEquals(publisher, def.publisher());
        assertEquals(now, def.lastModified());
    }

    @Test
    @DisplayName("属性值可以为 null")
    void testNullValue() {
        DefaultPropertyPublisher publisher = buildPublisher("p1");
        PropertyDefinition def = new PropertyDefinition("key1", null, publisher, Instant.now());
        assertNull(def.value());
    }

    @Test
    @DisplayName("equals 仅比较 name 和 publisher")
    void testEqualsBasedOnNameAndPublisher() {
        DefaultPropertyPublisher publisher = buildPublisher("p1");
        Instant t1 = Instant.now();
        Instant t2 = t1.plusSeconds(10);

        PropertyDefinition def1 = new PropertyDefinition("key", "value1", publisher, t1);
        PropertyDefinition def2 = new PropertyDefinition("key", "value2", publisher, t2);

        assertEquals(def1, def2, "相同 name 和 publisher，即使 value 和 lastModified 不同，也应相等");
    }

    @Test
    @DisplayName("不同 name 的 PropertyDefinition 不相等")
    void testNotEqualDifferentName() {
        DefaultPropertyPublisher publisher = buildPublisher("p1");
        Instant now = Instant.now();

        PropertyDefinition def1 = new PropertyDefinition("key1", "value", publisher, now);
        PropertyDefinition def2 = new PropertyDefinition("key2", "value", publisher, now);

        assertNotEquals(def1, def2);
    }

    @Test
    @DisplayName("不同 publisher 的 PropertyDefinition 不相等")
    void testNotEqualDifferentPublisher() {
        DefaultPropertyPublisher pub1 = buildPublisher("p1");
        DefaultPropertyPublisher pub2 = buildPublisher("p2");
        Instant now = Instant.now();

        PropertyDefinition def1 = new PropertyDefinition("key", "value", pub1, now);
        PropertyDefinition def2 = new PropertyDefinition("key", "value", pub2, now);

        assertNotEquals(def1, def2);
    }

    @Test
    @DisplayName("hashCode 一致性：相等的对象应有相同 hashCode")
    void testHashCodeConsistency() {
        DefaultPropertyPublisher publisher = buildPublisher("p1");
        PropertyDefinition def1 = new PropertyDefinition("key", "v1", publisher, Instant.now());
        PropertyDefinition def2 = new PropertyDefinition("key", "v2", publisher, Instant.now().plusSeconds(5));

        assertEquals(def1.hashCode(), def2.hashCode());
    }

    @Test
    @DisplayName("equals 应拒绝 null 和其他类型")
    void testEqualsWithNullAndOtherType() {
        DefaultPropertyPublisher publisher = buildPublisher("p1");
        PropertyDefinition def = new PropertyDefinition("key", "value", publisher, Instant.now());

        assertNotEquals(null, def);
        assertNotEquals("not a PropertyDefinition", def);
    }
}
