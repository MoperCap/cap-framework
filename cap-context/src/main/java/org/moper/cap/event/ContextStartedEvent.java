package org.moper.cap.event;

/**
 * 上下文启动事件，在 {@code ApplicationContext.run()} 完成后发布
 */
public class ContextStartedEvent extends ApplicationEvent {

    public ContextStartedEvent(Object source) {
        super(source);
    }
}
