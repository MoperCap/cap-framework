package org.moper.cap.bean.definition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.moper.cap.bean.fixture.SimpleBean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BeanDefinition")
class BeanDefinitionTest {

    // ==================== of() 工厂方法 ====================

    @Nested
    @DisplayName("of() - 静态工厂入口")
    class OfFactory {

        @Test
        @DisplayName("创建成功，name 和 type 被正确保存")
        void create_success_nameAndTypePreserved() {
            BeanDefinition def = BeanDefinition.of("myBean", SimpleBean.class);
            assertEquals("myBean", def.name());
            assertEquals(SimpleBean.class, def.type());
        }

        @Test
        @DisplayName("默认 scope 为 SINGLETON")
        void defaultScope_isSingleton() {
            BeanDefinition def = BeanDefinition.of("bean", SimpleBean.class);
            assertEquals(BeanScope.SINGLETON, def.scope());
            assertTrue(def.isSingleton());
            assertFalse(def.isPrototype());
        }

        @Test
        @DisplayName("默认 lazy 为 false")
        void defaultLazy_isFalse() {
            assertFalse(BeanDefinition.of("bean", SimpleBean.class).lazy());
        }

        @Test
        @DisplayName("默认 primary 为 false")
        void defaultPrimary_isFalse() {
            assertFalse(BeanDefinition.of("bean", SimpleBean.class).primary());
        }

        @Test
        @DisplayName("默认 autowired 为 true")
        void defaultAutowired_isTrue() {
            assertTrue(BeanDefinition.of("bean", SimpleBean.class).autowired());
        }

        @Test
        @DisplayName("默认 description 为空字符串")
        void defaultDescription_isEmpty() {
            assertEquals("", BeanDefinition.of("bean", SimpleBean.class).description());
        }

        @Test
        @DisplayName("默认 dependsOn 为空数组")
        void defaultDependsOn_isEmpty() {
            assertArrayEquals(new String[0], BeanDefinition.of("bean", SimpleBean.class).dependsOn());
        }

        @Test
        @DisplayName("默认 instantiationPolicy 为无参构造函数策略")
        void defaultInstantiationPolicy_isConstructor() {
            assertTrue(BeanDefinition.of("bean", SimpleBean.class)
                    .instantiationPolicy().isConstructor());
        }

