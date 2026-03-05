package org.moper.cap.web.dispatcher;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.interceptor.InterceptorRegistry;
import org.moper.cap.web.interceptor.RequestInterceptor;
import org.moper.cap.web.invoker.MethodInvoker;
import org.moper.cap.web.router.RouteDefinition;
import org.moper.cap.web.router.RouteRegistry;
import org.moper.cap.web.view.ViewHandlerRegistry;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 前端控制器 - 接收 HTTP 请求并分发到对应的路由处理
 *
 * 核心职责：
 * 1. 接收所有 HTTP 请求
 * 2. 通过 RouteRegistry 查找对应的路由
 * 3. 执行请求拦截器
 * 4. 通过 MethodInvoker 调用控制器方法
 * 5. 通过 ViewHandlerRegistry 处理返回值
 * 6. 处理异常和 404
 */
@Slf4j
public class DispatcherServlet extends HttpServlet {

    @Setter
    private RouteRegistry routeRegistry;

    @Setter
    private MethodInvoker methodInvoker;

    @Setter
    private ViewHandlerRegistry viewHandlerRegistry;

    @Setter
    private InterceptorRegistry interceptorRegistry;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String requestPath = extractRequestPath(request);
            HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());

            log.debug("处理请求: {} {}", httpMethod, requestPath);

            // 1. 通过 RouteRegistry 查找路由
            Optional<RouteDefinition> routeOpt = routeRegistry.findRoute(requestPath, httpMethod);
            if (routeOpt.isEmpty()) {
                handleNotFound(request, response, requestPath, httpMethod);
                return;
            }

            RouteDefinition routeDefinition = routeOpt.get();
            log.debug("找到路由映射: {}", routeDefinition);

            // 2. 提取路径变量
            Map<String, String> pathVariables = routeDefinition.extractPathVariables(requestPath);

            // 3. 执行拦截器 preHandle
            List<RequestInterceptor> interceptors = interceptorRegistry != null
                ? interceptorRegistry.getInterceptors()
                : List.of();

            for (RequestInterceptor interceptor : interceptors) {
                if (!interceptor.preHandle(request, response, routeDefinition)) {
                    log.debug("拦截器中止请求处理");
                    return;
                }
            }

            // 4. 调用方法
            Object returnValue = null;
            try {
                returnValue = methodInvoker.invoke(routeDefinition, request, response, pathVariables);
                log.debug("方法调用成功，返回值: {}", returnValue);
            } catch (Exception e) {
                log.error("方法调用异常", e);
                handleException(interceptors, request, response, routeDefinition, e);
                return;
            }

            // 5. 执行拦截器 postHandle
            for (RequestInterceptor interceptor : interceptors) {
                try {
                    interceptor.postHandle(request, response, routeDefinition, returnValue);
                } catch (Exception e) {
                    log.error("拦截器 postHandle 异常", e);
                    handleException(interceptors, request, response, routeDefinition, e);
                    return;
                }
            }

            // 6. 处理返回值
            Class<?> returnType = getReturnType(returnValue);
            viewHandlerRegistry.handle(returnValue, returnType, routeDefinition, request, response);
            log.debug("返回值处理完成");

        } catch (Exception e) {
            log.error("请求处理异常", e);
            handleInternalError(request, response, e);
        }
    }

    /**
     * 执行拦截器 afterCompletion
     */
    private void handleException(List<RequestInterceptor> interceptors,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                RouteDefinition routeDefinition,
                                Exception exception) {
        for (RequestInterceptor interceptor : interceptors) {
            try {
                interceptor.afterCompletion(request, response, routeDefinition, exception);
            } catch (Exception e) {
                log.error("拦截器 afterCompletion 异常", e);
            }
        }
    }

    /**
     * 处理 404 Not Found
     */
    private void handleNotFound(HttpServletRequest request,
                               HttpServletResponse response,
                               String requestPath,
                               HttpMethod httpMethod) throws IOException {
        log.warn("未找到路由: {} {}", httpMethod, requestPath);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"404 Not Found\"}");
    }

    /**
     * 处理 500 Internal Server Error
     */
    private void handleInternalError(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Exception exception) throws IOException {
        log.error("服务器内部错误", exception);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"500 Internal Server Error\"}");
    }

    /**
     * 提取请求路径（去掉 contextPath）
     */
    private String extractRequestPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        String path = requestUri.substring(contextPath.length());
        return path.isEmpty() ? "/" : path;
    }

    /**
     * 获取返回值的类型
     */
    private Class<?> getReturnType(Object returnValue) {
        if (returnValue == null) {
            return Void.class;
        }
        return returnValue.getClass();
    }
}
