package org.moper.cap.core.config;

import java.util.Collection;

public interface ConfigurationClassParser {

    /**
     * 获取配置类上指定的软件包扫描路径集合
     *
     * @return 若配置类上的软件包扫描路径不为空，则返回对应的路径集合; 否则返回配置类所在软件包路径
     */
    
    Collection<String> getComponentScanPaths();

    /**
     * 获取配置类上指定的资源包扫描路径集合
     *
     * @return 若配置上的资源包扫描路径不为空，则返回对应的路径集合; 否则返回 { "" }
     */
    
    Collection<String> getResourceScanPaths();
}
