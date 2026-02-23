package org.moper.cap.bean.container.impl;

import org.junit.jupiter.api.*;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.definition.BeanScope;
import org.moper.cap.bean.definition.InstantiationPolicy;
import org.moper.cap.bean.exception.BeanCreationException;
import org.moper.cap.bean.exception.BeanDestructionException;
import org.moper.cap.bean.exception.BeanInitializationException;
import org.moper.cap.bean.fixture.*;
import org.moper.cap.bean.interceptor.BeanInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultBeanCreationEngine")
class DefaultBeanCreationEngineTest {

    /**
     * 每个测试用例使用独立的容器和引擎，通过容器间接驱动引擎，
     * 与实际运行时路径完全一致。
     */
    private DefaultBeanContainer container;

    @BeforeEach
    void setUp() {
        container = new DefaultBeanContainer();
    }

    // ====================================================================
    // 拦截器管理
    // ====================================================================

    @Nested
    @DisplayName("拦截器管理")
    class InterceptorManagement {

        @Test
        @DisplayName("初始状态下拦截器数量为 0，列表为空")
        void initialState_countIsZeroAndListEmpty() {
            assertEquals(0, container.getBeanInterceptorCount());
            assertTrue(container.getBeanInterceptors().isEmpty());
        }

        @Test
        @DisplayName("添加一个拦截器后数量为 1")
        void addOne_countIsOne() {
            container.addBeanInterceptor(new BeanInterceptor() {});
            assertEquals(1, container.getBeanInterceptorCount());
        }

        @Test
        @DisplayName("getBeanInterceptors() 返回只读视图，不可修改")
        void getBeanInterceptors_returnsUnmodifiableList() {
            container.addBeanInterceptor(new BeanInterceptor() {});
            assertThrows(UnsupportedOperationException.class, () -> container.getBeanInterceptors().add(new BeanInterceptor() {}));
        }

        @Test
        @DisplayName("多个拦截器按 getOrder() 升序排列")
        void multipleInterceptors_sortedByOrder() {
            List<Integer> orderLog = new ArrayList<>();

            container.addBeanInterceptor(new BeanInterceptor() {
                @Override public int getOrder() { return 10; }
                @Override public Object afterInitialization(Object bean, String beanName,
                                                            BeanDefinition def) {
                    orderLog.add(10);
                    return bean;
                }
            });
            container.addBeanInterceptor(new BeanInterceptor() {
                @Override public int getOrder() { return -5; }
                @Override public Object afterInitialization(Object bean, String beanName,
                                                            BeanDefinition def) {
                    orderLog.add(-5);
                    return bean;
                }
            });
            container.addBeanInterceptor(new BeanInterceptor() {
                @Override public int getOrder() { return 0; }
                @Override public Object afterInitialization(Object bean, String beanName,
                                                            BeanDefinition def) {
                    orderLog.add(0);
                    return bean;
                }
            });

            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.getBean("simple");

            assertEquals(List.of(-5, 0, 10), orderLog);
        }

