package org.moper.cap.web.view.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.moper.cap.web.view.ViewHandler;
import org.moper.cap.web.view.ResponseEntity;
import org.moper.cap.web.router.RouteDefinition;
import org.moper.cap.common.priority.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * 处理 ResponseEntity 返回值
 *
 * 用途：返回包含状态码、请求头和响应体的完整响应
 */
@Priority(300)
public class ResponseEntityViewHandler implements ViewHandler {

    private final ObjectMapper objectMapper;

    public ResponseEntityViewHandler() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(Class<?> returnType, RouteDefinition mapping) {
        return returnType != null && ResponseEntity.class.isAssignableFrom(returnType);
    }

    @Override
    public void handle(Object returnValue,
                      RouteDefinition mapping,
                      HttpServletRequest request,
                      HttpServletResponse response) throws Exception {

        if (returnValue == null) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        @SuppressWarnings("unchecked")
        ResponseEntity<Object> entity = (ResponseEntity<Object>) returnValue;

        response.setStatus(entity.getStatusCode());

        if (entity.getHeaders() != null && !entity.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : entity.getHeaders().entrySet()) {
                response.setHeader(header.getKey(), header.getValue());
            }
        }

        Object body = entity.getBody();
        if (body != null) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }
}
