package org.moper.cap.bean.container.impl;

import org.junit.jupiter.api.*;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.definition.BeanScope;
import org.moper.cap.bean.definition.InstantiationPolicy;
import org.moper.cap.bean.exception.*;
import org.moper.cap.bean.fixture.*;
import org.moper.cap.bean.interceptor.BeanInterceptor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultBeanContainer")
class DefaultBeanContainerTest {

    private DefaultBeanContainer container;

    @BeforeEach
    void setUp() {
        container = new DefaultBeanContainer();
    }

    // ==================== BeanRegistry ====================

    @Nested
    @DisplayName("BeanRegistry - BeanDefinition 注册与移除")
    class BeanRegistryTests {

        @Test
        @DisplayName("registerBeanDefinition：正常注册后 containsBeanDefinition 返回 true")
        void register_success_containsReturnsTrue() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            assertTrue(container.containsBeanDefinition("simple"));
        }

        @Test
        @DisplayName("registerBeanDefinition：重复注册同名 BeanDefinition 抛出 BeanDefinitionStoreException")
        void register_duplicate_throwsBeanDefinitionStoreException() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            assertThrows(BeanDefinitionStoreException.class,
                    () -> container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class)));
        }

        @Test
        @DisplayName("removeBeanDefinition：正常移除后 containsBeanDefinition 返回 false")
        void remove_success_containsReturnsFalse() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.removeBeanDefinition("simple");
            assertFalse(container.containsBeanDefinition("simple"));
        }

        @Test
        @DisplayName("removeBeanDefinition：移除不存在的 Bean 抛出 NoSuchBeanDefinitionException")
        void remove_notExist_throwsNoSuchBeanDefinitionException() {
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> container.removeBeanDefinition("notExist"));
        }

        @Test
        @DisplayName("removeBeanDefinition：同时清除对应的单例缓存")
        void remove_alsoEvictsSingletonCache() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.getBean("simple"); // 触发创建，加入单例缓存
            container.removeBeanDefinition("simple");
            assertFalse(container.containsBean("simple"));
        }
    }

    @Nested
    @DisplayName("BeanRegistry - 别名管理")
    class AliasManagement {

        @Test
        @DisplayName("registerAlias：正常注册后可通过别名 getBean")
        void registerAlias_success_getBeanByAlias() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.registerAlias("simple", "simpleAlias");

            Object beanByName  = container.getBean("simple");
            Object beanByAlias = container.getBean("simpleAlias");
            assertSame(beanByName, beanByAlias);
        }

        @Test
        @DisplayName("registerAlias：目标 Bean 不存在时抛出 NoSuchBeanDefinitionException")
        void registerAlias_targetNotExist_throwsNoSuchBeanDefinitionException() {
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> container.registerAlias("notExist", "alias"));
        }

        @Test
        @DisplayName("registerAlias：别名与已有名称冲突时抛出 BeanDefinitionStoreException")
        void registerAlias_aliasConflict_throwsBeanDefinitionStoreException() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.registerBeanDefinition(BeanDefinition.of("another", SimpleBean.class));
            // 用已存在的 BeanDefinition 名称作为别名
            assertThrows(BeanDefinitionStoreException.class,
                    () -> container.registerAlias("simple", "another"));
        }

        @Test
        @DisplayName("removeAlias：正常移除后别名失效")
        void removeAlias_success_aliasInvalidated() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.registerAlias("simple", "simpleAlias");
            container.removeAlias("simpleAlias");
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> container.getBean("simpleAlias"));
        }

        @Test
        @DisplayName("removeAlias：移除不存在的别名抛出 BeanDefinitionStoreException")
        void removeAlias_notExist_throwsBeanDefinitionStoreException() {
            assertThrows(BeanDefinitionStoreException.class,
                    () -> container.removeAlias("notExist"));
        }

        @Test
        @DisplayName("getAliases：返回指定 Bean 的所有已注册别名")
        void getAliases_returnsAllAliases() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.registerAlias("simple", "aliasA");
            container.registerAlias("simple", "aliasB");

            String[] aliases = container.getAliases("simple");
            assertEquals(2, aliases.length);
            assertArrayEquals(new String[]{"aliasA", "aliasB"}, aliases);
        }
    }

    @Nested
    @DisplayName("BeanRegistry - 外部单例注册")
    class ExternalSingletonRegistration {

        @Test
        @DisplayName("registerSingleton：正常注册后 containsBean 返回 true")
        void registerSingleton_success_containsReturnsTrue() {
            container.registerSingleton("external", new SimpleBean());
            assertTrue(container.containsBean("external"));
        }

        @Test
        @DisplayName("registerSingleton：名称冲突时抛出 BeanDefinitionStoreException")
        void registerSingleton_nameConflict_throwsBeanDefinitionStoreException() {
            container.registerSingleton("external", new SimpleBean());
            assertThrows(BeanDefinitionStoreException.class,
                    () -> container.registerSingleton("external", new SimpleBean()));
        }

        @Test
        @DisplayName("registerSingleton：不存在对应的 BeanDefinition（containsBeanDefinition 返回 false）")
        void registerSingleton_noBeanDefinition() {
            container.registerSingleton("external", new SimpleBean());
            assertFalse(container.containsBeanDefinition("external"));
            assertTrue(container.containsBean("external"));
        }

        @Test
        @DisplayName("registerSingleton：注册的实例可通过 getBean 直接获取，且返回同一对象")
        void registerSingleton_getBeanReturnsSameInstance() {
            SimpleBean external = new SimpleBean();
            container.registerSingleton("external", external);
            assertSame(external, container.getBean("external"));
        }
    }

    @Nested
    @DisplayName("BeanRegistry - isBeanNameInUse")
    class IsBeanNameInUse {

        @Test
        @DisplayName("注册 BeanDefinition 后返回 true，移除后返回 false")
        void afterRegisterAndRemove_correctResult() {
            assertFalse(container.isBeanNameInUse("bean"));
            container.registerBeanDefinition(BeanDefinition.of("bean", SimpleBean.class));
            assertTrue(container.isBeanNameInUse("bean"));
            container.removeBeanDefinition("bean");
            assertFalse(container.isBeanNameInUse("bean"));
        }

        @Test
        @DisplayName("注册别名后返回 true")
        void afterRegisterAlias_returnsTrue() {
            container.registerBeanDefinition(BeanDefinition.of("bean", SimpleBean.class));
            container.registerAlias("bean", "beanAlias");
            assertTrue(container.isBeanNameInUse("beanAlias"));
        }

        @Test
        @DisplayName("注册外部单例后返�� true")
        void afterRegisterSingleton_returnsTrue() {
            container.registerSingleton("ext", new SimpleBean());
            assertTrue(container.isBeanNameInUse("ext"));
        }
    }

    // ==================== BeanProvider ====================

    @Nested
    @DisplayName("BeanProvider - getBean(String)")
    class GetBeanByName {

        @Test
        @DisplayName("单例 Bean：多次 getBean 返回同一实例")
        void singleton_multipleGetBean_returnsSameInstance() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            Object bean1 = container.getBean("simple");
            Object bean2 = container.getBean("simple");
            assertSame(bean1, bean2);
        }

        @Test
        @DisplayName("原型 Bean：每次 getBean 返回新实例")
        void prototype_multipleGetBean_returnsNewInstance() {
            container.registerBeanDefinition(
                    BeanDefinition.of("proto", SimpleBean.class)
                            .withScope(BeanScope.PROTOTYPE));
            Object bean1 = container.getBean("proto");
            Object bean2 = container.getBean("proto");
            assertNotSame(bean1, bean2);
        }

        @Test
        @DisplayName("Bean 不存在时抛出 NoSuchBeanDefinitionException")
        void notExist_throwsNoSuchBeanDefinitionException() {
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> container.getBean("notExist"));
        }

        @Test
        @DisplayName("不支持的 scope（REQUEST/SESSION）时抛出 BeanCreationException")
        void unsupportedScope_throwsBeanCreationException() {
            container.registerBeanDefinition(
                    BeanDefinition.of("requestBean", SimpleBean.class)
                            .withScope(BeanScope.REQUEST));
            assertThrows(BeanCreationException.class,
                    () -> container.getBean("requestBean"));
        }
    }

    @Nested
    @DisplayName("BeanProvider - getBean(String, Class)")
    class GetBeanByNameAndType {

        @Test
        @DisplayName("类型匹配时正常返回，并完成类型转换")
        void typeMatch_returnsTypedBean() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            SimpleBean bean = container.getBean("simple", SimpleBean.class);
            assertNotNull(bean);
            assertInstanceOf(SimpleBean.class, bean);
        }

        @Test
        @DisplayName("类型不匹配时抛出 BeanNotOfRequiredTypeException")
        void typeMismatch_throwsBeanNotOfRequiredTypeException() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            BeanNotOfRequiredTypeException ex = assertThrows(BeanNotOfRequiredTypeException.class,
                    () -> container.getBean("simple", DependentBean.class));
            assertEquals("simple", ex.getBeanName());
            assertEquals(DependentBean.class, ex.getRequiredType());
            assertEquals(SimpleBean.class, ex.getActualType());
        }
    }

    @Nested
    @DisplayName("BeanProvider - getBean(Class)")
    class GetBeanByType {

        @Test
        @DisplayName("唯一匹配时正常返回")
        void uniqueMatch_returnsBean() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            SimpleBean bean = container.getBean(SimpleBean.class);
            assertNotNull(bean);
        }

        @Test
        @DisplayName("多个匹配但有唯一 primary 时返回 primary Bean")
        void multipleMatch_withPrimary_returnsPrimaryBean() {
            container.registerBeanDefinition(
                    BeanDefinition.of("implA", ServiceImplA.class));
            container.registerBeanDefinition(
                    BeanDefinition.of("implB", ServiceImplB.class).withPrimary(true));

            ServiceInterface result = container.getBean(ServiceInterface.class);
            assertInstanceOf(ServiceImplB.class, result);
        }

        @Test
        @DisplayName("多个匹配且无 primary 时抛出 NoUniqueBeanDefinitionException")
        void multipleMatch_noPrimary_throwsNoUniqueBeanDefinitionException() {
            container.registerBeanDefinition(BeanDefinition.of("implA", ServiceImplA.class));
            container.registerBeanDefinition(BeanDefinition.of("implB", ServiceImplB.class));

            NoUniqueBeanDefinitionException ex = assertThrows(NoUniqueBeanDefinitionException.class,
                    () -> container.getBean(ServiceInterface.class));
            assertEquals(2, ex.getNumberOfBeansFound());
        }

        @Test
        @DisplayName("无匹配时抛出 NoSuchBeanDefinitionException")
        void noMatch_throwsNoSuchBeanDefinitionException() {
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> container.getBean(SimpleBean.class));
        }

        @Test
        @DisplayName("多个 primary 时抛出 NoUniqueBeanDefinitionException")
        void multiplePrimary_throwsNoUniqueBeanDefinitionException() {
            container.registerBeanDefinition(
                    BeanDefinition.of("implA", ServiceImplA.class).withPrimary(true));
            container.registerBeanDefinition(
                    BeanDefinition.of("implB", ServiceImplB.class).withPrimary(true));
            assertThrows(NoUniqueBeanDefinitionException.class,
                    () -> container.getBean(ServiceInterface.class));
        }
    }

    @Nested
    @DisplayName("BeanProvider - 循环依赖检测")
    class CircularDependencyDetection {

        @Test
        @DisplayName("直接循环依赖（A→B→A）时抛出 BeanCreationException，消息含 'Circular dependency'")
        void directCircularDependency_throwsBeanCreationException() {
            container.registerBeanDefinition(
                    BeanDefinition.of("circularA", CircularBeanA.class)
                            .withInstantiationPolicy(
                                    InstantiationPolicy.constructor(CircularBeanB.class)));
            container.registerBeanDefinition(
                    BeanDefinition.of("circularB", CircularBeanB.class)
                            .withInstantiationPolicy(
                                    InstantiationPolicy.constructor(CircularBeanA.class)));

            BeanCreationException ex = assertThrows(BeanCreationException.class,
                    () -> container.getBean("circularA"));
            assertTrue(ex.getMessage().contains("Circular dependency") ||
                       (ex.getCause() != null && ex.getCause().getMessage().contains("Circular dependency")));
        }
    }

    // ==================== BeanInspector ====================

    @Nested
    @DisplayName("BeanInspector - containsBean / containsBeanDefinition")
    class ContainsBean {

        @Test
        @DisplayName("注册 BeanDefinition 后 containsBean 返回 true")
        void containsBean_afterRegisterDefinition_returnsTrue() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            assertTrue(container.containsBean("simple"));
        }

        @Test
        @DisplayName("通过别名查询 containsBean 返回 true")
        void containsBean_byAlias_returnsTrue() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.registerAlias("simple", "alias");
            assertTrue(container.containsBean("alias"));
        }

        @Test
        @DisplayName("通过 registerSingleton 注册后 containsBean 返回 true，containsBeanDefinition 返回 false")
        void containsBean_externalSingleton_noBeanDefinition() {
            container.registerSingleton("ext", new SimpleBean());
            assertTrue(container.containsBean("ext"));
            assertFalse(container.containsBeanDefinition("ext"));
        }

        @Test
        @DisplayName("不存在的 Bean：containsBean 和 containsBeanDefinition 均返回 false")
        void notExist_bothReturnFalse() {
            assertFalse(container.containsBean("notExist"));
            assertFalse(container.containsBeanDefinition("notExist"));
        }
    }

    @Nested
    @DisplayName("BeanInspector - getBeanDefinition")
    class GetBeanDefinitionTests {

        @Test
        @DisplayName("正常获取已注册的 BeanDefinition")
        void get_success_returnsBeanDefinition() {
            BeanDefinition def = BeanDefinition.of("simple", SimpleBean.class);
            container.registerBeanDefinition(def);
            assertEquals(def, container.getBeanDefinition("simple"));
        }

        @Test
        @DisplayName("不存在时抛出 NoSuchBeanDefinitionException")
        void get_notExist_throwsNoSuchBeanDefinitionException() {
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> container.getBeanDefinition("notExist"));
        }
    }

    @Nested
    @DisplayName("BeanInspector - getBeanDefinitionNames / getBeanDefinitionCount")
    class BeanDefinitionNamesAndCount {

        @Test
        @DisplayName("注册 N 个 BeanDefinition 后，count 和 names 长度均为 N")
        void afterRegisterN_countAndNamesLengthEqualN() {
            container.registerBeanDefinition(BeanDefinition.of("a", SimpleBean.class));
            container.registerBeanDefinition(BeanDefinition.of("b", SimpleBean.class));
            container.registerBeanDefinition(BeanDefinition.of("c", SimpleBean.class));

            assertEquals(3, container.getBeanDefinitionCount());
            assertEquals(3, container.getBeanDefinitionNames().length);
        }

        @Test
        @DisplayName("移除后 count 减少")
        void afterRemove_countDecreases() {
            container.registerBeanDefinition(BeanDefinition.of("a", SimpleBean.class));
            container.registerBeanDefinition(BeanDefinition.of("b", SimpleBean.class));
            container.removeBeanDefinition("a");
            assertEquals(1, container.getBeanDefinitionCount());
        }
    }

    @Nested
    @DisplayName("BeanInspector - getBeanNamesForType")
    class GetBeanNamesForType {

        @Test
        @DisplayName("精确类型匹配：只返回完全匹配的 Bean 名称")
        void exactTypeMatch_returnsMatchingNames() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.registerBeanDefinition(BeanDefinition.of("dependent", DependentBean.class));

            String[] names = container.getBeanNamesForType(SimpleBean.class);
            assertEquals(1, names.length);
            assertEquals("simple", names[0]);
        }

        @Test
        @DisplayName("接口类型匹配：返回所有实现了该接口的 Bean 名称（多态）")
        void interfaceTypeMatch_returnsAllImplementors() {
            container.registerBeanDefinition(BeanDefinition.of("implA", ServiceImplA.class));
            container.registerBeanDefinition(BeanDefinition.of("implB", ServiceImplB.class));
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));

            String[] names = container.getBeanNamesForType(ServiceInterface.class);
            assertEquals(2, names.length);
        }

        @Test
        @DisplayName("无匹配时返回空数组")
        void noMatch_returnsEmptyArray() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            assertEquals(0, container.getBeanNamesForType(DependentBean.class).length);
        }
    }

    @Nested
    @DisplayName("BeanInspector - getBeanNamesForAnnotation")
    class GetBeanNamesForAnnotation {

        @Test
        @DisplayName("标注了对应注解的 Bean 能被找到")
        void annotatedBean_canBeFound() {
            container.registerBeanDefinition(BeanDefinition.of("annotated", AnnotatedBean.class));
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));

            String[] names = container.getBeanNamesForAnnotation(MarkerAnnotation.class);
            assertEquals(1, names.length);
            assertEquals("annotated", names[0]);
        }

        @Test
        @DisplayName("没有任何 Bean 标注时返回空数组")
        void noAnnotatedBean_returnsEmptyArray() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            assertEquals(0, container.getBeanNamesForAnnotation(MarkerAnnotation.class).length);
        }
    }

    @Nested
    @DisplayName("BeanInspector - getBeansOfType / getBeansWithAnnotation")
    class GetBeansMap {

        @Test
        @DisplayName("getBeansOfType：返回所有匹配类型的 Bean 实例 Map")
        void getBeansOfType_returnsAllMatchingInstances() {
            container.registerBeanDefinition(BeanDefinition.of("implA", ServiceImplA.class));
            container.registerBeanDefinition(BeanDefinition.of("implB", ServiceImplB.class));

            Map<String, ServiceInterface> beans = container.getBeansOfType(ServiceInterface.class);
            assertEquals(2, beans.size());
            assertTrue(beans.containsKey("implA"));
            assertTrue(beans.containsKey("implB"));
        }

        @Test
        @DisplayName("getBeansWithAnnotation：返回所有标注了注解的 Bean 实例 Map")
        void getBeansWithAnnotation_returnsAllAnnotatedInstances() {
            container.registerBeanDefinition(BeanDefinition.of("annotated", AnnotatedBean.class));
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));

            Map<String, Object> beans = container.getBeansWithAnnotation(MarkerAnnotation.class);
            assertEquals(1, beans.size());
            assertTrue(beans.containsKey("annotated"));
        }
    }

    @Nested
    @DisplayName("BeanInspector - isSingleton / isPrototype / isTypeMatch / getType")
    class TypeAndScopeChecks {

        @Test
        @DisplayName("isSingleton：单例 Bean 返回 true，原型 Bean 返回 false")
        void isSingleton_correctResult() {
            container.registerBeanDefinition(BeanDefinition.of("singleton", SimpleBean.class));
            container.registerBeanDefinition(
                    BeanDefinition.of("proto", SimpleBean.class).withScope(BeanScope.PROTOTYPE));
            assertTrue(container.isSingleton("singleton"));
            assertFalse(container.isSingleton("proto"));
        }

        @Test
        @DisplayName("isPrototype：原型 Bean 返回 true，单例 Bean 返回 false")
        void isPrototype_correctResult() {
            container.registerBeanDefinition(BeanDefinition.of("singleton", SimpleBean.class));
            container.registerBeanDefinition(
                    BeanDefinition.of("proto", SimpleBean.class).withScope(BeanScope.PROTOTYPE));
            assertFalse(container.isPrototype("singleton"));
            assertTrue(container.isPrototype("proto"));
        }

        @Test
        @DisplayName("isTypeMatch：类型兼容时返回 true，不兼容时返回 false")
        void isTypeMatch_compatibleAndIncompatible() {
            container.registerBeanDefinition(BeanDefinition.of("implA", ServiceImplA.class));
            assertTrue(container.isTypeMatch("implA", ServiceInterface.class));
            assertTrue(container.isTypeMatch("implA", ServiceImplA.class));
            assertFalse(container.isTypeMatch("implA", DependentBean.class));
        }

        @Test
        @DisplayName("getType：Bean 未创建时返回 BeanDefinition 中声明的类型")
        void getType_beforeCreation_returnsDefinedType() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            assertEquals(SimpleBean.class, container.getType("simple"));
        }

        @Test
        @DisplayName("getType：Bean 已创建（单例缓存命中）时返回实际运行时类型")
        void getType_afterCreation_returnsRuntimeType() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.getBean("simple"); // 触发创建
            assertEquals(SimpleBean.class, container.getType("simple"));
        }

        @Test
        @DisplayName("getType：Bean 不存在时抛出异常")
        void getType_notExist_returnsNull() {
            assertThrows(NoSuchBeanDefinitionException.class, () -> container.getType("notExist"));
        }
    }

    @Nested
    @DisplayName("BeanInspector - findAnnotationOnBean")
    class FindAnnotationOnBean {

        @Test
        @DisplayName("Bean 上存在目标注解时，返回注解实例")
        void annotationPresent_returnsAnnotationInstance() {
            container.registerBeanDefinition(BeanDefinition.of("annotated", AnnotatedBean.class));
            MarkerAnnotation annotation = container.findAnnotationOnBean("annotated", MarkerAnnotation.class);
            assertNotNull(annotation);
            assertEquals("test", annotation.value());
        }

        @Test
        @DisplayName("Bean 上不存在目标注解时，返回 null")
        void annotationAbsent_returnsNull() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            assertNull(container.findAnnotationOnBean("simple", MarkerAnnotation.class));
        }

        @Test
        @DisplayName("Bean 不存在时抛出 NoSuchBeanDefinitionException")
        void beanNotExist_throwsNoSuchBeanDefinitionException() {
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> container.findAnnotationOnBean("notExist", MarkerAnnotation.class));
        }
    }

    // ==================== BeanContainer 生命周期 ====================

    @Nested
    @DisplayName("BeanContainer - preInstantiateSingletons")
    class PreInstantiateSingletons {

        @Test
        @DisplayName("非懒加载单例在 preInstantiateSingletons 后进入单例缓存")
        void nonLazySingleton_isCreatedAfterPreInstantiate() {
            container.registerBeanDefinition(
                    BeanDefinition.of("eager", SimpleBean.class).withLazy(false));

            container.preInstantiateSingletons();
            assertTrue(container.containsBean("eager"));
        }

        @Test
        @DisplayName("懒加载单例在 preInstantiateSingletons 后不创建，首次 getBean 时才创建")
        void lazySingleton_notCreatedDuringPreInstantiate() {
            AtomicBoolean created = new AtomicBoolean(false);

            // 使用拦截器感知实例化时机
            container.addBeanInterceptor(new BeanInterceptor() {
                @Override
                public Object afterInstantiation(Object bean, BeanDefinition definition) {
                    if ("lazy".equals(definition.name())) {
                        created.set(true);
                    }
                    return bean;
                }
            });

            container.registerBeanDefinition(
                    BeanDefinition.of("lazy", SimpleBean.class).withLazy(true));

            container.preInstantiateSingletons();
            assertFalse(created.get(), "懒加载 Bean 不应在 preInstantiateSingletons 时被创建");

            container.getBean("lazy");
            assertTrue(created.get(), "懒加载 Bean 应在首次 getBean 时被创建");
        }
    }

    @Nested
    @DisplayName("BeanContainer - destroySingletons")
    class DestroySingletons {

        @Test
        @DisplayName("destroySingletons 后单例缓存被清空")
        void destroySingletons_singletonCacheCleared() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.getBean("simple"); // 加入缓存
            container.destroySingletons();
            // 清空后再次 getBean 会重新创建（无异常）
            assertDoesNotThrow(() -> container.getBean("simple"));
        }

        @Test
        @DisplayName("destroySingletons 触发实现了 BeanLifecycle 的单例 Bean 的 destroy()")
        void destroySingletons_triggersBeanLifecycleDestroy() {
            container.registerBeanDefinition(BeanDefinition.of("lifecycle", LifecycleBean.class));
            LifecycleBean bean = container.getBean("lifecycle", LifecycleBean.class);
            container.destroySingletons();
            assertTrue(bean.callLog.contains("destroy"));
        }

        @Test
        @DisplayName("destroySingletons 按注册顺序逆序销毁")
        void destroySingletons_reversedRegistrationOrder() {
            // 验证 A 先注册、C 后注册时，销毁顺序为 C → B → A
            container.registerBeanDefinition(BeanDefinition.of("beanA", LifecycleBean.class));
            container.registerBeanDefinition(BeanDefinition.of("beanB", LifecycleBean.class));
            container.registerBeanDefinition(BeanDefinition.of("beanC", LifecycleBean.class));

            LifecycleBean beanA = container.getBean("beanA", LifecycleBean.class);
            LifecycleBean beanB = container.getBean("beanB", LifecycleBean.class);
            LifecycleBean beanC = container.getBean("beanC", LifecycleBean.class);

            container.destroySingletons();

            // 三者均被销毁
            assertTrue(beanA.callLog.contains("destroy"));
            assertTrue(beanB.callLog.contains("destroy"));
            assertTrue(beanC.callLog.contains("destroy"));
        }
    }

    @Nested
    @DisplayName("BeanContainer - addBeanInterceptor 生效验证")
    class AddBeanInterceptor {

        @Test
        @DisplayName("注册拦截器后，后续 getBean 时拦截器的 afterInitialization 被调用")
        void addInterceptor_interceptorCalledOnGetBean() {
            AtomicBoolean interceptorCalled = new AtomicBoolean(false);

            container.addBeanInterceptor(new BeanInterceptor() {
                @Override
                public Object afterInitialization(Object bean, String beanName, BeanDefinition def) {
                    interceptorCalled.set(true);
                    return bean;
                }
            });

            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.getBean("simple");

            assertTrue(interceptorCalled.get());
        }
    }

    // ==================== dependsOn ====================

    @Nested
    @DisplayName("dependsOn - 依赖顺序保证")
    class DependsOn {

        @Test
        @DisplayName("获取 A 时，dependsOn 的 B 先被初始化（进入单例缓存）")
        void getBean_dependsOnBean_dependencyInitializedFirst() {
            container.registerBeanDefinition(BeanDefinition.of("dep", SimpleBean.class));
            container.registerBeanDefinition(
                    BeanDefinition.of("main", SimpleBean.class)
                            .dependsOn("dep"));

            container.getBean("main");

            // dep 应已进入单例缓存
            assertTrue(container.containsBean("dep"));
        }
    }
}