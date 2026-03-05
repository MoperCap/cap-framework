package org.moper.cap.web.invoker;

import org.moper.cap.web.router.RouteDefinition;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public interface MethodInvoker {

    Object invoke(RouteDefinition mapping,
                 HttpServletRequest request,
                 HttpServletResponse response,
                 Map<String, String> pathVariables) throws Exception;
}
