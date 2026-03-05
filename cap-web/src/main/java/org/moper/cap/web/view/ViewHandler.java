package org.moper.cap.web.view;

import org.moper.cap.web.router.RouteMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ViewHandler {

    boolean supports(Class<?> returnType, RouteMapping mapping);

    void handle(Object returnValue,
               RouteMapping mapping,
               HttpServletRequest request,
               HttpServletResponse response) throws Exception;
}
