package org.moper.cap.web.interceptor;

import org.moper.cap.web.router.RouteDefinition;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface RequestInterceptor {

    boolean preHandle(HttpServletRequest request,
                    HttpServletResponse response,
                    RouteDefinition mapping) throws Exception;

    void postHandle(HttpServletRequest request,
                   HttpServletResponse response,
                   RouteDefinition mapping,
                   Object returnValue) throws Exception;

    void afterCompletion(HttpServletRequest request,
                        HttpServletResponse response,
                        RouteDefinition mapping,
                        Exception exception) throws Exception;
}
