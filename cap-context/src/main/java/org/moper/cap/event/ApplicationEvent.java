package org.moper.cap.event;

/**
 * 应用事件基类
 */
public abstract class ApplicationEvent {

    private final Object source;
    private final long timestamp;

    /**
     * 创建应用事件
     *
     * @param source 事件源
     */
    protected ApplicationEvent(Object source) {
        this.source = source;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 获取事件源
     *
     * @return 事件源
     */
    public Object getSource() {
        return source;
    }

    /**
     * 获取事件发生时间戳（毫秒）
     *
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
}
