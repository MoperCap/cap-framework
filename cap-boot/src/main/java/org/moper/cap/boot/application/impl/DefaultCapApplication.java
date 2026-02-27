package org.moper.cap.boot.application.impl;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.boot.application.CapApplication;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.core.context.impl.DefaultBootstrapContext;
import org.moper.cap.core.exception.BootstrapRunnerException;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerDefinition;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.core.runner.RuntimeRunner;

import java.util.ServiceLoader;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class DefaultCapApplication implements CapApplication {

    private final RuntimeContext runtimeContext;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public DefaultCapApplication(Class<?> primarySource, String... args) throws Exception {

        DefaultBootstrapContext bootstrapContext = new DefaultBootstrapContext(primarySource);

        // 通过 SPI 发现所有 BootstrapRunner 收集到有序集合
        TreeSet<RunnerDefinition<BootstrapRunner>> runners = new TreeSet<>();
        ServiceLoader<BootstrapRunner> loader = ServiceLoader.load(BootstrapRunner.class);
        for (BootstrapRunner runner : loader) {
            Class<? extends BootstrapRunner> clazz = runner.getClass();
            RunnerMeta meta = clazz.getAnnotation(RunnerMeta.class);
            if(meta == null){
                throw new BootstrapRunnerException("BootstrapRunner[" + clazz.getName() + "] is missing @RunnerMeta annotation");
            }
            RunnerType type = meta.type();
            int order = meta.order();
            String name = meta.name();
            String description = meta.description();

            runners.add(new RunnerDefinition<>(type, order, clazz, runner, name, description));
        }

        // 按顺序执行所有 Initializer
        for(RunnerDefinition<BootstrapRunner> runner : runners){
            log.info("Running BootstrapRunner {} ({})", runner.name(), runner.clazz().getName());
            BootstrapRunner instance = runner.runner();
            instance.initialize(bootstrapContext);
            instance.close();
        }

        // 构造完成后，BootstrapContext 处于完全初始化状态
        this.runtimeContext = bootstrapContext.build();
    }

    @Override
    public RuntimeContext run() throws Exception {
        if(!started.compareAndSet(false, true)) {
            return runtimeContext;
        }

        // 通过 SPI 发现所有 RuntimeRunner 收集到有序集合
        TreeSet<RunnerDefinition<RuntimeRunner>> runners = new TreeSet<>();
        ServiceLoader<RuntimeRunner> loader = ServiceLoader.load(RuntimeRunner.class);
        for (RuntimeRunner runner : loader) {
            Class<? extends RuntimeRunner> clazz = runner.getClass();
            RunnerMeta meta = clazz.getAnnotation(RunnerMeta.class);
            if(meta == null){
                throw new BootstrapRunnerException("RuntimeRunner[" + clazz.getName() + "] is missing @RunnerMeta annotation");
            }
            RunnerType type = meta.type();
            int order = meta.order();
            String name = meta.name();
            String description = meta.description();

            runners.add(new RunnerDefinition<>(type, order, clazz, runner, name, description));
        }

        // 按顺序执行所有 Initializer
        for(RunnerDefinition<RuntimeRunner> runner : runners){
            log.info("Running BootstrapRunner {} ({})", runner.name(), runner.clazz().getName());
            RuntimeRunner instance = runner.runner();
            instance.onApplicationStarted(runtimeContext);
        }
        return runtimeContext;
    }
}
