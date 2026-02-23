package org.moper.cap.event;

/**
 * 应用事件监听器接口
 *
 * @param <E> 监听的事件类型
 */
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> {

    /**
     * 处理应用事件
     *
     * @param event 应用事件
     */
    void onEvent(E event);
}
