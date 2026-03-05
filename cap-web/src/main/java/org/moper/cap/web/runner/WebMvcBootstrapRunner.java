package org.moper.cap.web.runner;

import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.web.router.RouteRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Web MVC 框架启动器。
 *
 * <p>在框架启动阶段（order = 300）初始化 Web MVC 核心组件。
 */
@Slf4j
@RunnerMeta(type = RunnerType.FEATURE, order = 300, description = "Initializes Web MVC components")
public class WebMvcBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        log.info("初始化 Web MVC 模块");

        RouteRegistry routeRegistry = new RouteRegistry();
        routeRegistry.registerRoutesFromBeans(context.getBeanContainer());

        context.getBeanContainer().registerSingleton("routeRegistry", routeRegistry);

        log.info("Web MVC 模块初始化完成，共注册 {} 个路由",
                 routeRegistry.getAllRoutes().size());
    }
}
