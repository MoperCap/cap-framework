package org.moper.cap.property.subscriber;

/**
 * 属性选择器 </br>
 *
 * 负责判断指定的属性键是否满足要求
 */
@FunctionalInterface
public interface PropertySelector {

    /**
     * 属性选择接口
     *
     * @param key 属性键，不能为null或blank
     * @return 若符合条件则返回true；否则返回false
     */
    boolean matches(String key);
}
