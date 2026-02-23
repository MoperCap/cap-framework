package org.moper.cap.event;

/**
 * 上下文关闭事件，在 {@code ApplicationContext.close()} 开始时发布
 */
public class ContextClosedEvent extends ApplicationEvent {

    public ContextClosedEvent(Object source) {
        super(source);
    }
}
