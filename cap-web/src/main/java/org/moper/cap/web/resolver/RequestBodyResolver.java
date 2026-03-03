package org.moper.cap.web.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.web.annotation.RequestBody;
import org.moper.cap.web.model.ParameterMetadata;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 请求体解析器。
 *
 * <p>将 HTTP 请求体（JSON）反序列化并绑定到标注了 {@link RequestBody} 的方法参数。
 */
public class RequestBodyResolver implements ParameterResolver {

    private final ObjectMapper objectMapper;

    public RequestBodyResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(ParameterMetadata metadata) {
        return metadata.parameter().isAnnotationPresent(RequestBody.class);
    }

    @Override
    public Object resolve(ParameterMetadata metadata,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          Map<String, String> pathVariables) throws Exception {
        RequestBody annotation = metadata.parameter().getAnnotation(RequestBody.class);
        String body = readBody(request);
        if (body == null || body.isBlank()) {
            if (annotation.required()) {
                throw new IllegalStateException("Required request body is missing");
            }
            return null;
        }
        return objectMapper.readValue(body, metadata.type());
    }

    private String readBody(HttpServletRequest request) throws IOException {
        try (var reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
