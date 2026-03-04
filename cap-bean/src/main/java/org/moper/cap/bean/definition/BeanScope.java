package org.moper.cap.bean.definition;

/**
 * Bean 的作用域枚举
 *
 * <p>cap-bean 内核直接支持 {@link #SINGLETON} 和 {@link #PROTOTYPE}。
 * {@link #REQUEST} 和 {@link #SESSION} 由上层 cap-web 通过作用域代理实现，
 * 内核保留枚举值供上层模块识别。
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
