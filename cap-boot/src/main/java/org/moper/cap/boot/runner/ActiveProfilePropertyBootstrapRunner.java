package org.moper.cap.boot.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.boot.util.ResourceFileLoader;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.constants.PropertyArguments;
import org.moper.cap.core.constants.ResourceConstants;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.exception.ResourceLoaderException;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;

import java.io.InputStream;
import java.util.*;

@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 130, description = "Loads application-{profile}.yaml/.properties resources for active environment, flatten and inject via publisher")
public class ActiveProfilePropertyBootstrapRunner implements BootstrapRunner {

    private final ObjectMapper YamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * 根据application.profiles.active属性键的值扫描并加载对应Profile配置文件，
     * 并注册到Officer中 </br>
     *
     * @param context 框架初始化阶段系统上下文
     * @throws Exception 执行过程中可能抛出的异常
     */
    @Override
    public void initialize(BootstrapContext context) throws Exception {
        PropertyOfficer officer = context.getPropertyOfficer();

        Object profileValue = officer.getRawPropertyValue(PropertyArguments.SUPPORTED_ACTIVE_PROFILE_PROPERTY_KEY);
        if(profileValue == null || profileValue.toString().isBlank()){
            log.info("No profile specified ({}); skip loading profile config.", PropertyArguments.SUPPORTED_ACTIVE_PROFILE_PROPERTY_KEY);
            return;
        }

        String profile = profileValue.toString().trim();
        log.info("Active profile: {}", profile);

        // 获取资源扫描路径集合
        Collection<String> resourceScanPaths = context.getConfigurationClassParser().getResourceScanPaths();
        // 扫描资源扫描路径下的所有资源文件，过滤出符合条件的文件，并记录日志
        try (ScanResult scan = new ClassGraph().acceptPaths(resourceScanPaths.toArray(new String[0])).scan()) {

            // 扫描所有符合条件的资源文件
            List<Resource> resources = new ArrayList<>();
            for(String suffix : ResourceConstants.SUPPORTED_RESOURCE_SUFFIXES){
                resources.addAll(scan.getResourcesWithLeafName(ResourceConstants.SUPPORTED_RESOURCE_PREFIX + "-" + profile + suffix));
            }
            // 若找不到任何相关的资源文件，抛出异常
            if(resources.isEmpty()) throw new ResourceLoaderException("No resource file found for active profile: " + profile);
            // 若找到多个相关的资源文件，抛出异常，提示存在歧义
            else if(resources.size() > 1) throw new ResourceLoaderException("Multiple resource files found for active profile: " + profile);

            // 提取资源文件
            Resource resource = resources.get(0);
            // 资源文件路径
            String resourcePath = resource.getPath();
            // 资源文件名称
            String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
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

            // 如果没有解析到任何属性，记录日志
            if (flatProps.isEmpty()) {
                log.info("No properties found to publish in [{}]", resourcePath);
            }

            // 获取系统内部属性管理平台实例，并根据资源文件路径构建唯一的属性发布者名称，获取对应的属性发布者实例
            final String publisherName = ResourceConstants.getActiveProfileResourcePublisherName(profile);
            PropertyPublisher publisher = officer.getPublisher(publisherName);

            // 将扁平化后的属性转换为属性操作列表，并通过属性发布者发布这些属性，记录日志
            List<PropertyOperation> operations = new ArrayList<>();
            flatProps.forEach((key, value) -> {
                operations.add(new PropertySetOperation(key, value));
            });
            // 发布属性操作列表
            publisher.publish(operations.toArray(new PropertyOperation[0]));
        }

    }
}
