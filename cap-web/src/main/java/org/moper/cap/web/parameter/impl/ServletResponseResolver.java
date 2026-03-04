package org.moper.cap.web.parameter.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.common.annotation.Priority;
import org.moper.cap.web.parameter.ParameterMetadata;
import org.moper.cap.web.parameter.ParameterResolver;

import java.util.Map;

/**
 * {@link HttpServletResponse} 参数解析器。
 *
 * <p>当方法参数类型为 {@link HttpServletResponse} 时，直接注入响应对象。
 */
@Priority(95)
public class ServletResponseResolver implements ParameterResolver {

    @Override
    public boolean supports(ParameterMetadata metadata) {
        return HttpServletResponse.class.isAssignableFrom(metadata.type());
    }

    @Override
    public Object resolve(ParameterMetadata metadata,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          Map<String, String> pathVariables) {
        return response;
    }
}
