package org.moper.cap.web.resolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.common.annotation.Priority;
import org.moper.cap.common.converter.TypeResolver;
import org.moper.cap.web.annotation.CookieValue;
import org.moper.cap.web.model.ParameterMetadata;

import java.util.Map;

/**
 * Cookie 值解析器。
 *
 * <p>将 HTTP Cookie 的值绑定到标注了 {@link CookieValue} 的方法参数。
 */
@Priority(50)
public class CookieValueResolver implements ParameterResolver {

    private TypeResolver typeResolver;

    public CookieValueResolver() {
    }

    public CookieValueResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    public void setTypeResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public boolean supports(ParameterMetadata metadata) {
        return metadata.parameter().isAnnotationPresent(CookieValue.class);
    }

    @Override
    public Object resolve(ParameterMetadata metadata,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          Map<String, String> pathVariables) {
        CookieValue annotation = metadata.parameter().getAnnotation(CookieValue.class);
        String name = resolveName(annotation, metadata);
        String value = findCookieValue(request, name);
        if (value == null) {
            if (!annotation.defaultValue().isEmpty()) {
                value = annotation.defaultValue();
            } else if (annotation.required()) {
                throw new IllegalStateException("Required cookie '" + name + "' not present");
            } else {
                return null;
            }
        }
        return typeResolver.resolve(value, metadata.type());
    }

    private String findCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String resolveName(CookieValue annotation, ParameterMetadata metadata) {
        String name = annotation.value().isBlank() ? annotation.name() : annotation.value();
        return name.isBlank() ? metadata.name() : name;
    }
}
