package org.moper.cap.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.invoker.HandlerInvoker;
import org.moper.cap.web.model.HandlerMapping;
import org.moper.cap.web.registry.ExceptionResolverRegistry;
import org.moper.cap.web.registry.HandlerMappingRegistry;
import org.moper.cap.web.registry.ReturnValueHandlerRegistry;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * 前端控制器 Servlet，实现完整的 MVC 请求处理流程。
 *
 * <p>请求处理流程：
 * <ol>
 *   <li>提取 HTTP 请求方法和路径</li>
 *   <li>通过 {@link HandlerMappingRegistry} 查找匹配的 {@link HandlerMapping}</li>
 *   <li>从请求路径中提取路径变量</li>
 *   <li>通过 {@link HandlerInvoker} 解析参数并调用控制器方法</li>
 *   <li>通过 {@link ReturnValueHandlerRegistry} 处理返回值</li>
 *   <li>通过 {@link ExceptionResolverRegistry} 处理异常</li>
 * </ol>
 */
@Slf4j
public class DispatcherServlet extends HttpServlet {

    private final HandlerMappingRegistry handlerMappingRegistry;
    private final HandlerInvoker handlerInvoker;
    private final ReturnValueHandlerRegistry returnValueHandlerRegistry;
    private final ExceptionResolverRegistry exceptionResolverRegistry;

    public DispatcherServlet(HandlerMappingRegistry handlerMappingRegistry,
                             HandlerInvoker handlerInvoker,
                             ReturnValueHandlerRegistry returnValueHandlerRegistry,
                             ExceptionResolverRegistry exceptionResolverRegistry) {
        this.handlerMappingRegistry = handlerMappingRegistry;
        this.handlerInvoker = handlerInvoker;
        this.returnValueHandlerRegistry = returnValueHandlerRegistry;
        this.exceptionResolverRegistry = exceptionResolverRegistry;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String methodStr = request.getMethod();
        String requestPath = request.getRequestURI();

        log.debug("DispatcherServlet: {} {}", methodStr, requestPath);

        HttpMethod httpMethod;
        try {
            httpMethod = HttpMethod.fromString(methodStr);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "Unsupported HTTP method: " + methodStr);
            return;
        }

        Optional<HandlerMapping> mappingOpt = handlerMappingRegistry.findMapping(requestPath, httpMethod);
        if (mappingOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "No handler found for " + methodStr + " " + requestPath);
            return;
        }

        HandlerMapping mapping = mappingOpt.get();
        Map<String, String> pathVariables = mapping.extractPathVariables(requestPath);

        try {
            Object returnValue = handlerInvoker.invoke(mapping, request, response, pathVariables);
            Class<?> returnType = mapping.handlerMethod().getReturnType();
            if (returnType == void.class) {
                returnType = null;
            }
            returnValueHandlerRegistry.handle(returnValue, returnType, mapping, request, response);
        } catch (Exception ex) {
            log.error("Error processing request: {} {}", methodStr, requestPath, ex);
            if (!response.isCommitted()) {
                if (!exceptionResolverRegistry.resolve(ex, request, response)) {
                    exceptionResolverRegistry.handleDefault(ex, response);
                }
            }
        }
    }
}
