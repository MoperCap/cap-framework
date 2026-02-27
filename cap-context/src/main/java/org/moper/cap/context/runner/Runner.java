package org.moper.cap.context.runner;

/**
 * 框架执行器接口 </br>
 *
 * 目前仅支持BootstrapRunner和RuntimeRunner两种类型的执行器 </br>
 */
public sealed interface Runner permits BootstrapRunner, RuntimeRunner{
}
