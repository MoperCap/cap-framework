package org.moper.cap.boot.runner;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.PostConstruct;
import org.moper.cap.bean.annotation.PreDestroy;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanDefinitionException;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 扫描已注册的 BeanDefinition，查找并绑定 {@link PostConstruct} 和 {@link PreDestroy}
 * 标注的生命周期方法。
 *
 * <p>执行顺序为 325，在 {@link BeanParameterResolverBootstrapRunner}（315）之后、
 * {@link BeanInjectionBootstrapRunner}（350）之前执行。
 *
 * <p><b>生命周期方法签名要求：</b>
 * <ul>
 *   <li>访问修饰符：{@code public}</li>
 *   <li>返回类型：{@code void}</li>
 *   <li>参数列表：无参</li>
 * </ul>
 *
 * <p>每个类只能有一个 {@link PostConstruct} 方法和一个 {@link PreDestroy} 方法，
 * 存在多个则抛出 {@link BeanDefinitionException}。
 *
 * <p><b>注意：</b>{@link org.moper.cap.bean.annotation.Bean} 方法返回的 Bean 通常
 * 不含生命周期注解（第三方类），用户应在工厂方法内部完成初始化逻辑。
 * 因此，该 Runner 仅处理类上的注解，不处理工厂方法返回类型的注解。
 */
@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 325,
        description = "Attach @PostConstruct and @PreDestroy lifecycle methods to BeanDefinitions")
public class BeanLifecycleBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        BeanContainer container = context.getBeanContainer();
        String[] beanNames = container.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            BeanDefinition def = container.getBeanDefinition(beanName);

            // Skip beans that already have lifecycle methods set (e.g., by LifecycleMethodRegisterBootstrapRunner)
            if ((def.initMethod() != null && !def.initMethod().isBlank())
                    || (def.destroyMethod() != null && !def.destroyMethod().isBlank())) {
                continue;
            }

            Class<?> beanType = def.type();

            String initMethodName = findLifecycleMethod(beanType, PostConstruct.class, beanName);
            String destroyMethodName = findLifecycleMethod(beanType, PreDestroy.class, beanName);

            if (initMethodName == null && destroyMethodName == null) {
                continue;
            }

            BeanDefinition updated = def
                    .withInitMethod(initMethodName)
                    .withDestroyMethod(destroyMethodName);
            container.removeBeanDefinition(beanName);
            container.registerBeanDefinition(updated);
            log.info("Lifecycle methods attached to bean '{}': @PostConstruct={}, @PreDestroy={}",
                    beanName,
                    initMethodName != null ? initMethodName : "none",
                    destroyMethodName != null ? destroyMethodName : "none");
        }
    }

    /**
     * 在 Bean 类（含父类）中查找标注了指定生命周期注解的方法。
     *
     * @param beanType       Bean 类型
     * @param annotationType 生命周期注解类型（{@link PostConstruct} 或 {@link PreDestroy}）
     * @param beanName       Bean 名称（仅用于错误信息）
     * @return 方法名称，未找到返回 {@code null}
     * @throws BeanDefinitionException 若存在多个标注了该注解的方法，或方法签名不符合要求
     */
    private String findLifecycleMethod(Class<?> beanType,
                                       Class<? extends java.lang.annotation.Annotation> annotationType,
                                       String beanName) {
        List<Method> found = new ArrayList<>();
        Class<?> current = beanType;
        while (current != null && current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotationType)) {
                    found.add(method);
                }
            }
            current = current.getSuperclass();
        }

        if (found.isEmpty()) {
            return null;
        }

        if (found.size() > 1) {
            throw new BeanDefinitionException(
                    "Multiple @" + annotationType.getSimpleName()
                    + " methods found in bean '" + beanName + "' (" + beanType.getName() + ")."
                    + " Only one is allowed.");
        }

        Method method = found.get(0);
        validateLifecycleMethod(method, annotationType.getSimpleName(), beanName, beanType);
        return method.getName();
    }

    /**
     * 验证生命周期方法的签名是否符合要求。
     *
     * @param method         方法
     * @param annotationName 注解名称（用于错误信息）
     * @param beanName       Bean 名称（用于错误信息）
     * @param beanType       Bean 类型（用于错误信息）
     * @throws BeanDefinitionException 若方法签名不符合要求
     */
    private void validateLifecycleMethod(Method method, String annotationName,
                                         String beanName, Class<?> beanType) {
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new BeanDefinitionException(
                    "@" + annotationName + " method '" + method.getName()
                    + "' in bean '" + beanName + "' (" + beanType.getName()
                    + ") must be public.");
        }
        if (method.getParameterCount() != 0) {
            throw new BeanDefinitionException(
                    "@" + annotationName + " method '" + method.getName()
                    + "' in bean '" + beanName + "' (" + beanType.getName()
                    + ") must have no parameters, but found "
                    + method.getParameterCount() + " parameter(s).");
        }
        if (!method.getReturnType().equals(void.class)) {
            throw new BeanDefinitionException(
                    "@" + annotationName + " method '" + method.getName()
                    + "' in bean '" + beanName + "' (" + beanType.getName()
                    + ") must return void, but returns " + method.getReturnType().getName() + ".");
        }
    }
}
