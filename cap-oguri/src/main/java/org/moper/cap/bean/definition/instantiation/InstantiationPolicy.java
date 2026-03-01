package org.moper.cap.bean.definition.instantiation;

/**
 * Bean实例化策略接口 </br>
 *
 * 目前支持两种实现：基于构造函数的实例化策略 {@link ConstructorInstantiation} 和基于工厂方法的实例化策略 {@link FactoryInstantiation}
 */
public sealed interface InstantiationPolicy permits ConstructorInstantiation, FactoryInstantiation {}