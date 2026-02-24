package org.moper.cap.property.subscriber;

@FunctionalInterface
public interface PropertySelector {

    boolean matches(String key);
}
