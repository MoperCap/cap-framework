package org.moper.cap.core.context;

public sealed interface LifecycleContext extends Context permits BootstrapLifecycleContext, RuntimeLifecycleContext {
}