        @Test
        @DisplayName("name 为 null 时抛出 IllegalArgumentException")
        void nullName_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> BeanDefinition.of(null, SimpleBean.class));
        }

        @Test
        @DisplayName("name 为空白字符串时抛出 IllegalArgumentException")
        void blankName_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> BeanDefinition.of("   ", SimpleBean.class));
        }

        @Test
        @DisplayName("type 为 null 时抛出 IllegalArgumentException")
        void nullType_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> BeanDefinition.of("bean", null));
        }
    }

    // ==================== 不可变性验证（with* 方法）====================

    @Nested
    @DisplayName("with* 方法 - 不可变性验证")
    class WithMethods {

        @Test
        @DisplayName("withLazy() 返回新实例，原实例的 lazy 不变")
        void withLazy_returnsNewInstance_originalUnchanged() {
            BeanDefinition original = BeanDefinition.of("bean", SimpleBean.class);
            BeanDefinition modified = original.withLazy(true);

            assertNotSame(original, modified);
            assertFalse(original.lazy());
            assertTrue(modified.lazy());
        }

        @Test
        @DisplayName("withPrimary() 返回新实例，原实例的 primary 不变")
        void withPrimary_returnsNewInstance_originalUnchanged() {
            BeanDefinition original = BeanDefinition.of("bean", SimpleBean.class);
            BeanDefinition modified = original.withPrimary(true);

            assertNotSame(original, modified);
            assertFalse(original.primary());
            assertTrue(modified.primary());
        }

        @Test
        @DisplayName("withAutowired() 返回新实例，原实例的 autowired 不变")
        void withAutowired_returnsNewInstance_originalUnchanged() {
            BeanDefinition original = BeanDefinition.of("bean", SimpleBean.class);
            BeanDefinition modified = original.withAutowired(false);

            assertNotSame(original, modified);
            assertTrue(original.autowired());
            assertFalse(modified.autowired());
        }

        @Test
        @DisplayName("withScope() 返回新实例，原实例的 scope 不变")
        void withScope_returnsNewInstance_originalUnchanged() {
            BeanDefinition original = BeanDefinition.of("bean", SimpleBean.class);
            BeanDefinition modified = original.withScope(BeanScope.PROTOTYPE);

            assertNotSame(original, modified);
            assertEquals(BeanScope.SINGLETON, original.scope());
            assertEquals(BeanScope.PROTOTYPE, modified.scope());
        }

        @Test
        @DisplayName("withDescription() 返回新实例，原实例的 description 不变")
        void withDescription_returnsNewInstance_originalUnchanged() {
            BeanDefinition original = BeanDefinition.of("bean", SimpleBean.class);
            BeanDefinition modified = original.withDescription("new desc");

            assertNotSame(original, modified);
            assertEquals("", original.description());
            assertEquals("new desc", modified.description());
        }

        @Test
        @DisplayName("withInstantiationPolicy() 返回新实例，原实例的 policy 不变")
        void withInstantiationPolicy_returnsNewInstance_originalUnchanged() {
            BeanDefinition original = BeanDefinition.of("bean", SimpleBean.class);
            InstantiationPolicy staticPolicy = InstantiationPolicy.staticFactory("create");
            BeanDefinition modified = original.withInstantiationPolicy(staticPolicy);

            assertNotSame(original, modified);
            assertTrue(original.instantiationPolicy().isConstructor());
            assertTrue(modified.instantiationPolicy().isStaticFactory());
        }

        @Test
        @DisplayName("dependsOn() 返回新实例，原实例的 dependsOn 不变")
        void dependsOn_returnsNewInstance_originalUnchanged() {
            BeanDefinition original = BeanDefinition.of("bean", SimpleBean.class);
            BeanDefinition modified = original.dependsOn("depA", "depB");

            assertNotSame(original, modified);
            assertArrayEquals(new String[0], original.dependsOn());
            assertArrayEquals(new String[]{"depA", "depB"}, modified.dependsOn());
        }
    }

    // ==================== 链式调用 ====================

    @Nested
    @DisplayName("链式调用")
    class ChainedCalls {

        @Test
        @DisplayName("链式调用所有 with* 方法，每个字段都被正确设置")
        void chainedCalls_allFieldsSetCorrectly() {
            InstantiationPolicy policy = InstantiationPolicy.staticFactory("create");
            BeanDefinition def = BeanDefinition.of("dataSource", SimpleBean.class)
                    .withInstantiationPolicy(policy)
                    .withScope(BeanScope.PROTOTYPE)
                    .dependsOn("configBean")
                    .withLazy(true)
                    .withPrimary(true)
                    .withAutowired(false)
                    .withDescription("测试用数据源");

            assertEquals("dataSource", def.name());
            assertEquals(SimpleBean.class, def.type());
            assertEquals(BeanScope.PROTOTYPE, def.scope());
            assertTrue(def.instantiationPolicy().isStaticFactory());
            assertArrayEquals(new String[]{"configBean"}, def.dependsOn());
            assertTrue(def.lazy());
            assertTrue(def.primary());
            assertFalse(def.autowired());
            assertEquals("测试用数据源", def.description());
        }
    }

    // ==================== isSingleton / isPrototype ====================

    @Nested
    @DisplayName("isSingleton() / isPrototype()")
    class ScopeChecks {

        @Test
        @DisplayName("SINGLETON scope：isSingleton=true，isPrototype=false")
        void singleton_scope() {
            BeanDefinition def = BeanDefinition.of("bean", SimpleBean.class);
            assertTrue(def.isSingleton());
            assertFalse(def.isPrototype());
        }

        @Test
        @DisplayName("PROTOTYPE scope：isSingleton=false，isPrototype=true")
        void prototype_scope() {
            BeanDefinition def = BeanDefinition.of("bean", SimpleBean.class)
                    .withScope(BeanScope.PROTOTYPE);
            assertFalse(def.isSingleton());
            assertTrue(def.isPrototype());
        }

        @Test
        @DisplayName("REQUEST scope：isSingleton=false，isPrototype=false")
        void request_scope() {
            BeanDefinition def = BeanDefinition.of("bean", SimpleBean.class)
                    .withScope(BeanScope.REQUEST);
            assertFalse(def.isSingleton());
            assertFalse(def.isPrototype());
        }
    }
}