package org.moper.cap.bean.test;

import org.junit.jupiter.api.*;
import org.moper.cap.bean.aware.BeanClassLoaderAware;
import org.moper.cap.bean.aware.BeanFactoryAware;
import org.moper.cap.bean.aware.BeanNameAware;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.definition.BeanScope;
import org.moper.cap.bean.exception.*;
import org.moper.cap.bean.factory.BeanFactory;
import org.moper.cap.bean.factory.impl.DefaultBeanFactory;
import org.moper.cap.bean.lifecycle.DisposableBean;
import org.moper.cap.bean.lifecycle.InitializingBean;
import org.moper.cap.bean.processor.BeanPostProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DefaultBeanFactory 完整测试套件
 */
@DisplayName("DefaultBeanFactory 测试套件")
class DefaultBeanFactoryTest {

    private DefaultBeanFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DefaultBeanFactory();
    }

    // ===================== 辅助类型 =====================

    static class SimpleBean {
        String value = "default";
    }

    static class AnotherBean {
        String name = "another";
    }

    interface SomeService {
        String serve();
    }

    static class SomeServiceImpl implements SomeService {
        @Override
        public String serve() { return "ok"; }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface TestAnnotation {}

    @TestAnnotation
    static class AnnotatedBean {}

    static class InitBean implements InitializingBean {
        boolean initialized = false;
        @Override
        public void afterPropertiesSet() {
            initialized = true;
        }
    }

    static class DestroyBean implements DisposableBean {
        boolean destroyed = false;
        @Override
        public void destroy() {
            destroyed = true;
        }
    }

    static class CustomLifecycleBean {
        boolean inited = false;
        boolean destroyed = false;
        void init() { inited = true; }
        void close() { destroyed = true; }
    }

    static class AwareBean implements BeanNameAware, BeanFactoryAware, BeanClassLoaderAware {
        String beanName;
        BeanFactory beanFactory;
        ClassLoader classLoader;

        @Override
        public void setBeanName(String beanName) { this.beanName = beanName; }
        @Override
        public void setBeanFactory(BeanFactory beanFactory) { this.beanFactory = beanFactory; }
        @Override
        public void setBeanClassLoader(ClassLoader classLoader) { this.classLoader = classLoader; }
    }

    static class DependentBean {
        SomeServiceImpl service;  // 按类型注入
    }

    static class PrimaryBean implements SomeService {
        @Override public String serve() { return "primary"; }
    }

    static class SecondaryBean implements SomeService {
        @Override public String serve() { return "secondary"; }
    }

    /** 静态工厂方法使用的Bean */
    static class StaticFactoryProduct {
        static StaticFactoryProduct create() { return new StaticFactoryProduct(); }
    }

    /** 实例工厂Bean */
    static class InstanceFactory {
        SimpleBean produce() { return new SimpleBean(); }
    }

    // ===================== 辅助方法 =====================

    private BeanDefinition singletonDef(String name, Class<?> type) {
        return new BeanDefinition(name, type, BeanScope.SINGLETON,
                null, null, new Class<?>[0],
                false, false, true,
                null, null, new String[0], "");
    }

    private BeanDefinition prototypeDef(String name, Class<?> type) {
        return new BeanDefinition(name, type, BeanScope.PROTOTYPE,
                null, null, new Class<?>[0],
                false, false, true,
                null, null, new String[0], "");
    }

    private BeanDefinition primaryDef(String name, Class<?> type) {
        return new BeanDefinition(name, type, BeanScope.SINGLETON,
                null, null, new Class<?>[0],
                false, true, true,
                null, null, new String[0], "");
    }

    // ===================== 注册与基本查询 =====================

    @Nested
    @DisplayName("Bean 定义注册")
    class RegistrationTests {

        @Test
        @DisplayName("注册并查询BeanDefinition")
        void testRegisterAndQuery() throws BeanException {
            factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class));

            assertTrue(factory.containsBeanDefinition("simple"));
            assertEquals(SimpleBean.class, factory.getBeanDefinition("simple").type());
            assertEquals(1, factory.getBeanDefinitionCount());
        }

        @Test
        @DisplayName("重复注册同名Bean抛出异常")
        void testDuplicateRegistration() throws BeanException {
            factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class));
            assertThrows(BeanDefinitionStoreException.class,
                    () -> factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class)));
        }

        @Test
        @DisplayName("移除BeanDefinition")
        void testRemoveBeanDefinition() throws BeanException {
            factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class));
            factory.removeBeanDefinition("simple");
            assertFalse(factory.containsBeanDefinition("simple"));
        }

        @Test
        @DisplayName("移除不存在的Bean抛出异常")
        void testRemoveNonExistent() {
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> factory.removeBeanDefinition("notExist"));
        }

        @Test
        @DisplayName("注册外部单例对象")
        void testRegisterSingleton() throws BeanException {
            SimpleBean bean = new SimpleBean();
            factory.registerSingleton("manual", bean);

            assertTrue(factory.containsBean("manual"));
            assertSame(bean, factory.getBean("manual"));
        }

        @Test
        @DisplayName("isBeanNameInUse 检查名称占用")
        void testIsBeanNameInUse() throws BeanException {
            assertFalse(factory.isBeanNameInUse("foo"));
            factory.registerBeanDefinition(singletonDef("foo", SimpleBean.class));
            assertTrue(factory.isBeanNameInUse("foo"));
        }
    }

    // ===================== getBean =====================

    @Nested
    @DisplayName("getBean 获取Bean")
    class GetBeanTests {

        @Test
        @DisplayName("按名称获取单例Bean")
        void testGetBeanByName() throws BeanException {
            factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class));
            Object bean = factory.getBean("simple");
            assertNotNull(bean);
            assertInstanceOf(SimpleBean.class, bean);
        }

        @Test
        @DisplayName("单例Bean每次返回同一实例")
        void testSingletonReturnsSameInstance() throws BeanException {
            factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class));
            Object b1 = factory.getBean("simple");
            Object b2 = factory.getBean("simple");
            assertSame(b1, b2);
        }

        @Test
        @DisplayName("原型Bean每次返回不同实例")
        void testPrototypeReturnsDifferentInstance() throws BeanException {
            factory.registerBeanDefinition(prototypeDef("proto", SimpleBean.class));
            Object b1 = factory.getBean("proto");
            Object b2 = factory.getBean("proto");
            assertNotSame(b1, b2);
        }

        @Test
        @DisplayName("按名称+类型获取Bean")
        void testGetBeanByNameAndType() throws BeanException {
            factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class));
            SimpleBean bean = factory.getBean("simple", SimpleBean.class);
            assertNotNull(bean);
        }

        @Test
        @DisplayName("按类型获取唯一Bean")
        void testGetBeanByType() throws BeanException {
            factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class));
            SimpleBean bean = factory.getBean(SimpleBean.class);
            assertNotNull(bean);
        }

        @Test
        @DisplayName("按类型获取primary Bean")
        void testGetBeanByTypeWithPrimary() throws BeanException {
            factory.registerBeanDefinition(new BeanDefinition(
                    "primary", PrimaryBean.class, BeanScope.SINGLETON,
                    null, null, new Class<?>[0], false, true, true,
                    null, null, new String[0], ""));
            factory.registerBeanDefinition(singletonDef("secondary", SecondaryBean.class));

            SomeService service = factory.getBean(SomeService.class);
            assertInstanceOf(PrimaryBean.class, service);
        }

        @Test
        @DisplayName("不存在的Bean抛出异常")
        void testGetNonExistentBean() {
            assertThrows(NoSuchBeanDefinitionException.class,
                    () -> factory.getBean("notExist"));
        }

        @Test
        @DisplayName("类型不匹配抛出异常")
        void testGetBeanWrongType() throws BeanException {
            factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class));
            assertThrows(BeanNotOfRequiredTypeException.class,
                    () -> factory.getBean("simple", AnotherBean.class));
        }

        @Test
        @DisplayName("多个同类型无primary抛出NoUniqueBeanDefinitionException")
        void testNoUniqueBeanDefinition() throws BeanException {
            factory.registerBeanDefinition(singletonDef("s1", SecondaryBean.class));
            factory.registerBeanDefinition(singletonDef("s2", SecondaryBean.class));
            assertThrows(NoUniqueBeanDefinitionException.class,
                    () -> factory.getBean(SomeService.class));
        }
    }

    // ===================== 类型/作用域查询 =====================

    @Nested
    @DisplayName("类型与作用域查询")
    class TypeAndScopeTests {

        @Test
        @DisplayName("isSingleton / isPrototype")
        void testScopeChecks() throws BeanException {
            factory.registerBeanDefinition(singletonDef("s", SimpleBean.class));
            factory.registerBeanDefinition(prototypeDef("p", SimpleBean.class));

            assertTrue(factory.isSingleton("s"));
            assertFalse(factory.isPrototype("s"));
            assertFalse(factory.isSingleton("p"));
            assertTrue(factory.isPrototype("p"));
        }

        @Test
        @DisplayName("getType 返回正确类型")
        void testGetType() throws BeanException {
            factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class));
            assertEquals(SimpleBean.class, factory.getType("simple"));
        }

        @Test
        @DisplayName("isTypeMatch 类型匹配检测")
        void testIsTypeMatch() throws BeanException {
            factory.registerBeanDefinition(singletonDef("impl", SomeServiceImpl.class));
            assertTrue(factory.isTypeMatch("impl", SomeService.class));
            assertFalse(factory.isTypeMatch("impl", SimpleBean.class));
        }

        @Test
        @DisplayName("getBeanNamesForType 按类型查找名称")
        void testGetBeanNamesForType() throws BeanException {
            factory.registerBeanDefinition(singletonDef("s1", SomeServiceImpl.class));
            factory.registerBeanDefinition(singletonDef("other", SimpleBean.class));

            String[] names = factory.getBeanNamesForType(SomeService.class);
            assertEquals(1, names.length);
            assertEquals("s1", names[0]);
        }
    }

    // ===================== 别名 =====================

    @Nested
    @DisplayName("别名")
    class AliasTests {

        @Test
        @DisplayName("注册别名后可以通过别名获取Bean")
        void testAliasResolution() throws BeanException {
            factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class));
            factory.registerAlias("simple", "myAlias");

            assertNotNull(factory.getBean("myAlias"));
            assertArrayEquals(new String[]{"myAlias"}, factory.getAliases("simple"));
        }

        @Test
        @DisplayName("重复注册别名抛出异常")
        void testDuplicateAlias() throws BeanException {
            factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class));
            factory.registerAlias("simple", "alias1");
            assertThrows(BeanDefinitionStoreException.class,
                    () -> factory.registerAlias("simple", "alias1"));
        }
    }

    // ===================== 注解查询 =====================

    @Nested
    @DisplayName("注解查询")
    class AnnotationTests {

        @Test
        @DisplayName("getBeanNamesForAnnotation 按注解查找Bean名称")
        void testGetBeanNamesForAnnotation() throws BeanException {
            factory.registerBeanDefinition(singletonDef("annotated", AnnotatedBean.class));
            factory.registerBeanDefinition(singletonDef("plain", SimpleBean.class));

            String[] names = factory.getBeanNamesForAnnotation(TestAnnotation.class);
            assertEquals(1, names.length);
            assertEquals("annotated", names[0]);
        }

        @Test
        @DisplayName("getBeansWithAnnotation 按注解获取Bean实例")
        void testGetBeansWithAnnotation() throws BeanException {
            factory.registerBeanDefinition(singletonDef("annotated", AnnotatedBean.class));
            Map<String, Object> beans = factory.getBeansWithAnnotation(TestAnnotation.class);
            assertEquals(1, beans.size());
            assertTrue(beans.containsKey("annotated"));
        }

        @Test
        @DisplayName("findAnnotationOnBean 查找Bean上的注解")
        void testFindAnnotationOnBean() throws BeanException {
            factory.registerBeanDefinition(singletonDef("annotated", AnnotatedBean.class));
            TestAnnotation ann = factory.findAnnotationOnBean("annotated", TestAnnotation.class);
            assertNotNull(ann);
        }
    }

    // ===================== 生命周期 =====================

    @Nested
    @DisplayName("生命周期回调")
    class LifecycleTests {

        @Test
        @DisplayName("InitializingBean.afterPropertiesSet 被调用")
        void testInitializingBean() throws BeanException {
            factory.registerBeanDefinition(singletonDef("init", InitBean.class));
            InitBean bean = factory.getBean("init", InitBean.class);
            assertTrue(bean.initialized);
        }

        @Test
        @DisplayName("DisposableBean.destroy 被调用")
        void testDisposableBean() throws BeanException {
            factory.registerBeanDefinition(singletonDef("destroy", DestroyBean.class));
            DestroyBean bean = factory.getBean("destroy", DestroyBean.class);
            assertFalse(bean.destroyed);

            factory.destroySingletons();
            assertTrue(bean.destroyed);
        }

        @Test
        @DisplayName("自定义 initMethodName 和 destroyMethodName 被调用")
        void testCustomLifecycleMethods() throws BeanException {
            BeanDefinition def = new BeanDefinition(
                    "lifecycle", CustomLifecycleBean.class, BeanScope.SINGLETON,
                    null, null, new Class<?>[0], false, false, true,
                    "init", "close", new String[0], "");
            factory.registerBeanDefinition(def);

            CustomLifecycleBean bean = factory.getBean("lifecycle", CustomLifecycleBean.class);
            assertTrue(bean.inited);

            factory.destroySingletons();
            assertTrue(bean.destroyed);
        }
    }

    // ===================== Aware 接口 =====================

    @Nested
    @DisplayName("Aware 接口回调")
    class AwareTests {

        @Test
        @DisplayName("BeanNameAware / BeanFactoryAware / BeanClassLoaderAware 均被回调")
        void testAwareCallbacks() throws BeanException {
            factory.registerBeanDefinition(singletonDef("aware", AwareBean.class));
            AwareBean bean = factory.getBean("aware", AwareBean.class);

            assertEquals("aware", bean.beanName);
            assertSame(factory, bean.beanFactory);
            assertNotNull(bean.classLoader);
        }
    }

    // ===================== BeanPostProcessor =====================

    @Nested
    @DisplayName("BeanPostProcessor 后置处理器")
    class PostProcessorTests {

        @Test
        @DisplayName("后置处理器的 before/after 方法都被调用")
        void testPostProcessor() throws BeanException {
            AtomicBoolean beforeCalled = new AtomicBoolean(false);
            AtomicBoolean afterCalled = new AtomicBoolean(false);

            factory.addBeanPostProcessor(new BeanPostProcessor() {
                @Override
                public Object postProcessBeforeInitialization(Object bean, String beanName) {
                    beforeCalled.set(true);
                    return bean;
                }
                @Override
                public Object postProcessAfterInitialization(Object bean, String beanName) {
                    afterCalled.set(true);
                    return bean;
                }
            });

            factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class));
            factory.getBean("simple");

            assertTrue(beforeCalled.get());
            assertTrue(afterCalled.get());
        }
    }

    // ===================== 工厂方法实例化 =====================

    @Nested
    @DisplayName("工厂方法实例化")
    class FactoryMethodTests {

        @Test
        @DisplayName("静态工厂方法实例化")
        void testStaticFactoryMethod() throws BeanException {
            BeanDefinition def = new BeanDefinition(
                    "product", StaticFactoryProduct.class, BeanScope.SINGLETON,
                    null, "create", new Class<?>[0], false, false, true,
                    null, null, new String[0], "");
            factory.registerBeanDefinition(def);

            Object bean = factory.getBean("product");
            assertInstanceOf(StaticFactoryProduct.class, bean);
        }

        @Test
        @DisplayName("实例工厂方法实例化")
        void testInstanceFactoryMethod() throws BeanException {
            factory.registerBeanDefinition(singletonDef("instanceFactory", InstanceFactory.class));
            BeanDefinition def = new BeanDefinition(
                    "product", SimpleBean.class, BeanScope.SINGLETON,
                    "instanceFactory", "produce", new Class<?>[0], false, false, true,
                    null, null, new String[0], "");
            factory.registerBeanDefinition(def);

            Object bean = factory.getBean("product");
            assertInstanceOf(SimpleBean.class, bean);
        }
    }

    // ===================== AutowireCapableBeanFactory =====================

    @Nested
    @DisplayName("AutowireCapableBeanFactory")
    class AutowireTests {

        @Test
        @DisplayName("createBean 创建并初始化Bean（含Aware回调）")
        void testCreateBean() throws BeanException {
            factory.registerBeanDefinition(singletonDef("svc", SomeServiceImpl.class));
            AwareBean bean = factory.createBean(AwareBean.class);
            assertNotNull(bean);
            // AwareBean 实现了 BeanFactoryAware，工厂引用应被注入
            assertSame(factory, bean.beanFactory);
        }

        @Test
        @DisplayName("resolveDependency 按类型解析依赖")
        void testResolveDependency() throws BeanException {
            factory.registerBeanDefinition(singletonDef("svc", SomeServiceImpl.class));
            Object resolved = factory.resolveDependency(SomeService.class, null);
            assertNotNull(resolved);
            assertInstanceOf(SomeServiceImpl.class, resolved);
        }

        @Test
        @DisplayName("resolveDependency 对不存在类型返回null")
        void testResolveDependencyNotFound() throws BeanException {
            Object resolved = factory.resolveDependency(SimpleBean.class, null);
            assertNull(resolved);
        }

        @Test
        @DisplayName("destroyBean 销毁单个Bean")
        void testDestroyBean() throws BeanException {
            DestroyBean bean = new DestroyBean();
            factory.destroyBean(bean);
            assertTrue(bean.destroyed);
        }
    }

    // ===================== 配置冻结 =====================

    @Nested
    @DisplayName("配置冻结")
    class FreezeTests {

        @Test
        @DisplayName("冻结后不允许注册BeanDefinition")
        void testFrozenRegistration() throws BeanException {
            factory.freezeConfiguration();
            assertTrue(factory.isConfigurationFrozen());
            assertThrows(BeanFactoryConfigurationFrozenException.class,
                    () -> factory.registerBeanDefinition(singletonDef("simple", SimpleBean.class)));
        }

        @Test
        @DisplayName("冻结后不允许添加BeanPostProcessor")
        void testFrozenAddProcessor() throws BeanException {
            factory.freezeConfiguration();
            assertThrows(BeanFactoryConfigurationFrozenException.class,
                    () -> factory.addBeanPostProcessor(new BeanPostProcessor() {}));
        }
    }

    // ===================== preInstantiateSingletons =====================

    @Test
    @DisplayName("preInstantiateSingletons 预实例化非懒加载单例")
    void testPreInstantiateSingletons() throws BeanException {
        factory.registerBeanDefinition(singletonDef("s1", SimpleBean.class));
        factory.registerBeanDefinition(new BeanDefinition(
                "lazy", SimpleBean.class, BeanScope.SINGLETON,
                null, null, new Class<?>[0], true, false, true,
                null, null, new String[0], ""));

        factory.preInstantiateSingletons();

        // s1 已被预实例化（singleton cache 中有）
        assertTrue(factory.containsBean("s1"));
    }

    // ===================== getBeansOfType =====================

    @Test
    @DisplayName("getBeansOfType 按类型获取所有Bean实例")
    void testGetBeansOfType() throws BeanException {
        factory.registerBeanDefinition(singletonDef("s1", SomeServiceImpl.class));
        factory.registerBeanDefinition(singletonDef("plain", SimpleBean.class));

        Map<String, SomeService> beans = factory.getBeansOfType(SomeService.class);
        assertEquals(1, beans.size());
        assertTrue(beans.containsKey("s1"));
    }
}
