package org.moper.cap.bean.definition;

/**
 * Bean作用域枚举类
 */
public enum BeanScope {

    /**
     * 单例模式，容器中只有一个实例，默认值
     */
    SINGLETON,

    /**
     * 原型模式，每次获取Bean时都会创建一个新的实例
     */
    PROTOTYPE,

    /**
     * 请求作用域，每次HTTP请求都会创建一个新的实例
     */
    REQUEST,

    /**
     * 会话作用域，每个HTTP会话都会创建一个新的实例
     */
    SESSION
}
