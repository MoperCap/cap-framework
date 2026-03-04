package org.moper.cap.web.resolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.common.annotation.Priority;
import org.moper.cap.common.converter.TypeResolver;
import org.moper.cap.web.annotation.PathVariable;
import org.moper.cap.web.model.ParameterMetadata;

import java.util.Map;

/**
 * 路径变量解析器。
 *
 * <p>将 URL 路径中的变量（如 {@code /users/{id}} 中的 {@code id}）
 * 绑定到标注了 {@link PathVariable} 的方法参数。
 */
@Priority(90)
public class PathVariableResolver implements ParameterResolver {

    private TypeResolver typeResolver;

    public PathVariableResolver() {
    }

    public PathVariableResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    public void setTypeResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public boolean supports(ParameterMetadata metadata) {
        return metadata.parameter().isAnnotationPresent(PathVariable.class);
    }

    @Override
    public Object resolve(ParameterMetadata metadata,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          Map<String, String> pathVariables) {
        PathVariable annotation = metadata.parameter().getAnnotation(PathVariable.class);
        String name = resolveName(annotation, metadata);
        String value = pathVariables.get(name);
        if (value == null) {
            if (annotation.required()) {
                throw new IllegalStateException("Required path variable '" + name + "' not present");
            }
            return null;
        }
        return typeResolver.resolve(value, metadata.type());
    }

    private String resolveName(PathVariable annotation, ParameterMetadata metadata) {
        String name = annotation.value().isBlank() ? annotation.name() : annotation.value();
        return name.isBlank() ? metadata.name() : name;
    }
}
