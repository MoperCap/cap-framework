package org.moper.cap.web.binder.impl;

import org.moper.cap.web.binder.ParameterBinder;
import org.moper.cap.web.binder.ParameterMetadata;
import org.moper.cap.common.priority.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@Priority(200)
public class ServletResponseBinder implements ParameterBinder {

    @Override
    public boolean supports(ParameterMetadata metadata) {
        return metadata.type().equals(HttpServletResponse.class);
    }

    @Override
    public Object bind(ParameterMetadata metadata,
                      HttpServletRequest request,
                      HttpServletResponse response,
                      Map<String, String> pathVariables) throws Exception {
        return response;
    }
}
