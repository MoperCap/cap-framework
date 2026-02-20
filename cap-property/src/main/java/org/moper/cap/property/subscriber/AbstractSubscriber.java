package org.moper.cap.property.subscriber;

import jakarta.validation.constraints.NotBlank;

public abstract class AbstractSubscriber implements PropertySubscriber {
    private final @NotBlank String propertyKey;

    public AbstractSubscriber(@NotBlank String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * 获取属性订阅者所订阅的属性键 </br>
     *
     * @return 订阅的属性键
     */
    @Override
    public @NotBlank String getPropertyKey() {
        return this.propertyKey;
    }
}
