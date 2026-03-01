package org.moper.cap.core.config.impl;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.core.annotation.ComponentScan;
import org.moper.cap.core.annotation.ResourceScan;
import org.moper.cap.core.config.ConfigurationClassParser;
import org.moper.cap.core.util.ResourcePackageMerger;
import org.moper.cap.core.util.ResourcePathMerger;

import java.util.*;

@Slf4j
public final class DefaultConfigurationClassParser implements ConfigurationClassParser {

    /**
     * 配置类上指定的软件包扫描路径集合
     */
    private final Set<String> componentScanPaths;

    /**
     * 配置类上指定的资源包扫描路径集合
     */
    private final Set<String> resourceScanPaths;

    public DefaultConfigurationClassParser(Class<?>... configurations) {
        if(configurations == null || configurations.length == 0){
            throw new IllegalArgumentException("configurations is null or empty");
        }

        List<String> cacheComponentScanPaths = new ArrayList<>();
        List<String> cacheResourceScanPaths = new ArrayList<>();
        for(Class<?> configuration : configurations){
            ComponentScan componentScan = configuration.getAnnotation(ComponentScan.class);
            if(componentScan != null) cacheComponentScanPaths.addAll(Arrays.asList(componentScan.value()));
            else cacheComponentScanPaths.add(configuration.getPackageName());

            ResourceScan resourceScan = configuration.getAnnotation(ResourceScan.class);
            if(resourceScan != null) cacheResourceScanPaths.addAll(Arrays.asList(resourceScan.value()));
        }

        if(cacheResourceScanPaths.isEmpty()) cacheResourceScanPaths.add("");

        this.componentScanPaths = Set.copyOf(ResourcePackageMerger.merge(cacheComponentScanPaths));
        this.resourceScanPaths = Set.copyOf(ResourcePathMerger.merge(cacheResourceScanPaths));

        log.info("ConfigurationClassParser Component Scan Paths: {}", componentScanPaths);
        log.info("ConfigurationClassParser Resource Scan Paths: {}", resourceScanPaths);
    }

    /**
     * 获取配置类上指定的软件包扫描路径集合 </br>
     *
     * 若部分配置类上没有指定软件包扫描路径，则默认使用该配置类所在的软件包路径作为扫描路径
     *
     * @return 若配置类上的软件包扫描路径不为空，则返回对应的路径集合; 否则返回配置类所在软件包路径
     */
    @Override
    public Collection<String> getComponentScanPaths(){
        return componentScanPaths;
    }

    /**
     * 获取配置类上指定的资源包扫描路径集合
     *
     * @return 若配置上的资源包扫描路径不为空，则返回对应的路径集合; 否则返回 { "" }
     */
    @Override
    public Collection<String> getResourceScanPaths(){
        return resourceScanPaths;
    }


}
