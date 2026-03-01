package org.moper.cap.bean.definition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.moper.cap.bean.exception.BeanDefinitionStoreException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InstantiationPolicy")
class InstantiationPolicyTest {

    // ==================== 构造函数策略 ====================

    @Nested
    @DisplayName("constructor() - 无参构造函数策略")
    class ConstructorNoArg {

        @Test
        @DisplayName("创建成功，argTypes 为空数组")
        void create_success_argTypesIsEmpty() {
            InstantiationPolicy policy = InstantiationPolicy.constructor();
            assertNotNull(policy);
            assertInstanceOf(InstantiationPolicy.ConstructorInstantiation.class, policy);
            assertArrayEquals(new Class<?>[0], policy.argTypes());
        }

        @Test
        @DisplayName("isConstructor() 返回 true")
        void isConstructor_returnsTrue() {
            assertTrue(InstantiationPolicy.constructor().isConstructor());
        }

        @Test
        @DisplayName("isStaticFactory() 返回 false")
        void isStaticFactory_returnsFalse() {
            assertFalse(InstantiationPolicy.constructor().isStaticFactory());
        }

        @Test
        @DisplayName("isInstanceFactory() 返回 false")
        void isInstanceFactory_returnsFalse() {
            assertFalse(InstantiationPolicy.constructor().isInstanceFactory());
        }
    }

    @Nested
    @DisplayName("constructor(Class...) - 有参构造函数策略")
    class ConstructorWithArgs {

        @Test
        @DisplayName("创建成功，argTypes 被正确保存")
        void create_success_argTypesPreserved() {
            InstantiationPolicy policy = InstantiationPolicy.constructor(String.class, Integer.class);
            assertArrayEquals(new Class<?>[]{String.class, Integer.class}, policy.argTypes());
            assertInstanceOf(InstantiationPolicy.ConstructorInstantiation.class, policy);
        }

        @Test
        @DisplayName("isConstructor() 返回 true，其余返回 false")
        void isConstructor_returnsTrue_othersReturnFalse() {
            InstantiationPolicy policy = InstantiationPolicy.constructor(String.class);
            assertTrue(policy.isConstructor());
            assertFalse(policy.isStaticFactory());
            assertFalse(policy.isInstanceFactory());
        }
    }

    // ==================== 静态工厂策略 ====================

    @Nested
    @DisplayName("staticFactory() - 静态工厂方法策略")
    class StaticFactory {

        @Test
        @DisplayName("创建成功，factoryBeanName 和 factoryMethodName 均被正确保存")
        void create_success_fieldsCorrect() {
            InstantiationPolicy policy = InstantiationPolicy.staticFactory("myFactory", "create");
            assertInstanceOf(InstantiationPolicy.StaticFactoryInstantiation.class, policy);
            InstantiationPolicy.StaticFactoryInstantiation sfi =
                    (InstantiationPolicy.StaticFactoryInstantiation) policy;
            assertEquals("myFactory", sfi.factoryBeanName());
            assertEquals("create", sfi.factoryMethodName());
            assertArrayEquals(new Class<?>[0], policy.argTypes());
        }

        @Test
        @DisplayName("创建成功，有参场景 argTypes 被正确保存")
        void create_withArgs_argTypesPreserved() {
            InstantiationPolicy policy = InstantiationPolicy.staticFactory("myFactory", "createWith", String.class);
            assertArrayEquals(new Class<?>[]{String.class}, policy.argTypes());
        }

        @Test
        @DisplayName("factoryBeanName 为 null 时抛出 BeanDefinitionStoreException")
        void create_nullFactoryBeanName_throwsBeanDefinitionStoreException() {
            assertThrows(BeanDefinitionStoreException.class,
                    () -> InstantiationPolicy.staticFactory(null, "create"));
        }

        @Test
        @DisplayName("factoryBeanName 为空白字符串时抛出 BeanDefinitionStoreException")
        void create_blankFactoryBeanName_throwsBeanDefinitionStoreException() {
            assertThrows(BeanDefinitionStoreException.class,
                    () -> InstantiationPolicy.staticFactory("   ", "create"));
        }

        @Test
        @DisplayName("methodName 为 null 时抛出 BeanDefinitionStoreException")
        void create_nullMethodName_throwsBeanDefinitionStoreException() {
            assertThrows(BeanDefinitionStoreException.class,
                    () -> InstantiationPolicy.staticFactory("myFactory", null));
        }

