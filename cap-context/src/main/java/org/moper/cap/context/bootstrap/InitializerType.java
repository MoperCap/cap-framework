package org.moper.cap.context.bootstrap;

/**
 * 框架启动阶段构造机类型
 */
public enum InitializerType {

    /**
     * 框架最小运行内核
     */
    KERNEL(0),

    /**
     * 框架官方可选能力
     */
    FEATURE(100),

    /**
     * 第三方扩展能力
     * 通过依赖引入的插件
     */
    EXTENSION(200);

    private final int priority;

    InitializerType(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }

}

