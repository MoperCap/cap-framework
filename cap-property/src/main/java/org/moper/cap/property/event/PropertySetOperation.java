package org.moper.cap.property.event;

/**
 * 属性设置操作 </br>
 * 即添加、更新操作的整合
 *
 * @param key 属性键，不能为null或blank
 * @param value 属性值，可以为null
 */
public record PropertySetOperation(
        String key,
        Object value
) implements PropertyOperation {

    public PropertySetOperation {
        if(key == null || key.isBlank()){
            throw new IllegalArgumentException("PropertySetOperation Key cannot be null or blank");
        }
    }

    @Override
    public String toString() {
        return "PropertySetOperation{" +
                "key='" + key + '\'' +
                ", value=" + value.toString() +
                '}';
    }
}
