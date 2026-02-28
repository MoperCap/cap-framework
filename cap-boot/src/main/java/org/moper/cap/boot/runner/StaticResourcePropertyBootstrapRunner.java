package org.moper.cap.boot.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.boot.util.ResourceFileLoader;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.constants.ResourceConstants;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 30, description = "Scans resource paths for application.yaml or application.properties file, flattens and registers properties")
public class StaticResourcePropertyBootstrapRunner implements BootstrapRunner {

    private final Pattern pattern = Pattern.compile("^application.*\\.(yaml|yml|properties)$");
    private final ObjectMapper YamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * 基础资源配置文件执行器 </br>
     * <p>
     * 根据配置类上指定的资源扫描路径集合，
     * 扫描这些路径下的所有application.yaml、application.yml、application.properties文件，
     * 解析这些文件中的属性内容并注册到属性管理平台。 </br>
     *
     * @param context 框架初始化阶段系统上下文
     * @throws Exception 执行过程中可能抛出的异常
     */
    @Override
    public void initialize(BootstrapContext context) throws Exception {
        // 获取资源扫描路径集合
        Collection<String> resourceScanPaths = context.getConfigurationClassParser().getResourceScanPaths();
        // 扫描资源扫描路径下的所有资源文件，过滤出符合条件的文件，并记录日志
        try (ScanResult scan = new ClassGraph().acceptPaths(resourceScanPaths.toArray(new String[0])).scan()) {
            for (Resource resource : scan.getAllResources()) {
                // 资源文件路径
                String resourcePath = resource.getPath();
                // 资源文件名称
                String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
                // 过滤出符合条件的资源文件，记录日志
                if (!pattern.matcher(fileName).matches()) continue;
                log.info("Found resource file: {}", resourcePath);

                // 解析资源文件内容，支持YAML和Properties格式，并将嵌套结构扁平化为键值对
                Map<String, Object> flatProps = new LinkedHashMap<>();
                try (InputStream in = resource.open()) {
                    if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
                        flatProps = ResourceFileLoader.loadYaml(in, true);
                    }
                    // 解析Properties文件
                    else if (fileName.endsWith(".properties")) {
                        flatProps = ResourceFileLoader.loadProperties(in);
                    }
                }

                // 如果没有解析到任何属性，记录日志并继续处理下一个文件
                if (flatProps.isEmpty()) {
                    log.info("No properties found to publish in [{}]", resourcePath);
                }

                // 获取系统内部属性管理平台实例，并根据资源文件路径构建唯一的属性发布者名称，获取对应的属性发布者实例
                PropertyOfficer officer = context.getPropertyOfficer();
                final String publisherName = ResourceConstants.getResourcePublisherNam(resourcePath);
                PropertyPublisher publisher = officer.getPublisher(publisherName);

                // 将扁平化后的属性转换为属性操作列表，并通过属性发布者发布这些属性，记录日志
                List<PropertyOperation> operations = new ArrayList<>();
                flatProps.forEach((key, value) -> {
                    operations.add(new PropertySetOperation(key, value));
                });
                // 发布属性操作列表
                publisher.publish(operations.toArray(new PropertyOperation[0]));
                log.info("Registered {} properties from [{}]", flatProps.size(), resourcePath);
            }
        }
    }
}
