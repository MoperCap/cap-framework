package org.moper.cap.boot.runner;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.constants.ResourceConstants;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 100, description = "Register OS env variables and JVM system properties in Officer via dedicated publishers")
public class SystemPropertyBootstrapRunner implements BootstrapRunner {
    /**
     * 系统属性注册器 </br>
     *
     * @param context 框架初始化阶段系统上下文
     * @throws Exception 执行过程中可能抛出的异常
     */
    @Override
    public void initialize(BootstrapContext context) throws Exception {
        PropertyOfficer officer = context.getPropertyOfficer();
        PropertyPublisher publisher = officer.getPublisher(ResourceConstants.SYSTEM_PROPERTY_PUBLISHER);

        // 注册 OS 环境变量
        Map<String, String> env = System.getenv();
        if(env == null || env.isEmpty()) {
            log.warn("OS environment variables are null or empty (System.getenv() == null/empty); please verify your runtime security settings!");
            return;
        }
        List<PropertyOperation> envOperations = new ArrayList<>();
        for(Map.Entry<String, String> entry : env.entrySet()) {
            envOperations.add(new PropertySetOperation(entry.getKey(), entry.getValue()));
        }
        publisher.publish(envOperations.toArray(new PropertyOperation[0]));

        // 注册 JVM 系统属性
        Properties system = System.getProperties();
        if(system == null || system.isEmpty()) {
            log.warn("JVM system properties are null or empty (System.getProperties() == null/empty); please check your JVM launch options and security policy!");
            return;
        }
        List<PropertyOperation> systemOperations = new ArrayList<>();
        for(Map.Entry<Object, Object> entry : system.entrySet()) {
            systemOperations.add(new PropertySetOperation(entry.getKey().toString(), entry.getValue().toString()));
        }
        publisher.publish(systemOperations.toArray(new PropertyOperation[0]));

    }
}
