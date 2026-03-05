package org.moper.cap.web.invoker.factory;

import org.moper.cap.web.invoker.MethodInvoker;
import org.moper.cap.web.invoker.impl.DefaultMethodInvoker;
import org.moper.cap.web.binder.ParameterBinderRegistry;

/**
 * MethodInvoker 工厂类 - 用于创建 MethodInvoker 实例
 */
public class MethodInvokerFactory {

    /**
     * 创建默认的 MethodInvoker 实现
     *
     * @param binderRegistry 参数绑定器注册表
     * @return MethodInvoker 实例
     */
    public static MethodInvoker createDefault(ParameterBinderRegistry binderRegistry) {
        return new DefaultMethodInvoker(binderRegistry);
    }
}
