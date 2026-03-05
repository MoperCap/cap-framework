package org.moper.cap.web.router;

import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.bean.container.BeanContainer;
import java.util.Optional;

public interface RouteRegistry {

    void register(BeanContainer container);

    Optional<RouteMapping> findRoute(String requestPath, HttpMethod method);
}
