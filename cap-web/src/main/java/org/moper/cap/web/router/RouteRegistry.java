package org.moper.cap.web.router;

import org.moper.cap.web.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class RouteRegistry {

    private final List<RouteDefinition> routes = new ArrayList<>();
    private final Map<String, RouteDefinition> pathIndex = new HashMap<>();

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
