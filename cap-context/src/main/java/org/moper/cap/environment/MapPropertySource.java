package org.moper.cap.environment;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * 基于 Map 的 {@link PropertySource} 实现
 */
public class MapPropertySource implements PropertySource {

    private final String name;
    private final Map<String, Object> properties;
    private final int order;

    public MapPropertySource(String name, Map<String, Object> properties) {
        this(name, properties, 0);
    }

    public MapPropertySource(String name, Map<String, Object> properties, int order) {
        this.name = name;
        this.properties = Collections.unmodifiableMap(properties);
        this.order = order;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public boolean containsProperty(String key) {
        return properties.containsKey(key);
    }

    @Override
    public int getOrder() {
        return order;
    }
}
