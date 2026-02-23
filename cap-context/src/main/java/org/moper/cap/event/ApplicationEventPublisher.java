package org.moper.cap.event;

/**
 * 应用事件发布者接口
 */
@FunctionalInterface
public interface ApplicationEventPublisher {

    /**
     * 发布应用事件
     *
     * @param event 要发布的事件
     */
    void publishEvent(ApplicationEvent event);
}
