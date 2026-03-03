package org.moper.cap.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.web.annotation.ResponseBody;
import org.moper.cap.web.annotation.RestController;
import org.moper.cap.web.model.HandlerMapping;

import java.nio.charset.StandardCharsets;

/**
 * JSON 返回值处理器。
 *
 * <p>当控制器方法带有 {@link ResponseBody} 或 {@link RestController} 注解时，
 * 使用 Jackson 将返回值序列化为 JSON 写入响应体。
 */
public class JsonReturnValueHandler implements ReturnValueHandler {

    private final ObjectMapper objectMapper;

    public JsonReturnValueHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(Class<?> returnType, HandlerMapping mapping) {
        if (returnType == null || returnType == void.class || returnType == Void.class) {
            return false;
        }
        return isResponseBody(mapping);
    }

    @Override
    public void handle(Object returnValue,
                       HandlerMapping mapping,
                       HttpServletRequest request,
                       HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_OK);
        if (returnValue != null) {
            String json = objectMapper.writeValueAsString(returnValue);
            response.getWriter().write(json);
        }
    }

    private boolean isResponseBody(HandlerMapping mapping) {
        Class<?> controllerClass = mapping.handler().getClass();
        return controllerClass.isAnnotationPresent(RestController.class)
                || controllerClass.isAnnotationPresent(ResponseBody.class)
                || mapping.handlerMethod().isAnnotationPresent(ResponseBody.class);
    }
}
