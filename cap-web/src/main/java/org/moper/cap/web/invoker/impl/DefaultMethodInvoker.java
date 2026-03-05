package org.moper.cap.web.invoker.impl;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.web.invoker.MethodInvoker;
import org.moper.cap.web.router.RouteDefinition;
import org.moper.cap.web.binder.ParameterBinderRegistry;
import org.moper.cap.web.binder.ParameterMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认的方法调用器实现
 *
 * 流程：
 * 1. 从 RouteDefinition 获取控制器对象和方法
 * 2. 获取方法的所有参数元数据
 * 3. 通过 ParameterBinderRegistry 逐个绑定参数
 * 4. 使用反射调用方法
 * 5. 返回方法结果
 */
@Slf4j
public class DefaultMethodInvoker implements MethodInvoker {

    private final ParameterBinderRegistry binderRegistry;

    public DefaultMethodInvoker(ParameterBinderRegistry binderRegistry) {
        if (binderRegistry == null) {
            throw new IllegalArgumentException("ParameterBinderRegistry cannot be null");
        }
        this.binderRegistry = binderRegistry;
    }

    @Override
    public Object invoke(RouteDefinition mapping,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         Map<String, String> pathVariables) throws Exception {

        if (mapping == null) {
            throw new IllegalArgumentException("RouteDefinition cannot be null");
        }
        if (request == null || response == null) {
            throw new IllegalArgumentException("HttpServletRequest/Response cannot be null");
        }
        if (pathVariables == null) {
            pathVariables = Map.of();
        }

        Object controller = mapping.controller();
        Method method = mapping.controllerMethod();

        log.debug("准备调用方法: {}.{}",
                controller.getClass().getSimpleName(),
                method.getName());

        List<ParameterMetadata> parameterMetadataList = mapping.parameters();

        Object[] args = bindParameters(parameterMetadataList, request, response, pathVariables);

        try {
            method.setAccessible(true);
            Object result = method.invoke(controller, args);

            log.debug("方法调用成功: {}.{} (返回类型: {})",
                    controller.getClass().getSimpleName(),
                    method.getName(),
                    method.getReturnType().getSimpleName());

            return result;
        } catch (IllegalAccessException e) {
            log.error("方法访问异常: {}.{}",
                    controller.getClass().getSimpleName(),
                    method.getName(), e);
            throw new RuntimeException("Failed to invoke method: " + method.getName(), e);
        } catch (IllegalArgumentException e) {
            log.error("方法参数异常: {}.{}",
                    controller.getClass().getSimpleName(),
                    method.getName(), e);
            throw new RuntimeException("Invalid method arguments for: " + method.getName(), e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            log.error("方法执行异常: {}.{}",
                    controller.getClass().getSimpleName(),
                    method.getName(),
                    e.getCause());
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw new RuntimeException("Method invocation failed: " + method.getName(), e.getCause());
        }
    }

    /**
     * 绑定方法的所有参数
     *
     * @param parameterMetadataList 参数元数据列表
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param pathVariables 路径变量
     * @return 绑定后的参数数组
     * @throws Exception 参数绑定异常
     */
    private Object[] bindParameters(List<ParameterMetadata> parameterMetadataList,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    Map<String, String> pathVariables) throws Exception {

        if (parameterMetadataList == null || parameterMetadataList.isEmpty()) {
            return new Object[0];
        }

        List<Object> args = new ArrayList<>(parameterMetadataList.size());

        for (ParameterMetadata metadata : parameterMetadataList) {
            try {
                Object value = binderRegistry.resolve(metadata, request, response, pathVariables);
                args.add(value);

                log.debug("参数绑定成功: {} = {}", metadata.name(), value);
            } catch (Exception e) {
                log.error("参数绑定失败: {}", metadata.name(), e);
                throw new RuntimeException("Failed to bind parameter: " + metadata.name(), e);
            }
        }

        return args.toArray();
    }
}
