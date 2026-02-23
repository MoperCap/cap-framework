package org.moper.cap.boot.initializer;

import org.moper.cap.bootstrap.Initializer;
import org.moper.cap.bootstrap.InitializerType;
import org.moper.cap.config.ConfigClassResourceViewContext;
import org.moper.cap.config.impl.DefaultConfigClassResourceViewContext;
import org.moper.cap.context.BootstrapContext;
import org.moper.cap.environment.MapPropertySource;
import org.moper.cap.exception.ContextException;
import org.moper.cap.exception.InitializerException;
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
    public void initialize(BootstrapContext context) throws ContextException {
        String primarySourceName = context.getEnvironment().getProperty("cap.primary-source");
        if (primarySourceName == null) {
            log.warn("cap.primary-source not set, skipping property loading");
            return;
        }

        Class<?> primarySource;
        try {
            primarySource = Class.forName(primarySourceName);
        } catch (ClassNotFoundException e) {
            throw new InitializerException("Cannot load primary source class: " + primarySourceName, e);
        }

        ConfigClassResourceViewContext viewContext = new DefaultConfigClassResourceViewContext(primarySource);
        Collection<String> resourcePaths = viewContext.getResourceScanPaths();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) classLoader = getClass().getClassLoader();

        for (String path : resourcePaths) {
            loadResourcePath(context, classLoader, path);
        }
    }

    private void loadResourcePath(BootstrapContext context, ClassLoader classLoader, String path) {
        if (path == null || path.isEmpty()) {
            // Load default config files from classpath root
            tryLoad(context, classLoader, "application.properties");
            tryLoad(context, classLoader, "application.yml");
            tryLoad(context, classLoader, "application.yaml");
        } else {
            tryLoad(context, classLoader, path);
        }
    }

    private void tryLoad(BootstrapContext context, ClassLoader classLoader, String resourceName) {
        try (InputStream is = classLoader.getResourceAsStream(resourceName)) {
            if (is == null) return;
            Map<String, Object> props;
            if (resourceName.endsWith(".yml") || resourceName.endsWith(".yaml")) {
                props = loadYaml(is);
            } else if (resourceName.endsWith(".properties")) {
                props = loadProperties(is);
            } else {
                log.debug("Unknown resource format, skipping: {}", resourceName);
                return;
            }
            if (!props.isEmpty()) {
                context.getEnvironment().addPropertySource(new MapPropertySource(resourceName, props, 10));
                log.debug("Loaded property source from: {}", resourceName);
            }
        } catch (Exception e) {
            log.warn("Failed to load resource: {}", resourceName, e);
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
