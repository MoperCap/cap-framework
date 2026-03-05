package org.moper.cap.web.runner;

import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.web.binder.ParameterBinderRegistry;
import org.moper.cap.web.binder.ParameterMetadata;
import org.moper.cap.web.binder.impl.DefaultParameterBinderRegistry;
import org.moper.cap.web.router.RouteDefinition;
import org.moper.cap.web.router.RouteRegistry;
import org.moper.cap.web.util.ControllerUtils;
import org.moper.cap.web.util.RouterAnnotationResolver;
import org.moper.cap.web.util.RouterAnnotationResolver.RouterAnnotation;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

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

        BeanContainer beanContainer = context.getBeanContainer();
        RouteRegistry routeRegistry = new RouteRegistry();
        ParameterBinderRegistry parameterBinderRegistry = new DefaultParameterBinderRegistry();

        scanAndRegisterRoutes(beanContainer, routeRegistry);

        beanContainer.registerSingleton("routeRegistry", routeRegistry);
        beanContainer.registerSingleton("parameterBinderRegistry", parameterBinderRegistry);

        log.info("Web MVC 模块初始化完成，共注册 {} 个路由", routeRegistry.getAllRoutes().size());
    }

    /**
     * 扫描 BeanContainer 中的所有控制器并注册路由
     */
    private void scanAndRegisterRoutes(BeanContainer beanContainer, RouteRegistry routeRegistry) {
        String[] beanNames = beanContainer.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            BeanDefinition definition = beanContainer.getBeanDefinition(beanName);
            Class<?> beanClass = definition.type();

            if (!ControllerUtils.isController(beanClass)) {
                continue;
            }

            Object beanInstance = beanContainer.getBean(beanName);
            String basePath = RouterAnnotationResolver.resolve(beanClass);

            for (Method method : beanClass.getDeclaredMethods()) {
                RouterAnnotation routerAnnotation = RouterAnnotationResolver.resolve(method);

                if (routerAnnotation == null) {
                    continue;
                }

                String fullPath = basePath + routerAnnotation.path();
                List<ParameterMetadata> parameters = extractParameters(method);

                RouteDefinition route = new RouteDefinition(
                        fullPath,
                        routerAnnotation.httpMethod(),
                        beanInstance,
                        method,
                        parameters
                );

                routeRegistry.registerRoute(route);
            }
        }
    }

    /**
     * 提取方法的参数元数据
     */
    private List<ParameterMetadata> extractParameters(Method method) {
        List<ParameterMetadata> metadata = new ArrayList<>();
        Parameter[] parameters = method.getParameters();

        for (Parameter param : parameters) {
            metadata.add(new ParameterMetadata(
                    param,
                    param.getName(),
                    param.getType()
            ));
        }

        return metadata;
    }
}
