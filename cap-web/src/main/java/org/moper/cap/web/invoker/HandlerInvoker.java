package org.moper.cap.web.invoker;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.web.mapping.HandlerMapping;
import org.moper.cap.web.parameter.ParameterMetadata;
import org.moper.cap.web.parameter.ParameterResolverRegistry;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 控制器方法调用器。
 *
 * <p>使用 {@link ParameterResolverRegistry} 解析所有方法参数，
 * 然后通过反射调用控制器方法，并返回执行结果。
 */
public class HandlerInvoker {

    private final ParameterResolverRegistry parameterResolverRegistry;

    public HandlerInvoker(ParameterResolverRegistry parameterResolverRegistry) {
        this.parameterResolverRegistry = parameterResolverRegistry;
    }

    /**
     * 调用控制器方法并返回结果。
     *
     * @param mapping       路由映射
     * @param request       HTTP 请求
     * @param response      HTTP 响应
     * @param pathVariables 已提取的路径变量
     * @return 控制器方法的返回值（void 方法返回 null）
     * @throws Exception 调用过程中可能抛出的任意异常
     */
    public Object invoke(HandlerMapping mapping,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         Map<String, String> pathVariables) throws Exception {
        Method method = mapping.handlerMethod();
        List<ParameterMetadata> parameters = mapping.parameters();
        Object[] args = resolveArgs(parameters, request, response, pathVariables);
        return method.invoke(mapping.handler(), args);
    }

    private Object[] resolveArgs(List<ParameterMetadata> parameters,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  Map<String, String> pathVariables) throws Exception {
        Object[] args = new Object[parameters.size()];
        for (ParameterMetadata metadata : parameters) {
            args[metadata.index()] = parameterResolverRegistry.resolve(
                    metadata, request, response, pathVariables);
        }
        return args;
    }
}
