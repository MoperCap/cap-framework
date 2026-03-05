package org.moper.cap.web.result.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.common.priority.Priority;
import org.moper.cap.web.annotation.response.ResponseBody;
import org.moper.cap.web.annotation.controller.RestController;
import org.moper.cap.web.result.ReturnValueHandler;
import org.moper.cap.web.model.HandlerMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * String 返回值处理器。
 *
 * <p>当控制器方法带有 {@link ResponseBody} 或 {@link RestController} 注解，
 * 且返回 {@link String} 时，直接将字符串写入响应体。
 */
@Priority(90)
public class StringReturnValueHandler implements ReturnValueHandler {

    @Override
    public boolean supports(Class<?> returnType, HandlerMapping mapping) {
        if (returnType != String.class) {
            return false;
        }
        return isResponseBody(mapping);
    }

    @Override
    public void handle(Object returnValue,
                       HandlerMapping mapping,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        if (returnValue == null) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        String contentType = mapping.produces() != null && !mapping.produces().isBlank()
                ? mapping.produces()
                : "text/plain;charset=UTF-8";
        response.setContentType(contentType);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter writer = response.getWriter()) {
            writer.write((String) returnValue);
        }
    }

    private boolean isResponseBody(HandlerMapping mapping) {
        Class<?> controllerClass = mapping.handler().getClass();
        return controllerClass.isAnnotationPresent(RestController.class)
                || controllerClass.isAnnotationPresent(ResponseBody.class)
                || mapping.handlerMethod().isAnnotationPresent(ResponseBody.class);
    }
}
