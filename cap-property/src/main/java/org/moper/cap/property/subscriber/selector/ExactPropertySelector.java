package org.moper.cap.property.subscriber.selector;

import org.moper.cap.property.subscriber.PropertySelector;

import java.util.Set;


/**
 * 精确匹配属性选择器 </br>
 *
 * 该选择器仅匹配指定的属性键，只有当属性键完全匹配订阅者的兴趣范围时才返回true </br>
 */
public final class ExactPropertySelector implements PropertySelector {

    private final Set<String> keys;

    public ExactPropertySelector(Set<String> keys) {
        this.keys = keys;
    }

    /**
     * 判断给定的属性键是否匹配订阅者的兴趣范围 </br>
     *
     * @param key 属性键
     * @return 如果属性键匹配订阅者的兴趣范围，则返回true；否则返回false
     */
    @Override
    public boolean matches(String key) {
        return keys.contains(key);
    }
}
