package org.moper.cap.web.view;

import org.moper.cap.web.router.RouteDefinition;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ViewHandlerRegistry {

    void handle(Object returnValue,
               Class<?> returnType,
               RouteDefinition mapping,
               HttpServletRequest request,
               HttpServletResponse response) throws Exception;
}
