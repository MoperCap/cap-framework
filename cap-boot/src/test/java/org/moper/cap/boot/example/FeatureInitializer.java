package org.moper.cap.boot.example;

import org.moper.cap.boot.annotation.InitializerMeta;
import org.moper.cap.boot.bootstrap.Initializer;
import org.moper.cap.boot.bootstrap.InitializerType;
import org.moper.cap.core.exception.CapFrameworkException;

@InitializerMeta(type = InitializerType.FEATURE, order = 1)
public class FeatureInitializer implements Initializer<OldEnvironmentResourceContext> {
    /**
     * 框架启动阶段执行
     *
     * @param context 初始化上下文
     */
    @Override
    public void initialize(OldEnvironmentResourceContext context) throws CapFrameworkException {
        System.out.println("Initializing Feature");
    }
}
