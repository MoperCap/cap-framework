package org.moper.cap.boot.config;

import jakarta.validation.constraints.NotNull;
import org.moper.cap.boot.annotation.ComponentScan;
import org.moper.cap.boot.annotation.InitializerExtensions;
import org.moper.cap.boot.annotation.ResourceScan;
import org.moper.cap.bootstrap.Initializer;
import org.moper.cap.config.ConfigurationClass;

import java.util.Collection;
import java.util.Set;

public final class DefaultConfigurationClass implements ConfigurationClass {
    private final Class<?> configClass;

    public DefaultConfigurationClass(Class<?> configClass) {
        this.configClass = configClass;
    }

    /**
     * 获取配置类上指定的软件包扫描路径集合
     *
     * @return 若配置类上的软件包扫描路径不为空，则返回对应的路径集合; 否则返回配置类所在软件包路径
     */
    @Override
    @NotNull
    public Collection<String> getComponentScanPaths(){
        ComponentScan scan = configClass.getAnnotation(ComponentScan.class);
        if(scan == null) return Set.of(configClass.getPackageName());
        else return Set.of(scan.value());
    }

    /**
     * 获取配置类上指定的资源包扫描路径集合
     *
     * @return 若配置上的资源包扫描路径不为空，则返回对应的路径集合; 否则返回 { "" }
     */
    @Override
    @NotNull
    public Collection<String> getResourceScanPaths(){
        ResourceScan scan = configClass.getAnnotation(ResourceScan.class);
        if(scan == null) return Set.of("");
        else return Set.of(scan.value());
    }

    /**
     * 获取配置上指定的Initializer类集合
     *
     * @return 若配置类上存在Initializer类，则返回对应集合; 否则返回空集合
     */
    @Override
    public Collection<Class<? extends Initializer>> getInitializerExtensionClasses() {
        InitializerExtensions extensions = configClass.getAnnotation(InitializerExtensions.class);
        if(extensions == null) return Set.of();
        else return Set.of(extensions.value());
    }

}
