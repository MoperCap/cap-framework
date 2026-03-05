package org.moper.cap.web.invoker.factory;

import org.moper.cap.web.invoker.MethodInvoker;
import org.moper.cap.web.invoker.impl.DefaultMethodInvoker;
import org.moper.cap.web.binder.ParameterBinderRegistry;

/**
 * MethodInvoker 工厂类
 */
public class MethodInvokerFactory {

    private MethodInvokerFactory() {
        // 私有构造函数，防止实例化
    }

    /**
     * 创建默认的 MethodInvoker 实现
     */
    public static MethodInvoker create(ParameterBinderRegistry binderRegistry) {
        return new DefaultMethodInvoker(binderRegistry);
    }
}
