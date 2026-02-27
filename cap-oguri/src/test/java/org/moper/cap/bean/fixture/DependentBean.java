package org.moper.cap.bean.fixture;

/** 依赖 SimpleBean 的有参构造 Bean，用于构造函数依赖注入测试 */
public class DependentBean {
    private final SimpleBean dependency;

    public DependentBean(SimpleBean dependency) {
        this.dependency = dependency;
    }

    public SimpleBean getDependency() { return dependency; }
}