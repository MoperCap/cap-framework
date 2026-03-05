package org.moper.cap.web.view.support;

import org.moper.cap.web.view.ViewHandler;
import org.moper.cap.web.router.RouteDefinition;
import org.moper.cap.common.priority.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理 void/Void 返回值
 *
 * 用途：处理无返回值的方法，返回 204 No Content
 */
@Priority(200)
public class VoidViewHandler implements ViewHandler {

    @Override
    public boolean supports(Class<?> returnType, RouteDefinition mapping) {
        return returnType == void.class || returnType == Void.class;
    }

    @Override
    public void handle(Object returnValue,
                      RouteDefinition mapping,
                      HttpServletRequest request,
                      HttpServletResponse response) throws Exception {

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
