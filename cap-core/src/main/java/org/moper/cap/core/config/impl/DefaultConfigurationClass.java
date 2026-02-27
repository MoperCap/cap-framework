package org.moper.cap.core.config.impl;

import org.moper.cap.core.annotation.ComponentScan;
import org.moper.cap.core.annotation.ResourceScan;
import org.moper.cap.core.config.ConfigurationClass;

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
    public Collection<String> getResourceScanPaths(){
        ResourceScan scan = configClass.getAnnotation(ResourceScan.class);
        if(scan == null) return Set.of("");
        else return Set.of(scan.value());
    }
}