        @Test
        @DisplayName("methodName 为空白字符串时抛出 BeanDefinitionStoreException")
        void create_blankMethodName_throwsBeanDefinitionStoreException() {
            assertThrows(BeanDefinitionStoreException.class,
                    () -> InstantiationPolicy.staticFactory("myFactory", "   "));
        }

        @Test
        @DisplayName("isStaticFactory() 返回 true，其余返回 false")
        void isStaticFactory_returnsTrue_othersReturnFalse() {
            InstantiationPolicy policy = InstantiationPolicy.staticFactory("myFactory", "create");
            assertFalse(policy.isConstructor());
            assertTrue(policy.isStaticFactory());
            assertFalse(policy.isInstanceFactory());
        }
    }

    // ==================== 实例工厂策略 ====================

    @Nested
    @DisplayName("instanceFactory() - 实例工厂方法策略")
    class InstanceFactory {

        @Test
        @DisplayName("创建成功，factoryBeanName 和 factoryMethodName 均被正确保存")
        void create_success_fieldsCorrect() {
            InstantiationPolicy policy = InstantiationPolicy.instanceFactory("myFactory", "build");
            assertInstanceOf(InstantiationPolicy.InstanceFactoryInstantiation.class, policy);
            InstantiationPolicy.InstanceFactoryInstantiation ifi =
                    (InstantiationPolicy.InstanceFactoryInstantiation) policy;
            assertEquals("myFactory", ifi.factoryBeanName());
            assertEquals("build", ifi.factoryMethodName());
            assertArrayEquals(new Class<?>[0], policy.argTypes());
        }

        @Test
        @DisplayName("factoryBeanName 为 null 时抛出 BeanDefinitionStoreException")
        void create_nullFactoryBeanName_throwsBeanDefinitionStoreException() {
            assertThrows(BeanDefinitionStoreException.class,
                    () -> InstantiationPolicy.instanceFactory(null, "build"));
        }

        @Test
        @DisplayName("factoryBeanName 为空白字符串时抛出 BeanDefinitionStoreException")
        void create_blankFactoryBeanName_throwsBeanDefinitionStoreException() {
            assertThrows(BeanDefinitionStoreException.class,
                    () -> InstantiationPolicy.instanceFactory("  ", "build"));
        }

        @Test
        @DisplayName("methodName 为 null 时抛出 BeanDefinitionStoreException")
        void create_nullMethodName_throwsBeanDefinitionStoreException() {
            assertThrows(BeanDefinitionStoreException.class,
                    () -> InstantiationPolicy.instanceFactory("myFactory", null));
        }

        @Test
        @DisplayName("methodName 为空白字符串时抛出 BeanDefinitionStoreException")
        void create_blankMethodName_throwsBeanDefinitionStoreException() {
            assertThrows(BeanDefinitionStoreException.class,
                    () -> InstantiationPolicy.instanceFactory("myFactory", "  "));
        }

        @Test
        @DisplayName("isInstanceFactory() 返回 true，其余返回 false")
        void isInstanceFactory_returnsTrue_othersReturnFalse() {
            InstantiationPolicy policy = InstantiationPolicy.instanceFactory("factory", "build");
            assertFalse(policy.isConstructor());
            assertFalse(policy.isStaticFactory());
            assertTrue(policy.isInstanceFactory());
        }
    }

    // ==================== 三种策略互斥验证 ====================

    @Nested
    @DisplayName("三种策略互斥性验证")
    class MutualExclusivity {

        @Test
        @DisplayName("同一时刻只有一种策略的 isXxx() 返回 true")
        void onlyOneStrategyActiveAtATime() {
            InstantiationPolicy constructor    = InstantiationPolicy.constructor();
            InstantiationPolicy staticFactory  = InstantiationPolicy.staticFactory("f", "m");
            InstantiationPolicy instanceFactory = InstantiationPolicy.instanceFactory("f", "m");

            // constructor
            assertEquals(1, countTrue(constructor.isConstructor(),
                    constructor.isStaticFactory(), constructor.isInstanceFactory()));
            // staticFactory
            assertEquals(1, countTrue(staticFactory.isConstructor(),
                    staticFactory.isStaticFactory(), staticFactory.isInstanceFactory()));
            // instanceFactory
            assertEquals(1, countTrue(instanceFactory.isConstructor(),
                    instanceFactory.isStaticFactory(), instanceFactory.isInstanceFactory()));
        }

        private int countTrue(boolean... values) {
            int count = 0;
            for (boolean v : values) if (v) count++;
            return count;
        }
    }
}
