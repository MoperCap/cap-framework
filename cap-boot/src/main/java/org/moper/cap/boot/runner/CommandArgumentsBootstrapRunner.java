package org.moper.cap.boot.runner;

import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.constants.AppConstants;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.property.event.PropertyManifest;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.publisher.impl.DefaultPropertyPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunnerMeta(type = RunnerType.KERNEL, order = 10, name = "CommandArgumentsBootstrapRunner", description = "Parse Command Line Arguments and set them as properties in the PropertyOfficer")
public class CommandArgumentsBootstrapRunner implements BootstrapRunner {
    /**
     * 以默认方式创建名为"command-property-publisher"的PropertyPublisher，
     * 并将Bootstrap中所有的命令行参数注册到PropertyOfficer中 </br>
     *
     * @param context 框架初始化阶段系统上下文
     * @throws Exception 执行过程中可能抛出的异常
     */
    @Override
    public void initialize(BootstrapContext context) throws Exception {
        PropertyOfficer officer = context.getPropertyOfficer();

        PropertyPublisher publisher = officer.getPublisher(AppConstants.COMMAND_PUBLISHER,
                () -> new DefaultPropertyPublisher(AppConstants.COMMAND_PUBLISHER, officer));

        List<PropertyOperation> operations = new ArrayList<>();
        Map<String, Object> arguments = context.getCommandArgumentParser().parse();
        arguments.forEach((key, value) -> {
            PropertySetOperation operation = new PropertySetOperation(key, value);
            operations.add(operation);
        });
        publisher.publish(operations.toArray(new PropertyOperation[0]));
    }
}
