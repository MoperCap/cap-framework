package org.moper.cap.web.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.common.exception.ExceptionResolverRegistry;
import org.moper.cap.web.result.ReturnValueHandlerRegistry;
import org.moper.cap.web.mapping.registry.HandlerMappingRegistry;
import org.moper.cap.web.invoker.HandlerInvoker;
import org.moper.cap.web.parameter.ParameterResolverRegistry;
import org.moper.cap.web.servlet.DispatcherServlet;

/**
 * Web MVC 框架启动器。
 *
 * <p>在框架启动阶段（order = 300）初始化所有 Web MVC 核心组件：
 * <ul>
 *   <li>{@link HandlerMappingRegistry} — 扫描并注册路由映射</li>
 *   <li>{@link ParameterResolverRegistry} — 注册参数解析器</li>
 *   <li>{@link ReturnValueHandlerRegistry} — 注册返回值处理器</li>
 *   <li>{@link ExceptionResolverRegistry} — 注册异常处理器</li>
 *   <li>{@link HandlerInvoker} — 控制器方法调用器</li>
 *   <li>{@link DispatcherServlet} — 前端控制器</li>
 * </ul>
 *
 * <p>所有初始化完成的组件会注册为单例 Bean，方便其他模块（如嵌入式服务器）获取
 * {@link DispatcherServlet} 实例并挂载到 Servlet 容器中。
 */
@Slf4j
@RunnerMeta(type = RunnerType.FEATURE, order = 300, description = "Initializes Web MVC registries and DispatcherServlet")
public class WebMvcBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        BeanContainer container = context.getBeanContainer();

        // Jackson ObjectMapper with Java 8 time support
        ObjectMapper objectMapper = createObjectMapper();

        // Route registration
        HandlerMappingRegistry handlerMappingRegistry = new HandlerMappingRegistry();
        handlerMappingRegistry.register(container);

        // Parameter resolution
        ParameterResolverRegistry parameterResolverRegistry =
                new ParameterResolverRegistry(objectMapper, context.getTypeResolver());

        // Return value handling
        ReturnValueHandlerRegistry returnValueHandlerRegistry =
                new ReturnValueHandlerRegistry(objectMapper);

        // Exception handling
        ExceptionResolverRegistry exceptionResolverRegistry = new ExceptionResolverRegistry();

        // Handler invoker
        HandlerInvoker handlerInvoker = new HandlerInvoker(parameterResolverRegistry);

        // DispatcherServlet
        DispatcherServlet dispatcherServlet = new DispatcherServlet(
                handlerMappingRegistry, handlerInvoker,
                returnValueHandlerRegistry, exceptionResolverRegistry);

        // Register all components as singletons for external access
        container.registerSingleton("handlerMappingRegistry", handlerMappingRegistry);
        container.registerSingleton("parameterResolverRegistry", parameterResolverRegistry);
        container.registerSingleton("returnValueHandlerRegistry", returnValueHandlerRegistry);
        container.registerSingleton("exceptionResolverRegistry", exceptionResolverRegistry);
        container.registerSingleton("handlerInvoker", handlerInvoker);
        container.registerSingleton("dispatcherServlet", dispatcherServlet);

        log.info("WebMvcBootstrapRunner: Web MVC initialized successfully");
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Register Java Time module for LocalDateTime etc.
        try {
            mapper.registerModule(new JavaTimeModule());
        } catch (Exception e) {
            log.debug("JavaTimeModule not available, skipping registration");
        }
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
