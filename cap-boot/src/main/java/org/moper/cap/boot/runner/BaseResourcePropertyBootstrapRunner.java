package org.moper.cap.boot.runner;

import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.constants.AppConstants;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.property.officer.PropertyOfficer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RunnerMeta(type = RunnerType.KERNEL, order = 20, name = "ResourcePropertyBootstrapRunner",
        description = "Scans resource paths for application.yaml or application.properties file, flattens and registers properties")
public class BaseResourcePropertyBootstrapRunner implements BootstrapRunner {

    /**
     * 框架初始化阶段执行器 </br>
     *
     * @param context 框架初始化阶段系统上下文
     * @throws Exception 执行过程中可能抛出的异常
     */
    @Override
    public void initialize(BootstrapContext context) throws Exception {
        // 获取资源扫描路径集合
        Collection<String> resourceScanPaths = context.getConfigurationClassParser().getResourceScanPaths();
        List<String> supportedBaseResources = List.of(AppConstants.SUPPORTED_BASE_RESOURCES);
        // TODO: 如果资源扫描路径集合本身出现嵌套，该如何处理
        List<Path> files = new ArrayList<>();
        for(String resourceScanPath : resourceScanPaths) {
            Path path = Paths.get(resourceScanPath);
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(p-> supportedBaseResources.contains(p.getFileName().toString()))
                    .forEach(files::add);
        }

        // 获取类加载器
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if(classLoader == null) classLoader = getClass().getClassLoader();
        // 获取属性管理平台
        PropertyOfficer officer = context.getPropertyOfficer();


    }
}
