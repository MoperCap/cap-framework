package org.moper.cap.web.registry;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.web.annotation.*;
import org.moper.cap.web.annotation.controller.Controller;
import org.moper.cap.web.annotation.controller.RestController;
import org.moper.cap.web.annotation.mapping.*;
import org.moper.cap.web.annotation.request.RequestHeader;
import org.moper.cap.web.annotation.request.RequestParam;
import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.model.HandlerMapping;
import org.moper.cap.web.parameter.ParameterMetadata;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 路由映射注册表。
 *
 * <p>扫描所有 {@link Controller} 和 {@link RestController} Bean，
 * 收集带有请求映射注解的方法，并构建 {@link HandlerMapping} 列表。
 */
@Slf4j
public class HandlerMappingRegistry {

    private final List<HandlerMapping> mappings = new ArrayList<>();

    /**
     * 扫描容器中的所有控制器 Bean，注册路由映射。
     *
     * @param container Bean 容器
     */
    public void register(BeanContainer container) {
        Map<String, Object> controllers = new LinkedHashMap<>();
        controllers.putAll(container.getBeansWithAnnotation(Controller.class));
        controllers.putAll(container.getBeansWithAnnotation(RestController.class));

        for (Map.Entry<String, Object> entry : controllers.entrySet()) {
            Object handler = entry.getValue();
            Class<?> clazz = handler.getClass();
            String classPath = extractClassPath(clazz);
            for (Method method : clazz.getDeclaredMethods()) {
                List<HandlerMapping> methodMappings = buildMappings(classPath, handler, method);
                mappings.addAll(methodMappings);
            }
        }
        log.info("HandlerMappingRegistry: registered {} route mappings", mappings.size());
    }

    /**
     * 根据请求路径和 HTTP 方法查找匹配的 HandlerMapping。
     *
     * @param path       请求路径
     * @param httpMethod HTTP 方法
     * @return 匹配的 HandlerMapping，若未找到则返回 empty
     */
    public Optional<HandlerMapping> findMapping(String path, HttpMethod httpMethod) {
        return mappings.stream()
                .filter(m -> m.matches(path, httpMethod))
                .findFirst();
    }

    /**
     * 获取所有已注册的路由映射（只读视图）。
     *
     * @return 不可变的 HandlerMapping 列表
     */
    public List<HandlerMapping> getAllMappings() {
        return Collections.unmodifiableList(mappings);
    }

    // ──────────────── 私有方法 ────────────────

    private String extractClassPath(Class<?> clazz) {
        RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
        if (mapping != null) {
            String[] paths = mapping.value().length > 0 ? mapping.value() : mapping.path();
            if (paths.length > 0 && !paths[0].isBlank()) {
                return normalizePath(paths[0]);
            }
        }
        return "";
    }

