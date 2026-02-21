package org.moper.cap.boot.bootstrap;

import jakarta.validation.constraints.NotNull;
import org.moper.cap.boot.exception.InitializerInstanceException;

import java.lang.reflect.InvocationTargetException;

/**
 * 构造器Definition
 * @param clazz 构造器具体类型
 * @param type 构造器级别
 * @param order 该构造器在同级别构造器中的优先级（值越小，优先级越高）
 * @param name 构造器名(不要求唯一, 可以为空)
 * @param description 构造器相关描述(可以为空)
 */
public record InitializerDefinition(
        @NotNull Class<? extends Initializer> clazz,
        @NotNull InitializerType type,
        int order,
        @NotNull String name,
        @NotNull String description
) implements Comparable<InitializerDefinition> {

    /**
     * 获取构造机实例
     * @return Initializer实例
     * @throws InitializerInstanceException 若获取实例失败，则抛出异常
     */
    public Initializer newInstance() throws InitializerInstanceException {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InitializerInstanceException("Failed to instance Initializer{" + clazz.getName() + ":" + name + "}", e);
        }
    }

    @Override
    public int compareTo(@NotNull InitializerDefinition o) {
        if(type != o.type) {
            return Integer.compare(type.priority(), o.type.priority());
        }else {
            return Integer.compare(order, o.order);
        }
    }
}
