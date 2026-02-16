package org.moper.cap.core.context;

/**
 * 上下文语义标记接口
 */
public sealed interface Context permits LifecycleContext, ResourceContext, DispatcherContext{}
