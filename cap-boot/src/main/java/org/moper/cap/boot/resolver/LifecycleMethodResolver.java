package org.moper.cap.boot.resolver;

import org.moper.cap.bean.exception.BeanDefinitionException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 生命周期方法解析器。
 *
 * <p>在 Bean 定义注册阶段验证 initMethod/destroyMethod 是否存在，
 * 并确保其签名符合规范：无参、void 返回值。
 * 支持 private/protected/public 可见性。
 */
public final class LifecycleMethodResolver {

    private LifecycleMethodResolver() {}

    /**
     * 在指定类（及其父类）中查找并验证生命周期方法。
     *
     * <p>验证规则：
     * <ul>
     *   <li>方法必须存在于类或其父类中</li>
     *   <li>方法必须无参</li>
     *   <li>方法返回类型必须为 void</li>
     * </ul>
     *
     * @param beanType   Bean 的实际类型，不能为 null
     * @param methodName 生命周期方法名称，不能为空
     * @throws BeanDefinitionException 如果方法不存在或签名不符合要求
     */
    public static void validate(Class<?> beanType, String methodName) {
        if (methodName == null || methodName.isBlank()) {
            return;
        }
        Method method = findMethod(beanType, methodName);
        if (method == null) {
            throw new BeanDefinitionException(
                    "Lifecycle method '" + methodName + "' not found in " + beanType.getName() +
                    " (including superclasses). The method must be a no-arg void method.");
        }
        if (method.getParameterCount() != 0) {
            throw new BeanDefinitionException(
                    "Lifecycle method '" + methodName + "' in " + beanType.getName() +
                    " must have no parameters, but found " + method.getParameterCount() + " parameter(s).");
        }
        if (!method.getReturnType().equals(void.class)) {
            throw new BeanDefinitionException(
                    "Lifecycle method '" + methodName + "' in " + beanType.getName() +
                    " must return void, but returns " + method.getReturnType().getName() + ".");
        }
    }

    /**
     * 在类的继承层级中查找指定名称的无参方法（含 private/protected/public）。
     *
     * @param clazz      起始类
     * @param methodName 方法名称
     * @return 找到的方法，未找到时返回 null
     */
    private static Method findMethod(Class<?> clazz, String methodName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 0
                        && !Modifier.isAbstract(method.getModifiers())) {
                    return method;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
