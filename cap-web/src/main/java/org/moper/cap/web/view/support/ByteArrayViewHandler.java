package org.moper.cap.web.view.support;

import org.moper.cap.web.view.ViewHandler;
import org.moper.cap.web.router.RouteDefinition;
import org.moper.cap.common.priority.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理 byte[] 返回值
 *
 * 用途：返回文件内容、图片、PDF 等二进制数据
 */
@Priority(250)
public class ByteArrayViewHandler implements ViewHandler {

    @Override
    public boolean supports(Class<?> returnType, RouteDefinition mapping) {
        return returnType == byte[].class;
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

        byte[] bytes = (byte[]) returnValue;

        response.setContentType("application/octet-stream");
        response.setContentLength(bytes.length);

        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}
