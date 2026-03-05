package org.moper.cap.web.runner;

import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

/**
 * Web MVC 框架启动器。
 *
 * <p>在框架启动阶段（order = 300）初始化 Web MVC 核心组件。
 */
@RunnerMeta(type = RunnerType.FEATURE, order = 300, description = "Initializes Web MVC components")
public class WebMvcBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        // TODO: initialize Web MVC components
    }
}
