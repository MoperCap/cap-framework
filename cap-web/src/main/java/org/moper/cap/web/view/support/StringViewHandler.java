package org.moper.cap.web.view.support;

import org.moper.cap.web.view.ViewHandler;
import org.moper.cap.web.router.RouteDefinition;
import org.moper.cap.common.priority.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理 String 返回值
 *
 * 用途：返回纯文本或 HTML 内容
 */
@Priority(200)
public class StringViewHandler implements ViewHandler {

    @Override
    public boolean supports(Class<?> returnType, RouteDefinition mapping) {
        return returnType == String.class;
    }

    @Override
    public void handle(Object returnValue,
                      RouteDefinition mapping,
                      HttpServletRequest request,
                      HttpServletResponse response) throws Exception {

        if (returnValue == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        String content = (String) returnValue;

        if (isHtmlContent(content)) {
            response.setContentType("text/html;charset=UTF-8");
        } else {
            response.setContentType("text/plain;charset=UTF-8");
        }

        response.getWriter().write(content);
    }

    private boolean isHtmlContent(String content) {
        return content != null && content.trim().toLowerCase().startsWith("<");
    }
}
