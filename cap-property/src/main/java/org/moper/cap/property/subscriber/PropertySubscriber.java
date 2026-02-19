package org.moper.cap.property.subscriber;

/**
 * 单一属性订阅者 </br>
 * 属性订阅者用于订阅单个属性的相关事件，负责处理该属性的添加、更新和移除事件。 </br>
 * 属性订阅者可以通过实现该接口来定义自己的事件处理逻辑，具体的属性的相关事件由其隶属的属性订阅客户端Subscription负责通知 </br>
 * 属性订阅者的声明周期也由其隶属的属性订阅客户端Subscription负责管理，用户可以根据需要创建和销毁属性订阅者 </br>
 */
public interface PropertySubscriber {

    /**
     * 获取属性订阅者所订阅的属性键 </br>
     *
     * @return 订阅的属性键
     */
    String getPropertyKey();

    /**
     * 属性设置事件处理方法 </br>
     *
     * 当属性被添加或更新时，属性订阅客户端 Subscription 会调用该方法来通知属性订阅者。
     * 属性订阅者可以在该方法中执行相应的逻辑，例如更新相关状态、触发其他操作等 </br>
     *
     * 若处理过程中发生异常，允许抛出任意异常。异常将被 Subscription 捕获和处理，
     * 并不会中断其他订阅者的处理。
     *
     * @param value 被添加或更新的最新属性值
     * @throws Exception 处理过程中发生的任何异常
     */
    void onSet(Object value) throws Exception;

    /**
     * 属性移除事件处理方法 </br>
     *
     * 当属性被移除时，属性订阅客户端 Subscription 会调用该方法来通知属性订阅者。
     * 属性订阅者可以在该方法中执行相应的逻辑，例如清理相关资源、更新状态等 </br>
     *
     * 若处理过程中发生异常，允许抛出任意异常。异常将被 Subscription 捕获和处理，
     * 并不会中断其他订阅者的处理。
     *
     * @throws Exception 处理过程中发生的任何异常
     */
    void onRemoved() throws Exception;
}