    private List<HandlerMapping> buildMappings(String classPath, Object handler, Method method) {
        List<HandlerMapping> result = new ArrayList<>();

        // @GetMapping
        GetMapping get = method.getAnnotation(GetMapping.class);
        if (get != null) {
            String path = resolvePath(get.value(), get.path());
            result.add(buildMapping(classPath, path, HttpMethod.GET,
                    firstOrNull(get.produces()), firstOrNull(get.consumes()), handler, method));
        }

        // @PostMapping
        PostMapping post = method.getAnnotation(PostMapping.class);
        if (post != null) {
            String path = resolvePath(post.value(), post.path());
            result.add(buildMapping(classPath, path, HttpMethod.POST,
                    firstOrNull(post.produces()), firstOrNull(post.consumes()), handler, method));
        }

        // @PutMapping
        PutMapping put = method.getAnnotation(PutMapping.class);
        if (put != null) {
            String path = resolvePath(put.value(), put.path());
            result.add(buildMapping(classPath, path, HttpMethod.PUT,
                    firstOrNull(put.produces()), firstOrNull(put.consumes()), handler, method));
        }

        // @DeleteMapping
        DeleteMapping delete = method.getAnnotation(DeleteMapping.class);
        if (delete != null) {
            String path = resolvePath(delete.value(), delete.path());
            result.add(buildMapping(classPath, path, HttpMethod.DELETE,
                    firstOrNull(delete.produces()), firstOrNull(delete.consumes()), handler, method));
        }

        // @PatchMapping
        PatchMapping patch = method.getAnnotation(PatchMapping.class);
        if (patch != null) {
            String path = resolvePath(patch.value(), patch.path());
            result.add(buildMapping(classPath, path, HttpMethod.PATCH,
                    firstOrNull(patch.produces()), firstOrNull(patch.consumes()), handler, method));
        }

        // @HeadMapping
        HeadMapping head = method.getAnnotation(HeadMapping.class);
        if (head != null) {
            String path = resolvePath(head.value(), head.path());
            result.add(buildMapping(classPath, path, HttpMethod.HEAD,
                    firstOrNull(head.produces()), firstOrNull(head.consumes()), handler, method));
        }

        // @OptionsMapping
        OptionsMapping options = method.getAnnotation(OptionsMapping.class);
        if (options != null) {
            String path = resolvePath(options.value(), options.path());
            result.add(buildMapping(classPath, path, HttpMethod.OPTIONS,
                    firstOrNull(options.produces()), firstOrNull(options.consumes()), handler, method));
        }

        // @TraceMapping
        TraceMapping trace = method.getAnnotation(TraceMapping.class);
        if (trace != null) {
            String path = resolvePath(trace.value(), trace.path());
            result.add(buildMapping(classPath, path, HttpMethod.TRACE,
                    firstOrNull(trace.produces()), firstOrNull(trace.consumes()), handler, method));
        }

        // @ConnectMapping
        ConnectMapping connect = method.getAnnotation(ConnectMapping.class);
        if (connect != null) {
            String path = resolvePath(connect.value(), connect.path());
            result.add(buildMapping(classPath, path, HttpMethod.CONNECT,
                    firstOrNull(connect.produces()), firstOrNull(connect.consumes()), handler, method));
        }

        // @RequestMapping (generic)
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            String path = resolvePath(requestMapping.value(), requestMapping.path());
            HttpMethod[] methods = requestMapping.method();
            if (methods.length == 0) {
                // no method restriction: register for all HTTP methods
                for (HttpMethod m : HttpMethod.values()) {
                    result.add(buildMapping(classPath, path, m,
                            firstOrNull(requestMapping.produces()), firstOrNull(requestMapping.consumes()),
                            handler, method));
                }
            } else {
                for (HttpMethod m : methods) {
                    result.add(buildMapping(classPath, path, m,
                            firstOrNull(requestMapping.produces()), firstOrNull(requestMapping.consumes()),
                            handler, method));
                }
            }
        }

        return result;
    }

    private HandlerMapping buildMapping(String classPath, String methodPath,
                                        HttpMethod httpMethod, String produces, String consumes,
                                        Object handler, Method method) {
        String fullPath = normalizePath(classPath + normalizePath(methodPath));
        List<ParameterMetadata> parameters = buildParameterMetadata(method);
        List<String> pathVariableNames = extractPathVariableNames(fullPath);
        method.setAccessible(true);
        return new HandlerMapping(fullPath, httpMethod, produces, consumes,
                handler, method, parameters, pathVariableNames);
    }

    private List<ParameterMetadata> buildParameterMetadata(Method method) {
        Parameter[] params = method.getParameters();
        List<ParameterMetadata> result = new ArrayList<>(params.length);
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            String name = resolveParameterName(param, i);
            result.add(new ParameterMetadata(i, name, param.getType(), param));
        }
        return result;
    }

    private String resolveParameterName(Parameter param, int index) {
        // Try annotations first
        PathVariable pv = param.getAnnotation(PathVariable.class);
        if (pv != null) {
            String name = pv.value().isBlank() ? pv.name() : pv.value();
            if (!name.isBlank()) return name;
        }
        RequestParam rp = param.getAnnotation(RequestParam.class);
        if (rp != null) {
            String name = rp.value().isBlank() ? rp.name() : rp.value();
            if (!name.isBlank()) return name;
        }
        RequestHeader rh = param.getAnnotation(RequestHeader.class);
        if (rh != null) {
            String name = rh.value().isBlank() ? rh.name() : rh.value();
            if (!name.isBlank()) return name;
        }
        CookieValue cv = param.getAnnotation(CookieValue.class);
        if (cv != null) {
            String name = cv.value().isBlank() ? cv.name() : cv.value();
            if (!name.isBlank()) return name;
        }
        // Fall back to reflection name (arg0, arg1, ... unless compiled with -parameters)
        return param.isNamePresent() ? param.getName() : "arg" + index;
    }

    private List<String> extractPathVariableNames(String path) {
        List<String> names = new ArrayList<>();
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("\\{([^/]+?)}").matcher(path);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private String resolvePath(String[] value, String[] path) {
        if (value.length > 0 && !value[0].isBlank()) return value[0];
        if (path.length > 0 && !path[0].isBlank()) return path[0];
        return "";
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) return "/";
        return path.startsWith("/") ? path : "/" + path;
    }

    private String firstOrNull(String[] arr) {
        return (arr != null && arr.length > 0 && !arr[0].isBlank()) ? arr[0] : null;
    }
}
