package org.moper.cap.bean.definition;

import jakarta.validation.constraints.NotNull;

public sealed interface BeanCreation permits
        ConstructorBeanCreation
        , InstanceFactoryMethodBeanCreation
        , StaticFactoryMethodBeanCreation {

    /**
     * 获取Bean实例创建方式
     *
     * @return Bean实例创建方式
     */
    @NotNull BeanCreationType type();

    /**
     * Bean创建方式类型枚举
     */
    enum BeanCreationType {
        /**
         * 通过构造函数创建Bean实例
         */
        CONSTRUCTOR,

        /**
         * 通过实例工厂方法创建Bean实例
         * 需要先获取工厂Bean实例，再调用其实例方法
         */
        INSTANCE_FACTORY_METHOD,

        /**
         * 通过静态工厂方法创建Bean实例
         * 直接调用类的静态方法
         */
        STATIC_FACTORY_METHOD
    }
}
