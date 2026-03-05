package org.moper.cap.web.interceptor;

import org.moper.cap.web.router.RouteMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface RequestInterceptor {

    boolean preHandle(HttpServletRequest request,
                    HttpServletResponse response,
                    RouteMapping mapping) throws Exception;

    void postHandle(HttpServletRequest request,
                   HttpServletResponse response,
                   RouteMapping mapping,
                   Object returnValue) throws Exception;

    void afterCompletion(HttpServletRequest request,
                        HttpServletResponse response,
                        RouteMapping mapping,
                        Exception exception) throws Exception;
}
