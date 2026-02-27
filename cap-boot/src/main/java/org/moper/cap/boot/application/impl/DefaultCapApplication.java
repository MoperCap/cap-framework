package org.moper.cap.boot.application.impl;

import org.moper.cap.boot.application.CapApplication;
import org.moper.cap.context.context.ApplicationContext;
import org.moper.cap.context.context.impl.DefaultBootstrapContext;
import org.moper.cap.context.initializer.Initializer;

import java.io.Closeable;
import java.util.ServiceLoader;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultCapApplication implements CapApplication {

    private final ApplicationContext applicationContext;

    public DefaultCapApplication(Class<?> primarySource, String... args) throws Exception {

        DefaultBootstrapContext bootstrapContext = new DefaultBootstrapContext(primarySource);

        // 通过 SPI 发现所有 Initializer，收集到有序集合
        TreeSet<Initializer> initializers = new TreeSet<>();
        ServiceLoader<Initializer> loader = ServiceLoader.load(Initializer.class);
        for (Initializer initializer : loader) {
            initializers.add(initializer);
        }

        // 按顺序执行所有 Initializer
        for (Initializer initializer : initializers) {
            initializer.initialize(bootstrapContext);
            initializer.close();
        }
        // 构造完成后，BootstrapContext 处于完全初始化状态
        this.applicationContext = bootstrapContext.build();
    }

    @Override
    public ApplicationContext run() throws Exception {



        return applicationContext;
    }
}
