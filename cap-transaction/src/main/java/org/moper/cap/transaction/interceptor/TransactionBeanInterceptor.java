package org.moper.cap.transaction.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 事务 Bean 拦截器
 *
 * <p>在 Bean 创建阶段检测是否存在 {@link Transactional} 注解的方法，
 * 若存在则为其创建代理，代理负责在方法执行前后进行事务管理。
 *
 * <p>工作流程：
 * <ol>
 *   <li>在 Bean 注入后检查是否有 {@link Transactional} 方法</li>
 *   <li>如果有，为其创建代理</li>
 *   <li>代理在运行时从容器中查找 {@link org.moper.cap.transaction.manager.TransactionManager} 实现</li>
 * </ol>
 *
 * <p>支持：
 * <ul>
 *   <li>JDK 动态代理（Bean 实现了接口时）</li>
 *   <li>嵌套事务（通过 {@link org.moper.cap.transaction.context.TransactionContext} 的 Stack 实现）</li>
 *   <li>事务传播性（REQUIRED, REQUIRES_NEW, NESTED, NEVER, NOT_SUPPORTED, MANDATORY, SUPPORTS）</li>
 *   <li>灵活的回滚规则（rollbackFor, noRollbackFor）</li>
 * </ul>
 *
 * <p>本拦截器的 {@link #getOrder()} 为 500，在 AOP 拦截器（400）之后执行，
 * 以确保事务代理包裹 AOP 代理。
 */
@Slf4j
public class TransactionBeanInterceptor implements BeanInterceptor {

    private final BeanContainer beanContainer;

    public TransactionBeanInterceptor(BeanContainer beanContainer) {
        this.beanContainer = beanContainer;
    }

    @Override
    public Object afterPropertyInjection(Object bean, BeanDefinition definition) throws BeanException {
        if (bean == null) {
            return null;
        }

        if (!hasTransactionalMethods(bean.getClass())) {
            return bean;
        }
        log.debug("为 Bean [{}] 创建事务代理", definition != null ? definition.name() : bean.getClass().getSimpleName());
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
     *   <li>否则：暂不支持代理（TODO: 使用 Javassist 子类代理）</li>
     * </ul>
     */
    private Object createProxy(Object target) {
        Class<?> targetClass = target.getClass();
        Class<?>[] interfaces = getNonInternalInterfaces(targetClass);

        if (interfaces.length > 0) {
            return createJdkProxy(target, targetClass, interfaces);
        } else {
            return createJavassistTransactionProxy(target, targetClass);
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
                new TransactionInvocationHandler(target, targetClass, beanContainer)
        );
    }

    /**
     * 使用 Javassist 创建无接口类的代理。
     *
     * <p>TODO: 实现与 AopBeanInterceptor 类似的 Javassist 代理逻辑
     */
    private Object createJavassistTransactionProxy(Object target, Class<?> beanClass) {
        log.warn("暂不支持为无接口类创建事务代理: {}", beanClass.getName());
        return target;
    }

    @Override
    public int getOrder() {
        return 500;
    }
}
