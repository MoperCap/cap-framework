package org.moper.cap.core.runner;

/**
 * 框架执行器类型 </br>
 */
public enum RunnerType {

    /**
     * 框架最小运行内核
     */
    KERNEL,

    /**
     * 框架官方可选能力
     */
    FEATURE,

    /**
     * 第三方扩展能力
     * 通过依赖引入的插件
     */
    EXTENSION;
}

