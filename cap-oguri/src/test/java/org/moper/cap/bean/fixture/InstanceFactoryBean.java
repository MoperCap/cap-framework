package org.moper.cap.bean.fixture;

/** 提供实例工厂方法的工厂 Bean */
public class InstanceFactoryBean {
    public SimpleBean createSimpleBean() {
        SimpleBean bean = new SimpleBean();
        bean.setValue("from-instance-factory");
        return bean;
    }
}