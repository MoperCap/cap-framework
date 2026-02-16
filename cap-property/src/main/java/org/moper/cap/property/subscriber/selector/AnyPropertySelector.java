package org.moper.cap.property.subscriber.selector;

import org.moper.cap.property.subscriber.PropertySelector;

/**
 * 任意属性选择器 </br>
 *
 * 该选择器匹配所有属性键，无论属性键是什么都返回true </br>
 * 使用该选择器的属性订阅者将接收所有属性相关的事件，无论事件涉及哪个属性键 </br>
 */
public final class AnyPropertySelector implements PropertySelector {


    /**
     * 判断给定的属性键是否匹配订阅者的兴趣范围 </br>
     *
     * @param key 属性键
     * @return 如果属性键匹配订阅者的兴趣范围，则返回true；否则返回false
     */
    @Override
    public boolean matches(String key) {
        return true;
    }
}
