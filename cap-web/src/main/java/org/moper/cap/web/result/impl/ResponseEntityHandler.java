package org.moper.cap.web.result.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.common.priority.Priority;
import org.moper.cap.web.result.ReturnValueHandler;
import org.moper.cap.web.mapping.HandlerMapping;
import org.moper.cap.web.result.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * {@link ResponseEntity} 返回值处理器。
 *
 * <p>处理控制器方法返回 {@link ResponseEntity} 的情形，
 * 自动设置状态码、响应头，并将响应体序列化为 JSON。
 */
@Priority(100)
public class ResponseEntityHandler implements ReturnValueHandler {

    private ObjectMapper objectMapper;

    public ResponseEntityHandler() {
    }

    public ResponseEntityHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(Class<?> returnType, HandlerMapping mapping) {
        return returnType != null && ResponseEntity.class.isAssignableFrom(returnType);
    }

    @Override
    public void handle(Object returnValue,
                       HandlerMapping mapping,
                       HttpServletRequest request,
                       HttpServletResponse response) throws Exception {
        if (returnValue == null) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        ResponseEntity<?> entity = (ResponseEntity<?>) returnValue;
        response.setStatus(entity.status());
        for (Map.Entry<String, String> entry : entity.headers().entrySet()) {
            response.setHeader(entry.getKey(), entry.getValue());
        }
        Object body = entity.body();
        if (body != null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String json = objectMapper.writeValueAsString(body);
            response.getWriter().write(json);
        }
    }
}
