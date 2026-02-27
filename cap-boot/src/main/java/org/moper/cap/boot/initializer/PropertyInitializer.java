package org.moper.cap.boot.initializer;

import org.moper.cap.context.initializer.Initializer;
import org.moper.cap.context.initializer.InitializerType;
import org.moper.cap.context.context.BootstrapContext;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

/**
 * 属性加载构造机，负责加载配置文件并注册到 Environment </br>
 * 支持 .properties 和 .yml/.yaml 格式
 */
public class PropertyInitializer extends Initializer {

    private static final Logger log = LoggerFactory.getLogger(PropertyInitializer.class);

    public PropertyInitializer() {
        super(InitializerType.KERNEL, 0, "PropertyInitializer", "Loads property files into Environment");
    }

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        Collection<String> resourcePaths = context.getConfigurationClass().getResourceScanPaths();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) classLoader = getClass().getClassLoader();

        for (String path : resourcePaths) {
            List<String> filesToLoad = resolveFilesToLoad(path);
            for (String resourceName : filesToLoad) {
                Map<String, Object> props = tryLoadFile(classLoader, resourceName);
                if (props == null || props.isEmpty()) continue;

                // 为每个配置文件创建独立的 Publisher
                PropertyPublisher publisher = context.getPropertyOfficer().getPublisher("property-publisher-" + resourceName);


                // 将所有属性转换为 PropertySetOperation 发布
                PropertySetOperation[] operations = props.entrySet().stream()
                        .map(e -> new PropertySetOperation(e.getKey(), e.getValue()))
                        .toArray(PropertySetOperation[]::new);

                // 发布属性
                publisher.publish(operations);
            }
        }
    }

    private List<String> resolveFilesToLoad(String path) {
        if (path == null || path.isEmpty()) {
            return List.of("application.properties", "application.yml", "application.yaml");
        }
        return List.of(path);
    }

    private Map<String, Object> tryLoadFile(ClassLoader classLoader, String resourceName) {
        try (InputStream is = classLoader.getResourceAsStream(resourceName)) {
            if (is == null) return null;
            if (resourceName.endsWith(".yml") || resourceName.endsWith(".yaml")) {
                return loadYaml(is);
            } else if (resourceName.endsWith(".properties")) {
                return loadProperties(is);
            } else {
                log.debug("Unknown resource format, skipping: {}", resourceName);
                return null;
            }
        } catch (Exception e) {
            log.warn("Failed to load resource: {}", resourceName, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYaml(InputStream is) {
        Yaml yaml = new Yaml();
        Object loaded = yaml.load(is);
        if (!(loaded instanceof Map)) return Collections.emptyMap();
        Map<String, Object> flat = new LinkedHashMap<>();
        flatten("", (Map<String, Object>) loaded, flat);
        return flat;
    }

    @SuppressWarnings("unchecked")
    private void flatten(String prefix, Map<String, Object> map, Map<String, Object> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flatten(key, (Map<String, Object>) value, result);
            } else {
                result.put(key, value);
            }
        }
    }

    private Map<String, Object> loadProperties(InputStream is) throws Exception {
        Properties props = new Properties();
        props.load(is);
        Map<String, Object> map = new LinkedHashMap<>();
        for (String name : props.stringPropertyNames()) {
            map.put(name, props.getProperty(name));
        }
        return map;
    }
}
