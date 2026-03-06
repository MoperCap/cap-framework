package org.moper.cap.transaction.interceptor;

import javassist.util.proxy.ProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.transaction.annotation.Transactional;
import org.moper.cap.transaction.aspect.TransactionAspect;
import org.moper.cap.transaction.manager.TransactionManager;
import sun.misc.Unsafe;

import java.lang.reflect.*;

/**
 * 事务 Bean 拦截器
 *
 * <p>在 Bean 创建阶段检测是否存在 {@link Transactional} 注解的方法，
 * 若存在则为其创建代理，代理负责在方法执行前后进行事务管理。
 *
 * <p>支持：
 * <ul>
 *   <li>JDK 动态代理（Bean 实现了接口时）</li>
 *   <li>Javassist 子类代理（Bean 未实现接口时）</li>
 *   <li>嵌套事务（通过 {@link TransactionContext} 的 Stack 实现）</li>
 *   <li>事务传播性（REQUIRED, REQUIRES_NEW, NESTED, NEVER, NOT_SUPPORTED, MANDATORY, SUPPORTS）</li>
 *   <li>灵活的回滚规则（rollbackFor, noRollbackFor）</li>
 * </ul>
 *
 * <p>本拦截器的 {@link #getOrder()} 为 500，在 AOP 拦截器（400）之后执行，
 * 以确保事务代理包裹 AOP 代理。
 */
@Slf4j
public class TransactionBeanInterceptor implements BeanInterceptor {

    private static final Unsafe UNSAFE = getUnsafe();

    private final TransactionManager transactionManager;
    private final TransactionAspect transactionAspect;

    public TransactionBeanInterceptor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.transactionAspect = new TransactionAspect(transactionManager);
    }

    @Override
    public Object afterPropertyInjection(Object bean, BeanDefinition definition) throws BeanException {
        if (!hasTransactionalMethods(bean.getClass())) {
            return bean;
        }
        log.debug("为 Bean [{}] 创建事务代理", definition.name());
        return createProxy(bean);
    }

    /**
     * 检查类（及其父类层级）是否含有 {@link Transactional} 注解的方法。
     */
    private boolean hasTransactionalMethods(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Method m : current.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Transactional.class)) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }

    /**
     * 根据 Bean 类型选择合适的代理策略。
     *
     * <ul>
     *   <li>若 Bean 实现了接口：使用 JDK 动态代理</li>
     *   <li>否则：使用 Javassist 子类代理</li>
     * </ul>
     */
    private Object createProxy(Object target) {
        Class<?> targetClass = target.getClass();
        Class<?>[] interfaces = getNonInternalInterfaces(targetClass);

        if (interfaces.length > 0) {
            return createJdkProxy(target, targetClass, interfaces);
        } else {
            return createJavassistProxy(target, targetClass);
        }
    }

    /**
     * 获取类实现的非 JDK 内部接口列表（排除 java.* / sun.* 等内部接口）。
     */
    private Class<?>[] getNonInternalInterfaces(Class<?> clazz) {
        java.util.function.Predicate<Class<?>> isUserDefined =
                i -> !i.getName().startsWith("java.") && !i.getName().startsWith("sun.");
        return java.util.Arrays.stream(clazz.getInterfaces())
                .filter(isUserDefined)
                .toArray(Class<?>[]::new);
    }

    private Object createJdkProxy(Object target, Class<?> targetClass, Class<?>[] interfaces) {
        return Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                interfaces,
                new TransactionInvocationHandler(target, targetClass, transactionAspect)
        );
    }

    private Object createJavassistProxy(Object target, Class<?> targetClass) {
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(targetClass);
            factory.setFilter(m -> {
                int mod = m.getModifiers();
                return !Modifier.isAbstract(mod)
                        && !Modifier.isNative(mod)
                        && !Modifier.isStatic(mod);
            });

            Class<?> proxyClass = factory.createClass();
            Object proxyInstance = UNSAFE.allocateInstance(proxyClass);

            ((javassist.util.proxy.Proxy) proxyInstance).setHandler((self, thisMethod, proceed, args) -> {
                // thisMethod may be declared in an intermediate proxy class (e.g., AOP proxy);
                // traverse the class hierarchy to find the method with @Transactional
                Method annotatedMethod = findAnnotatedMethodInHierarchy(thisMethod);
                if (annotatedMethod == null) {
                    thisMethod.setAccessible(true);
                    return thisMethod.invoke(target, args);
                }
                try {
                    transactionAspect.handleTransactionBegin(annotatedMethod);
                    thisMethod.setAccessible(true);
                    Object result = thisMethod.invoke(target, args);
                    transactionAspect.handleTransactionEnd(annotatedMethod, null);
                    return result;
                } catch (InvocationTargetException ite) {
                    Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
                    transactionAspect.handleTransactionEnd(annotatedMethod, cause);
                    throw cause;
                } catch (Exception e) {
                    transactionAspect.handleTransactionEnd(annotatedMethod, e);
                    throw e;
                }
            });

            return proxyInstance;
        } catch (Exception e) {
            throw new BeanException("Failed to create Javassist transaction proxy for " + targetClass.getName(), e);
        }
    }

    /**
     * 在方法声明类及其父类层级中查找带有 {@link Transactional} 注解的同名同参数方法。
     *
     * <p>Javassist 代理的 {@code thisMethod} 声明在直接父类（可能是 AOP 代理类）中，
     * AOP 代理类的方法没有 {@link Transactional} 注解，因此需要向上遍历到原始类。
     */
    private static Method findAnnotatedMethodInHierarchy(Method method) {
        // Check the method itself first
        if (method.isAnnotationPresent(Transactional.class)) {
            return method;
        }

        Class<?> superClass = method.getDeclaringClass().getSuperclass();
        while (superClass != null && superClass != Object.class) {
            try {
                Method superMethod = superClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                if (superMethod.isAnnotationPresent(Transactional.class)) {
                    return superMethod;
                }
            } catch (NoSuchMethodException e) {
                // not declared in this superclass
            }
            superClass = superClass.getSuperclass();
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 500;
    }

    private static Unsafe getUnsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
