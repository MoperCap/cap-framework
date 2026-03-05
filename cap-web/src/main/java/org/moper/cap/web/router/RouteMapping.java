package org.moper.cap.web.router;

import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.binder.ParameterMetadata;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public interface RouteMapping {

    String getPath();

    HttpMethod getHttpMethod();

    Object getController();

    Method getControllerMethod();

    List<ParameterMetadata> getParameters();

    List<String> getPathVariableNames();

    boolean matches(String requestPath, HttpMethod method);

    Map<String, String> extractPathVariables(String requestPath);
}
