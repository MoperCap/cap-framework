package org.moper.cap.web.binder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public interface ParameterBinderRegistry {

    Object resolve(ParameterMetadata metadata,
                  HttpServletRequest request,
                  HttpServletResponse response,
                  Map<String, String> pathVariables) throws Exception;
}
