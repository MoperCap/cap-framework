package org.moper.cap.web.binder.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.moper.cap.web.binder.ParameterBinder;
import org.moper.cap.web.binder.ParameterMetadata;
import org.moper.cap.web.annotation.request.RequestBody;
import org.moper.cap.common.priority.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@Priority(100)
public class RequestBodyBinder implements ParameterBinder {

    private final ObjectMapper objectMapper;

    public RequestBodyBinder() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean supports(ParameterMetadata metadata) {
        return metadata.parameter().isAnnotationPresent(RequestBody.class);
    }

    @Override
    public Object bind(ParameterMetadata metadata,
                      HttpServletRequest request,
                      HttpServletResponse response,
                      Map<String, String> pathVariables) throws Exception {

        RequestBody annotation = metadata.parameter().getAnnotation(RequestBody.class);

        byte[] bytes = request.getInputStream().readAllBytes();

        if (bytes.length == 0) {
            if (annotation.required()) {
                throw new IllegalArgumentException("Request body is required but empty");
            }
            return null;
        }

        return objectMapper.readValue(bytes, metadata.type());
    }
}
