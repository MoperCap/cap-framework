package org.moper.cap.web.view.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.moper.cap.web.view.ViewHandler;
import org.moper.cap.web.view.ResponseEntity;
import org.moper.cap.web.router.RouteDefinition;
import org.moper.cap.common.priority.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理 JSON 响应（默认处理器）
 *
 * 用途：将任意对象序列化为 JSON 返回，作为 fallback 处理器
 * 优先级最低（Integer.MAX_VALUE）
 */
@Priority(Integer.MAX_VALUE)
public class JsonViewHandler implements ViewHandler {

    private final ObjectMapper objectMapper;

    public JsonViewHandler() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(Class<?> returnType, RouteDefinition mapping) {
        if (returnType == null) return false;
        if (returnType == String.class) return false;
        if (returnType == void.class || returnType == Void.class) return false;
        if (returnType == byte[].class) return false;
        if (ResponseEntity.class.isAssignableFrom(returnType)) return false;

        return true;
    }

    @Override
    public void handle(Object returnValue,
                      RouteDefinition mapping,
                      HttpServletRequest request,
                      HttpServletResponse response) throws Exception {

        response.setContentType("application/json;charset=UTF-8");

        if (returnValue == null) {
            response.getWriter().write("null");
        } else {
            response.getWriter().write(objectMapper.writeValueAsString(returnValue));
        }
    }
}
