package org.moper.cap.web.view;

import org.moper.cap.web.router.RouteDefinition;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ViewHandler {

    boolean supports(Class<?> returnType, RouteDefinition mapping);

    void handle(Object returnValue,
               RouteDefinition mapping,
               HttpServletRequest request,
               HttpServletResponse response) throws Exception;
}
