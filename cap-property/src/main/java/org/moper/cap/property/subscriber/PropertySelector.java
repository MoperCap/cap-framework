package org.moper.cap.property.subscriber;

/**
 * 属性选择器接口 </br>
 *
 * 属性选择器用于定义属性订阅者感兴趣的属性范围，属性订阅者通过实现该接口来指定自己关注的属性键，以便属性管理平台能够准确地将相关事件通知给订阅者。 </br>
 */
@FunctionalInterface
public interface PropertySelector {

    /**
     * 判断给定的属性键是否匹配订阅者的兴趣范围 </br>
     *
     * @param key 属性键
     * @return 如果属性键匹配订阅者的兴趣范围，则返回true；否则返回false
     */
    boolean matches(String key);
}
