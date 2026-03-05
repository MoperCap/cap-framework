package org.moper.cap.common.priority;

public final class PriorityUtils {



    /**
     * 获取指定类上 @Priority 注解中的 value
     *
     * @param clazz 指定类
     * @return 若指定类上存在 @Priority 注解则返回对应值；否则返回默认值 0
     */
    public static int getPriority(Class<?> clazz) {
        if(clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }

        Priority priority = clazz.getAnnotation(Priority.class);
        if(priority == null) {
            return 0;
        }else return priority.value();
    }
}
