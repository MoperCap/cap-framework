package org.moper.cap.web.router;

import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.web.annotation.controller.Controller;
import org.moper.cap.web.annotation.controller.RestController;
import org.moper.cap.web.annotation.mapping.*;
import org.moper.cap.web.annotation.request.PathVariable;
import org.moper.cap.web.binder.ParameterMetadata;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Slf4j
public class RouteRegistry {

    private final List<RouteDefinition> routes = new ArrayList<>();
    private final Map<String, RouteDefinition> pathIndex = new HashMap<>();

    /**
     * 从 BeanContainer 扫描注解并注册路由
     */
    public void registerRoutesFromBeans(BeanContainer beanContainer) {
        String[] beanNames = beanContainer.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            BeanDefinition definition = beanContainer.getBeanDefinition(beanName);
            Class<?> beanClass = definition.type();

            if (!isControllerClass(beanClass)) {
                continue;
            }

            Object beanInstance = beanContainer.getBean(beanName);
            String classPath = extractClassPath(beanClass);

            for (Method method : beanClass.getDeclaredMethods()) {
                RouteDefinition route = createRouteFromMethod(beanInstance, method, classPath);

                if (route != null) {
                    registerRoute(route);
                }
            }
        }

        log.info("RouteRegistry 共注册 {} 个路由", routes.size());
    }

    /**
     * 创建路由定义
     */
    private RouteDefinition createRouteFromMethod(Object controller, Method method, String classPath) {
        HttpMethod httpMethod = null;
        String methodPath = null;

        if (method.isAnnotationPresent(GetRouter.class)) {
            httpMethod = HttpMethod.GET;
            methodPath = method.getAnnotation(GetRouter.class).value();
        } else if (method.isAnnotationPresent(PostRouter.class)) {
            httpMethod = HttpMethod.POST;
            methodPath = method.getAnnotation(PostRouter.class).value();
        } else if (method.isAnnotationPresent(PutRouter.class)) {
            httpMethod = HttpMethod.PUT;
            methodPath = method.getAnnotation(PutRouter.class).value();
        } else if (method.isAnnotationPresent(DeleteRouter.class)) {
            httpMethod = HttpMethod.DELETE;
            methodPath = method.getAnnotation(DeleteRouter.class).value();
        } else if (method.isAnnotationPresent(PatchRouter.class)) {
            httpMethod = HttpMethod.PATCH;
            methodPath = method.getAnnotation(PatchRouter.class).value();
        } else if (method.isAnnotationPresent(HeadRouter.class)) {
            httpMethod = HttpMethod.HEAD;
            methodPath = method.getAnnotation(HeadRouter.class).value();
        } else if (method.isAnnotationPresent(OptionsRouter.class)) {
            httpMethod = HttpMethod.OPTIONS;
            methodPath = method.getAnnotation(OptionsRouter.class).value();
        } else if (method.isAnnotationPresent(Router.class)) {
            Router router = method.getAnnotation(Router.class);
            httpMethod = router.method();
            String path = router.path();
            methodPath = path.isBlank() ? router.value() : path;
        }

        if (httpMethod == null) {
            return null;
        }

        String fullPath = classPath + (methodPath != null ? methodPath : "");
        List<ParameterMetadata> parameters = extractParameters(method);
        List<String> pathVariableNames = extractPathVariableNames(method);

        return new RouteDefinition(
                fullPath,
                httpMethod,
                controller,
                method,
                parameters,
                pathVariableNames
        );
    }

    /**
     * 检查类是否为控制器
     */
    private boolean isControllerClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class) ||
               clazz.isAnnotationPresent(RestController.class);
    }

    /**
     * 提取类上的路由路径
     */
    private String extractClassPath(Class<?> clazz) {
        Router classRouter = clazz.getAnnotation(Router.class);
        if (classRouter != null) {
            String path = classRouter.path();
            return path.isBlank() ? classRouter.value() : path;
        }
        return "";
    }

    /**
     * 提取方法的参数元数据
     */
    private List<ParameterMetadata> extractParameters(Method method) {
        List<ParameterMetadata> metadata = new ArrayList<>();
        Parameter[] parameters = method.getParameters();

        for (Parameter param : parameters) {
            metadata.add(new ParameterMetadata(
                    param,
                    param.getName(),
                    param.getType()
            ));
        }

        return metadata;
    }

    /**
     * 提取方法中的路径变量名
     */
    private List<String> extractPathVariableNames(Method method) {
        List<String> names = new ArrayList<>();
        Parameter[] parameters = method.getParameters();

        for (Parameter param : parameters) {
            if (param.isAnnotationPresent(PathVariable.class)) {
                PathVariable annotation = param.getAnnotation(PathVariable.class);
                String varName = annotation.value().isBlank() ? param.getName() : annotation.value();
                names.add(varName);
            }
        }

        return names;
    }

    /**
     * 注册单个路由
     */
    public void registerRoute(RouteDefinition route) {
        routes.add(route);

        String indexKey = route.httpMethod() + ":" + route.path();
        pathIndex.put(indexKey, route);

        log.debug("注册路由: {} {} -> {}#{}",
                 route.httpMethod(),
                 route.path(),
                 route.controller().getClass().getSimpleName(),
                 route.controllerMethod().getName());
    }

    /**
     * 查找路由
     */
    public Optional<RouteDefinition> findRoute(String requestPath, HttpMethod method) {
        String indexKey = method + ":" + requestPath;
        if (pathIndex.containsKey(indexKey)) {
            return Optional.of(pathIndex.get(indexKey));
        }

        for (RouteDefinition route : routes) {
            if (route.matches(requestPath, method)) {
                return Optional.of(route);
            }
        }

        return Optional.empty();
    }

    /**
     * 获取所有路由
     */
    public List<RouteDefinition> getAllRoutes() {
        return Collections.unmodifiableList(routes);
    }
}
