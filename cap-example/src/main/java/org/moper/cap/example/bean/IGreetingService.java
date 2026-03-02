package org.moper.cap.example.bean;

/**
 * 问候服务接口，用于 JDK 动态代理示例。
 *
 * <p>实现类 {@link GreetingServiceImpl} 实现了该接口，
 * 由于存在接口，AOP 框架将使用 JDK Proxy 对其进行代理。
 */
public interface IGreetingService {

    /**
     * 向指定名称打招呼
     *
     * @param name 被问候的名称
     * @return 问候语
     */
    String greet(String name);

    /**
     * 发送消息
     *
     * @param message 消息内容
     * @return 发送结果
     */
    String sendMessage(String message);
}
