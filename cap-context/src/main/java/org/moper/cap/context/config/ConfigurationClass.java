package org.moper.cap.context.config;

import jakarta.validation.constraints.NotNull;
import org.moper.cap.context.bootstrap.Initializer;

import java.util.Collection;

public interface ConfigurationClass {

    /**
     * 获取配置类上指定的软件包扫描路径集合
     *
     * @return 若配置类上的软件包扫描路径不为空，则返回对应的路径集合; 否则返回配置类所在软件包路径
     */
    @NotNull
    Collection<String> getComponentScanPaths();

    /**
     * 获取配置类上指定的资源包扫描路径集合
     *
     * @return 若配置上的资源包扫描路径不为空，则返回对应的路径集合; 否则返回 { "" }
     */
    @NotNull
    Collection<String> getResourceScanPaths();

    /**
     * 获取配置上指定的Initializer类集合
     *
     * @return 若配置类上存在Initializer类，则返回对应集合; 否则返回空集合
     */
    @NotNull
    Collection<Class<? extends Initializer>> getInitializerExtensionClasses();
}
