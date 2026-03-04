package org.moper.cap.boot.application.impl;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.container.impl.DefaultBeanContainer;
import org.moper.cap.boot.application.CapApplication;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.command.CommandArgumentParser;
import org.moper.cap.core.command.impl.DefaultCommandArgumentParser;
import org.moper.cap.core.config.ConfigurationClassParser;
import org.moper.cap.core.config.impl.DefaultConfigurationClassParser;
import org.moper.cap.core.constants.BannerConstants;
import org.moper.cap.core.constants.ResourceConstants;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.core.context.impl.DefaultBootstrapContext;
import org.moper.cap.core.exception.BootstrapRunnerException;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerDefinition;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.core.runner.RuntimeRunner;
import org.moper.cap.common.banner.BannerPrinter;
import org.moper.cap.common.converter.TypeResolver;
import org.moper.cap.common.converter.impl.DefaultTypeResolver;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.officer.impl.DefaultPropertyOfficer;

import java.net.URL;
import java.util.ServiceLoader;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class DefaultCapApplication implements CapApplication {

    private final RuntimeContext runtimeContext;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public DefaultCapApplication(Class<?> primarySource, String... args) throws Exception {
        // 输出系统banner
        printSystemBanner();

        BeanContainer beanContainer = new DefaultBeanContainer();
        TypeResolver typeResolver = new DefaultTypeResolver();
        PropertyOfficer propertyOfficer = new DefaultPropertyOfficer(ResourceConstants.PROPERTY_OFFICER, typeResolver);
        CommandArgumentParser commandArgumentParser = new DefaultCommandArgumentParser(args);
        ConfigurationClassParser configurationClassParser = new DefaultConfigurationClassParser(primarySource);

        DefaultBootstrapContext bootstrapContext = new DefaultBootstrapContext(beanContainer, propertyOfficer, commandArgumentParser, configurationClassParser, typeResolver);

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

            runners.add(new RunnerDefinition<>(order, clazz, runner, type, name, description));
        }


        // 输出 Bootstrap 阶段 banner
        printBootstrapBanner();
        // 按顺序执行所有 Initializer
        for(RunnerDefinition<BootstrapRunner> runner : runners){
            log.info("Running BootstrapRunner [{}]", runner);
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

            runners.add(new RunnerDefinition<>(order, clazz, runner, type, name, description));
        }

        // 输出 Runtime 阶段 banner
        printRuntimeBanner();
        // 按顺序执行所有 Initializer
        for(RunnerDefinition<RuntimeRunner> runner : runners){
            log.info("Running BootstrapRunner {} ({})", runner.name(), runner.clazz().getName());
            RuntimeRunner instance = runner.runner();
            instance.onApplicationStarted(runtimeContext);
        }
        return runtimeContext;
    }

    private void printSystemBanner(){
        URL url = getClass().getClassLoader().getResource(BannerConstants.SUPPORTED_BANNER_FILE);
        if(url == null) BannerPrinter.printBannerFromClasspath(BannerConstants.DEFAULT_BANNER_FILE);
        else BannerPrinter.printBannerFromClasspath(BannerConstants.SUPPORTED_BANNER_FILE);
    }

    private void printBootstrapBanner(){
        BannerPrinter.printBannerFromClasspath(BannerConstants.BOOTSTRAP_BANNER_FILE);
    }

    private void printRuntimeBanner(){
        BannerPrinter.printBannerFromClasspath(BannerConstants.RUNTIME_BANNER_FILE);
    }
}
