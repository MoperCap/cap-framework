package org.moper.cap.property;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PropertyOperation 测试")
class PropertyOperationTest {

    @Nested
    @DisplayName("PropertySetOperation")
    class SetOperationTests {

        @Test
        @DisplayName("正确构造并访问字段")
        void testFieldAccess() {
            PropertySetOperation op = new PropertySetOperation("db.host", "localhost");
            assertEquals("db.host", op.key());
            assertEquals("localhost", op.value());
        }

        @Test
        @DisplayName("值可以为 null")
        void testNullValue() {
            PropertySetOperation op = new PropertySetOperation("key", null);
            assertNull(op.value());
        }

        @Test
        @DisplayName("值可以是任意类型")
        void testArbitraryValueType() {
            PropertySetOperation intOp = new PropertySetOperation("port", 3306);
            assertEquals(3306, intOp.value());

            PropertySetOperation boolOp = new PropertySetOperation("enabled", true);
            assertEquals(true, boolOp.value());
        }

        @Test
        @DisplayName("相同 key 和 value 的操作应相等")
        void testEquals() {
            PropertySetOperation op1 = new PropertySetOperation("key", "value");
            PropertySetOperation op2 = new PropertySetOperation("key", "value");
            assertEquals(op1, op2);
        }

        @Test
        @DisplayName("不同 key 的操作不相等")
        void testNotEqualDifferentKey() {
            PropertySetOperation op1 = new PropertySetOperation("key1", "value");
            PropertySetOperation op2 = new PropertySetOperation("key2", "value");
            assertNotEquals(op1, op2);
        }

        @Test
        @DisplayName("不同 value 的操作不相等")
        void testNotEqualDifferentValue() {
            PropertySetOperation op1 = new PropertySetOperation("key", "value1");
            PropertySetOperation op2 = new PropertySetOperation("key", "value2");
            assertNotEquals(op1, op2);
        }

        @Test
        @DisplayName("hashCode 一致性")
        void testHashCode() {
            PropertySetOperation op1 = new PropertySetOperation("key", "value");
            PropertySetOperation op2 = new PropertySetOperation("key", "value");
            assertEquals(op1.hashCode(), op2.hashCode());
        }

        @Test
        @DisplayName("equals 拒绝 null 和其他类型")
        void testEqualsWithNullAndOtherType() {
            PropertySetOperation op = new PropertySetOperation("key", "value");
            assertNotEquals(null, op);
            assertNotEquals("string", op);
        }
    }

    @Nested
    @DisplayName("PropertyRemoveOperation")
    class RemoveOperationTests {

        @Test
        @DisplayName("正确构造并访问字段")
        void testFieldAccess() {
            PropertyRemoveOperation op = new PropertyRemoveOperation("db.host");
            assertEquals("db.host", op.key());
        }

        @Test
        @DisplayName("相同 key 的操作应相等")
        void testEquals() {
            PropertyRemoveOperation op1 = new PropertyRemoveOperation("key");
            PropertyRemoveOperation op2 = new PropertyRemoveOperation("key");
            assertEquals(op1, op2);
        }

        @Test
        @DisplayName("不同 key 的操作不相等")
        void testNotEqualDifferentKey() {
            PropertyRemoveOperation op1 = new PropertyRemoveOperation("key1");
            PropertyRemoveOperation op2 = new PropertyRemoveOperation("key2");
            assertNotEquals(op1, op2);
        }

        @Test
        @DisplayName("hashCode 一致性")
        void testHashCode() {
            PropertyRemoveOperation op1 = new PropertyRemoveOperation("key");
            PropertyRemoveOperation op2 = new PropertyRemoveOperation("key");
            assertEquals(op1.hashCode(), op2.hashCode());
        }

        @Test
        @DisplayName("equals 拒绝 null 和其他类型")
        void testEqualsWithNullAndOtherType() {
            PropertyRemoveOperation op = new PropertyRemoveOperation("key");
            assertNotEquals(null, op);
            assertNotEquals("string", op);
        }
    }

    @Nested
    @DisplayName("SetOperation 和 RemoveOperation 互不相等")
    class CrossTypeTests {

        @Test
        @DisplayName("SetOperation 不等于 RemoveOperation（即使 key 相同）")
        void testSetAndRemoveAreNotEqual() {
            PropertySetOperation setOp = new PropertySetOperation("key", null);
            PropertyRemoveOperation removeOp = new PropertyRemoveOperation("key");
            assertNotEquals(setOp, removeOp);
        }
    }
}
