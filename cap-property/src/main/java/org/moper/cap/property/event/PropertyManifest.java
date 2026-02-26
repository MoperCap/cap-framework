package org.moper.cap.property.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 属性操作清单 </br>
 * 记录一串属性操作的详细信息，包括操作人、操作内容和操作时间等。 </br>
 *
 * @param operator 操作人，唯一标识一个操作人，不允许为空或者空白
 * @param operations 操作内容，包含一系列属性操作的详细信息，不允许为空或者空白
 * @param timestamp 操作时间，表示属性操作的发生时间，不允许为空
 */
public record PropertyManifest(
        String operator,
        List<PropertyOperation> operations,
        Instant timestamp
) implements Serializable {

    public PropertyManifest{

        if(operator == null || operator.isBlank()) {
            throw new IllegalArgumentException("PropertyManifest Operator cannot be null or blank");
        }

        if(operations == null || operations.isEmpty()) {
            throw new IllegalArgumentException("PropertyManifest Operations cannot be null or empty");
        }

        if(timestamp == null){
            throw new IllegalArgumentException("PropertyManifest Timestamp cannot be null");
        }
    }

    /**
     * 属性操作清单工厂方法，简化属性操作清单的创建过程，自动设置操作时间为当前时间。
     *
     * @param operator 操作人，唯一标识一个操作人，不允许为空或者空白
     * @param operations 操作内容，包含一系列属性操作的详细信息，不允许为空或者空白
     * @return 一个新的属性操作清单实例，包含指定的操作人、操作内容和当前时间作为操作时间
     */
    public static PropertyManifest of(String operator, List<PropertyOperation> operations) {
        return new PropertyManifest(operator, operations, Instant.now());
    }

    @Override
    public String toString() {
        return "PropertyManifest{" +
                "operator='" + operator + '\'' +
                ", operations=[" + operations +
                "], timestamp=" + timestamp +
                '}';
    }
}
