package org.moper.cap.web.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.web.annotation.Controller;
import org.moper.cap.web.annotation.ExceptionHandler;
import org.moper.cap.web.annotation.RestController;
import org.moper.cap.web.model.ErrorResponse;
import org.moper.cap.web.model.ExceptionHandlerInfo;
import org.moper.cap.web.model.ResponseEntity;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 异常处理器注册表。
 *
 * <p>扫描所有控制器 Bean 中标注了 {@link ExceptionHandler} 的方法，
 * 构建异常类型到处理器方法的映射，并在请求处理过程中捕获异常后调用匹配的处理器。
 */
@Slf4j
public class ExceptionResolverRegistry {

    private final List<ExceptionHandlerInfo> handlers = new ArrayList<>();
    private final ObjectMapper objectMapper;

    public ExceptionResolverRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 扫描容器中所有控制器 Bean，注册 {@link ExceptionHandler} 方法。
     *
     * @param container Bean 容器
     */
    public void register(BeanContainer container) {
        Map<String, Object> controllers = new LinkedHashMap<>();
        controllers.putAll(container.getBeansWithAnnotation(Controller.class));
        controllers.putAll(container.getBeansWithAnnotation(RestController.class));

        int order = 0;
        for (Map.Entry<String, Object> entry : controllers.entrySet()) {
            Object handler = entry.getValue();
            for (Method method : handler.getClass().getDeclaredMethods()) {
                ExceptionHandler annotation = method.getAnnotation(ExceptionHandler.class);
                if (annotation == null) continue;
                Class<? extends Throwable>[] types = annotation.value();
                if (types.length == 0) {
                    // infer from method parameters
                    types = inferExceptionTypes(method);
                }
                for (Class<? extends Throwable> exType : types) {
                    method.setAccessible(true);
                    handlers.add(new ExceptionHandlerInfo(handler, method, exType, order++));
                }
            }
        }
        // Sort by order
        handlers.sort(Comparator.comparingInt(ExceptionHandlerInfo::order));
        log.info("ExceptionResolverRegistry: registered {} exception handlers", handlers.size());
    }

    /**
     * 尝试用已注册的异常处理器处理给定异常，并将结果写入响应。
     *
     * @param ex       捕获的异常
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @return 如果找到并成功处理则返回 {@code true}，否则返回 {@code false}
     */
    public boolean resolve(Throwable ex, HttpServletRequest request, HttpServletResponse response) {
        ExceptionHandlerInfo info = findHandler(ex);
        if (info == null) {
            return false;
        }
        try {
            Object result = info.method().invoke(info.handler(), buildArgs(info.method(), ex));
            writeResult(result, response);
            return true;
        } catch (Exception invokeEx) {
            log.error("Exception handler invocation failed", invokeEx);
            return false;
        }
    }

    // ──────────────── 私有方法 ────────────────

    private ExceptionHandlerInfo findHandler(Throwable ex) {
        // Exact match first
        for (ExceptionHandlerInfo info : handlers) {
            if (info.exceptionType().equals(ex.getClass())) {
                return info;
            }
        }
        // Assignable match
        for (ExceptionHandlerInfo info : handlers) {
            if (info.exceptionType().isAssignableFrom(ex.getClass())) {
                return info;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Throwable>[] inferExceptionTypes(Method method) {
        List<Class<? extends Throwable>> types = new ArrayList<>();
        for (Class<?> paramType : method.getParameterTypes()) {
            if (Throwable.class.isAssignableFrom(paramType)) {
                types.add((Class<? extends Throwable>) paramType);
            }
        }
        return types.toArray(new Class[0]);
    }

    private Object[] buildArgs(Method method, Throwable ex) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (Throwable.class.isAssignableFrom(paramTypes[i])) {
                args[i] = ex;
            }
        }
        return args;
    }

    private void writeResult(Object result, HttpServletResponse response) throws Exception {
        if (result == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        if (result instanceof ResponseEntity<?> entity) {
            response.setStatus(entity.status());
            entity.headers().forEach(response::setHeader);
            if (entity.body() != null) {
                writeJson(entity.body(), response);
            }
        } else if (result instanceof String str) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain;charset=UTF-8");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(str);
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(result, response);
        }
    }

    private void writeJson(Object obj, HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(obj));
    }

    /**
     * 默认的异常处理：写出标准 500 错误响应。
     *
     * @param ex       异常
     * @param response HTTP 响应
     */
    public void handleDefault(Throwable ex, HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(ErrorResponse.of(500, ex.getMessage() != null ? ex.getMessage() : "Internal Server Error"),
                    response);
        } catch (Exception writeEx) {
            log.error("Failed to write default error response", writeEx);
        }
    }
}