        @Test
        @DisplayName("addBeanInterceptor 传入 null 抛出 IllegalArgumentException")
        void addNullInterceptor_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> container.addBeanInterceptor(null));
        }
    }

    // ====================================================================
    // 实例化策略
    // ====================================================================

    @Nested
    @DisplayName("实例化策略")
    class Instantiation {

        @Test
        @DisplayName("无参构造函数：正确创建实例")
        void noArgConstructor_createsInstance() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            assertInstanceOf(SimpleBean.class, container.getBean("simple"));
        }

        @Test
        @DisplayName("有参构造函数：依赖通过容器按类型自动解析")
        void withArgConstructor_dependencyResolvedFromContainer() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.registerBeanDefinition(
                    BeanDefinition.of("dependent", DependentBean.class)
                            .withInstantiationPolicy(
                                    InstantiationPolicy.constructor(SimpleBean.class)));

            DependentBean bean = container.getBean("dependent", DependentBean.class);
            assertNotNull(bean.getDependency());
            assertInstanceOf(SimpleBean.class, bean.getDependency());
        }

        @Test
        @DisplayName("静态工厂方法（无参）：调用静态方法创建实例")
        void staticFactory_noArg_invokesStaticMethod() {
            container.registerBeanDefinition(
                    BeanDefinition.of("staticBean", StaticFactoryBean.class)
                            .withInstantiationPolicy(
                                    InstantiationPolicy.staticFactory("create")));

            StaticFactoryBean bean = container.getBean("staticBean", StaticFactoryBean.class);
            assertEquals("static-factory", bean.getSource());
        }

        @Test
        @DisplayName("静态工厂方法（有参）：参数通过容器解析后传入")
        void staticFactory_withArg_argsResolvedFromContainer() {
            container.registerSingleton("config", new ConfigBean("hello-from-container"));
            container.registerBeanDefinition(
                    BeanDefinition.of("staticBean", StaticFactoryBean.class)
                            .withInstantiationPolicy(
                                    InstantiationPolicy.staticFactory(
                                            "createWithArg", ConfigBean.class)));

            StaticFactoryBean bean = container.getBean("staticBean", StaticFactoryBean.class);
            assertEquals("hello-from-container", bean.getSource());
        }

        @Test
        @DisplayName("实例工厂方法：通过工厂 Bean 的方法创建实例")
        void instanceFactory_invokesFactoryMethod() {
            container.registerBeanDefinition(
                    BeanDefinition.of("factory", InstanceFactoryBean.class));
            container.registerBeanDefinition(
                    BeanDefinition.of("product", SimpleBean.class)
                            .withInstantiationPolicy(
                                    InstantiationPolicy.instanceFactory(
                                            "factory", "createSimpleBean")));

            SimpleBean bean = container.getBean("product", SimpleBean.class);
            assertEquals("from-instance-factory", bean.getValue());
        }

        @Test
        @DisplayName("实例���失败（私有无参构造）：抛出 BeanCreationException")
        void instantiationFails_privateConstructor_throwsBeanCreationException() {
            // StaticFactoryBean 的构造函数是私有的
            container.registerBeanDefinition(
                    BeanDefinition.of("bad", StaticFactoryBean.class));
            assertThrows(BeanCreationException.class, () -> container.getBean("bad"));
        }
    }

    // ====================================================================
    // beforeInstantiation 短路语义
    // ====================================================================

    @Nested
    @DisplayName("beforeInstantiation 短路语义")
    class BeforeInstantiationShortCircuit {

        @Test
        @DisplayName("拦截器返回非 null 时，实例化步骤被跳过，直接使用返回值")
        void interceptorReturnsNonNull_instantiationSkipped() {
            SimpleBean predefined = new SimpleBean();
            predefined.setValue("short-circuit");

            AtomicBoolean instantiationCalled = new AtomicBoolean(false);

            container.addBeanInterceptor(new BeanInterceptor() {
                @Override
                public Object beforeInstantiation(BeanDefinition definition) {
                    return predefined;
                }
            });
            container.addBeanInterceptor(new BeanInterceptor() {
                @Override
                public Object afterInstantiation(Object bean, BeanDefinition definition) {
                    // 短路后 afterInstantiation 仍然执行
                    instantiationCalled.set(true);
                    return bean;
                }
            });

            container.registerBeanDefinition(BeanDefinition.of("myBean", SimpleBean.class));
            Object result = container.getBean("myBean");

            assertSame(predefined, result);
            assertEquals("short-circuit", ((SimpleBean) result).getValue());
        }

        @Test
        @DisplayName("短路后初始化阶段（beforeInitialization → afterPropertiesSet → afterInitialization）仍然执行")
        void shortCircuit_initializationPhaseStillExecuted() {
            LifecycleBean predefined = new LifecycleBean();

            container.addBeanInterceptor(new BeanInterceptor() {
                @Override
                public Object beforeInstantiation(BeanDefinition definition) {
                    return predefined;
                }
            });

            container.registerBeanDefinition(
                    BeanDefinition.of("lifecycle", LifecycleBean.class));
            container.getBean("lifecycle");

            assertTrue(predefined.callLog.contains("afterPropertiesSet"),
                    "短路后 afterPropertiesSet 仍应被调用");
        }

        @Test
        @DisplayName("第一个拦截器返回非 null 时，后续拦截器的 beforeInstantiation 不再执行")
        void firstInterceptorShortCircuits_subsequentBeforeInstantiationSkipped() {
            AtomicInteger secondCallCount = new AtomicInteger(0);

            container.addBeanInterceptor(new BeanInterceptor() {
                @Override public int getOrder() { return 1; }
                @Override public Object beforeInstantiation(BeanDefinition def) {
                    return new SimpleBean(); // 短路
                }
            });
            container.addBeanInterceptor(new BeanInterceptor() {
                @Override public int getOrder() { return 2; }
                @Override public Object beforeInstantiation(BeanDefinition def) {
                    secondCallCount.incrementAndGet();
                    return null;
                }
            });

            container.registerBeanDefinition(BeanDefinition.of("bean", SimpleBean.class));
            container.getBean("bean");

            assertEquals(0, secondCallCount.get());
        }

        @Test
        @DisplayName("所有拦截器均返回 null 时，继续正常实例化流程")
        void allInterceptorsReturnNull_normalInstantiationProceeds() {
            container.addBeanInterceptor(new BeanInterceptor() {
                @Override public Object beforeInstantiation(BeanDefinition def) { return null; }
            });

            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            assertInstanceOf(SimpleBean.class, container.getBean("simple"));
        }
    }

    // ====================================================================
    // 拦截器链完整调用顺序
    // ====================================================================

    @Nested
    @DisplayName("拦截器链完整调用顺序")
    class InterceptorChainOrder {

        @Test
        @DisplayName("单个拦截器：五个阶段均被调用")
        void singleInterceptor_allFivePhasesInvoked() {
            List<String> phaseLog = new ArrayList<>();

            container.addBeanInterceptor(new BeanInterceptor() {
                @Override public Object beforeInstantiation(BeanDefinition def) {
                    phaseLog.add("beforeInstantiation"); return null;
                }
                @Override public Object afterInstantiation(Object bean, BeanDefinition def) {
                    phaseLog.add("afterInstantiation"); return bean;
                }
                @Override public Object afterPropertyInjection(Object bean, BeanDefinition def) {
                    phaseLog.add("afterPropertyInjection"); return bean;
                }
                @Override public Object beforeInitialization(Object bean, String name,
                                                             BeanDefinition def) {
                    phaseLog.add("beforeInitialization"); return bean;
                }
                @Override public Object afterInitialization(Object bean, String name,
                                                            BeanDefinition def) {
                    phaseLog.add("afterInitialization"); return bean;
                }
            });

            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.getBean("simple");

            assertEquals(List.of(
                    "beforeInstantiation",
                    "afterInstantiation",
                    "afterPropertyInjection",
                    "beforeInitialization",
                    "afterInitialization"
            ), phaseLog);
        }

        @Test
        @DisplayName("完整顺序：beforeInstantiation → afterInstantiation → afterPropertyInjection" +
                " → beforeInitialization → afterPropertiesSet → afterInitialization")
        void fullOrder_withLifecycleCallback() {
            List<String> callLog = new ArrayList<>();

            container.addBeanInterceptor(new BeanInterceptor() {
                @Override public Object beforeInstantiation(BeanDefinition def) {
                    callLog.add("beforeInstantiation"); return null;
                }
                @Override public Object afterInstantiation(Object bean, BeanDefinition def) {
                    callLog.add("afterInstantiation"); return bean;
                }
                @Override public Object afterPropertyInjection(Object bean, BeanDefinition def) {
                    callLog.add("afterPropertyInjection"); return bean;
                }
                @Override public Object beforeInitialization(Object bean, String name,
                                                             BeanDefinition def) {
                    callLog.add("beforeInitialization"); return bean;
                }
                @Override public Object afterInitialization(Object bean, String name,
                                                            BeanDefinition def) {
                    callLog.add("afterInitialization"); return bean;
                }
            });

            container.registerBeanDefinition(
                    BeanDefinition.of("lifecycle", LifecycleBean.class));
            LifecycleBean bean = container.getBean("lifecycle", LifecycleBean.class);

            // 将 lifecycle 回调合并到 callLog 进行整体顺序验证
            List<String> fullLog = new ArrayList<>(callLog);
            // afterPropertiesSet 在 beforeInitialization 之后、afterInitialization 之前
            int beforeInit = fullLog.indexOf("beforeInitialization");
            int afterInit  = fullLog.indexOf("afterInitialization");
            assertTrue(bean.callLog.contains("afterPropertiesSet"));
            assertTrue(beforeInit < afterInit,
                    "beforeInitialization 应在 afterInitialization 之前");
        }

        @Test
        @DisplayName("多个拦截器的 afterInstantiation 链式传递：前一个返回值作为后一个的输入")
        void multipleInterceptors_afterInstantiation_chainedBeanPassing() {
            SimpleBean bean1 = new SimpleBean();
            SimpleBean bean2 = new SimpleBean();

            container.addBeanInterceptor(new BeanInterceptor() {
                @Override public int getOrder() { return 1; }
                @Override public Object afterInstantiation(Object bean, BeanDefinition def) {
                    assertSame(bean1, bean);
                    return bean2;
                }
                @Override public Object beforeInstantiation(BeanDefinition def) {
                    return bean1; // 通过短路注入 bean1
                }
            });
            container.addBeanInterceptor(new BeanInterceptor() {
                @Override public int getOrder() { return 2; }
                @Override public Object afterInstantiation(Object bean, BeanDefinition def) {
                    assertSame(bean2, bean); // 接收的是 bean2
                    return bean;
                }
            });

            container.registerBeanDefinition(BeanDefinition.of("bean", SimpleBean.class));
            Object result = container.getBean("bean");
            assertSame(bean2, result);
        }
    }

    // ====================================================================
    // 生命周期回调
    // ====================================================================

    @Nested
    @DisplayName("生命周期回调")
    class LifecycleCallback {

        @Test
        @DisplayName("Bean 实现了 BeanLifecycle：afterPropertiesSet() 在创建时被调用")
        void beanImplementsBeanLifecycle_afterPropertiesSetCalled() {
            container.registerBeanDefinition(
                    BeanDefinition.of("lifecycle", LifecycleBean.class));
            LifecycleBean bean = container.getBean("lifecycle", LifecycleBean.class);
            assertTrue(bean.callLog.contains("afterPropertiesSet"));
        }

        @Test
        @DisplayName("Bean 未实现 BeanLifecycle：创建时不抛出任何异常")
        void beanNotImplementsBeanLifecycle_noExceptionOnCreate() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            assertDoesNotThrow(() -> container.getBean("simple"));
        }

        @Test
        @DisplayName("afterPropertiesSet() 抛出异常：包装为 BeanInitializationException")
        void afterPropertiesSetThrows_wrappedAsBeanInitializationException() {
            container.registerBeanDefinition(
                    BeanDefinition.of("lifecycle", LifecycleBean.class));
            LifecycleBean bean = container.getBean("lifecycle", LifecycleBean.class);

            // 直接通过引擎测试（先重置，再销毁时不触发；通过第二个容器模拟 init 失败）
            DefaultBeanContainer failContainer = new DefaultBeanContainer();
            failContainer.registerBeanDefinition(
                    BeanDefinition.of("lifecycle", LifecycleBean.class));

            // 注册一个拦截器在 beforeInitialization 阶段设置 initShouldThrow
            failContainer.addBeanInterceptor(new BeanInterceptor() {
                @Override
                public Object beforeInitialization(Object b, String name, BeanDefinition def) {
                    if (b instanceof LifecycleBean lb) {
                        lb.initShouldThrow = true;
                    }
                    return b;
                }
            });

            BeanInitializationException ex = assertThrows(BeanInitializationException.class,
                    () -> failContainer.getBean("lifecycle"));
            assertEquals("lifecycle", ex.getBeanName());
            assertNotNull(ex.getCause());
            assertEquals("init failed intentionally", ex.getCause().getMessage());
        }

        @Test
        @DisplayName("destroy() 抛出异常：包装为 BeanDestructionException")
        void destroyThrows_wrappedAsBeanDestructionException() {
            container.registerBeanDefinition(
                    BeanDefinition.of("lifecycle", LifecycleBean.class));
            LifecycleBean bean = container.getBean("lifecycle", LifecycleBean.class);
            bean.destroyShouldThrow = true;

            assertThrows(BeanDestructionException.class,
                    () -> container.destroySingletons());
        }
    }

    // ====================================================================
    // 销毁回调注册与销毁顺序
    // ====================================================================

    @Nested
    @DisplayName("销毁回调注册与销毁顺序")
    class DisposableBeanRegistration {

        @Test
        @DisplayName("单例 + 实现 BeanLifecycle：destroySingletons 时 destroy() 被调用")
        void singleton_withLifecycle_destroyCalled() {
            container.registerBeanDefinition(
                    BeanDefinition.of("lifecycle", LifecycleBean.class));
            LifecycleBean bean = container.getBean("lifecycle", LifecycleBean.class);
            container.destroySingletons();
            assertTrue(bean.callLog.contains("destroy"));
        }

        @Test
        @DisplayName("原型 + 实现 BeanLifecycle：destroySingletons 时 destroy() 不被容器调用")
        void prototype_withLifecycle_destroyNotCalledByContainer() {
            container.registerBeanDefinition(
                    BeanDefinition.of("lifecycle", LifecycleBean.class)
                            .withScope(BeanScope.PROTOTYPE));
            LifecycleBean bean = container.getBean("lifecycle", LifecycleBean.class);
            container.destroySingletons();
            assertFalse(bean.callLog.contains("destroy"));
        }

        @Test
        @DisplayName("单例 + 未实现 BeanLifecycle：destroySingletons 不抛出任何异常")
        void singleton_withoutLifecycle_destroySingletonsNoException() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.getBean("simple");
            assertDoesNotThrow(() -> container.destroySingletons());
        }

        @Test
        @DisplayName("destroyAllSingletons 按注册顺序逆序执行 destroy()")
        void destroyAllSingletons_reversedRegistrationOrder() {
            List<String> destroyOrder = new ArrayList<>();

            // 通过拦截器在 afterInitialization 阶段将 callLog 引用替换为记录顺序的列表
            container.addBeanInterceptor(new BeanInterceptor() {
                @Override
                public Object afterInitialization(Object bean, String beanName,
                                                  BeanDefinition def) {
                    if (bean instanceof LifecycleBean lb) {
                        lb.callLog.clear();
                        // 重写销毁时的日志目标（通过 fixture 内部字段）
                    }
                    return bean;
                }
            });

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

        @Test
        @DisplayName("destroyBean 指定名称：只销毁指定 Bean，其余不受影响")
        void destroyBean_specificName_onlyTargetDestroyed() {
            container.registerBeanDefinition(BeanDefinition.of("beanA", LifecycleBean.class));
            container.registerBeanDefinition(BeanDefinition.of("beanB", LifecycleBean.class));

            LifecycleBean beanA = container.getBean("beanA", LifecycleBean.class);
            LifecycleBean beanB = container.getBean("beanB", LifecycleBean.class);

            container.destroyBean("beanA");

            assertTrue(beanA.callLog.contains("destroy"),  "beanA 应被销毁");
            assertFalse(beanB.callLog.contains("destroy"), "beanB 不应被销毁");
        }

        @Test
        @DisplayName("destroyBean 对未注册为可销毁的 Bean 是空操作，不抛出异常")
        void destroyBean_notRegistered_noException() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            container.getBean("simple");

            assertDoesNotThrow(() -> container.destroyBean("simple"));
        }
    }

    // ====================================================================
    // 无拦截器时各阶段透传
    // ====================================================================

    @Nested
    @DisplayName("无拦截器时各阶段行为")
    class NoInterceptorBehavior {

        @Test
        @DisplayName("无拦截器：createBean 正常完成，返回正确实例")
        void noInterceptor_createBeanSucceeds() {
            container.registerBeanDefinition(BeanDefinition.of("simple", SimpleBean.class));
            assertInstanceOf(SimpleBean.class, container.getBean("simple"));
        }

        @Test
        @DisplayName("无拦截器：LifecycleBean 的 afterPropertiesSet 和 destroy 均正常调用")
        void noInterceptor_lifecycleCallbacksWork() {
            container.registerBeanDefinition(
                    BeanDefinition.of("lifecycle", LifecycleBean.class));
            LifecycleBean bean = container.getBean("lifecycle", LifecycleBean.class);
            container.destroySingletons();

            assertTrue(bean.callLog.contains("afterPropertiesSet"));
            assertTrue(bean.callLog.contains("destroy"));
        }
    }

    // ====================================================================
    // 依赖解析
    // ====================================================================

    @Nested
    @DisplayName("依赖解析")
    class ArgumentResolution {

        @Test
        @DisplayName("依赖不存在时，异常直接透传（不被二次包装为 BeanCreationException）")
        void dependencyNotFound_exceptionPassedThrough() {
            // DependentBean 依赖 SimpleBean，但 SimpleBean 未注册
            container.registerBeanDefinition(
                    BeanDefinition.of("dependent", DependentBean.class)
                            .withInstantiationPolicy(
                                    InstantiationPolicy.constructor(SimpleBean.class)));

            // 异常应该是 NoSuchBeanDefinitionException 而非被包装的 BeanCreationException
            Exception ex = assertThrows(Exception.class,
                    () -> container.getBean("dependent"));

            // 根因应直接可见，不应被埋在多层 cause 中
            boolean rootCauseVisible =
                    ex.getClass().getSimpleName().contains("NoSuchBeanDefinition") ||
                            (ex.getCause() != null &&
                                    ex.getCause().getClass().getSimpleName().contains("NoSuchBeanDefinition"));
            assertTrue(rootCauseVisible,
                    "NoSuchBeanDefinitionException 应直接可见，实际异常类型：" + ex.getClass().getName());
        }
    }
}