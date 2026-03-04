package org.moper.cap.web.parameter.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.common.annotation.Priority;
import org.moper.cap.web.parameter.ParameterMetadata;
import org.moper.cap.web.parameter.ParameterResolver;

import java.util.Map;

/**
 * {@link HttpServletRequest} 参数解析器。
 *
 * <p>当方法参数类型为 {@link HttpServletRequest} 时，直接注入请求对象。
 */
@Priority(100)
public class ServletRequestResolver implements ParameterResolver {

    @Override
    public boolean supports(ParameterMetadata metadata) {
        return HttpServletRequest.class.isAssignableFrom(metadata.type());
    }

    @Override
    public Object resolve(ParameterMetadata metadata,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          Map<String, String> pathVariables) {
        return request;
    }
}
