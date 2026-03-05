package org.moper.cap.web.router.util;

import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.annotation.route.*;
import org.moper.cap.web.annotation.controller.Controller;
import org.moper.cap.web.annotation.controller.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class RouterAnnotationResolver {

    /**
     * 解析任意注解，返回 RouterAnnotation 或 null
     */
    public static RouterAnnotation resolve(Annotation annotation) {
        if (annotation instanceof GetRouter) {
            return resolve((GetRouter) annotation);
        }
        if (annotation instanceof PostRouter) {
            return resolve((PostRouter) annotation);
        }
        if (annotation instanceof PutRouter) {
            return resolve((PutRouter) annotation);
        }
        if (annotation instanceof DeleteRouter) {
            return resolve((DeleteRouter) annotation);
        }
        if (annotation instanceof PatchRouter) {
            return resolve((PatchRouter) annotation);
        }
        if (annotation instanceof HeadRouter) {
            return resolve((HeadRouter) annotation);
        }
        if (annotation instanceof OptionsRouter) {
            return resolve((OptionsRouter) annotation);
        }
        if (annotation instanceof ConnectRouter) {
            return resolve((ConnectRouter) annotation);
        }
        if (annotation instanceof TraceRouter) {
            return resolve((TraceRouter) annotation);
        }
        if (annotation instanceof Router) {
            return resolve((Router) annotation);
        }
        return null;
    }

    public static RouterAnnotation resolve(Router router) {
        String path = router.path();
        return new RouterAnnotation(router.method(), path.isBlank() ? router.value() : path);
    }

    public static RouterAnnotation resolve(GetRouter router) {
        return new RouterAnnotation(HttpMethod.GET, router.value());
    }

    public static RouterAnnotation resolve(PostRouter router) {
        return new RouterAnnotation(HttpMethod.POST, router.value());
    }

    public static RouterAnnotation resolve(PutRouter router) {
        return new RouterAnnotation(HttpMethod.PUT, router.value());
    }

    public static RouterAnnotation resolve(DeleteRouter router) {
        return new RouterAnnotation(HttpMethod.DELETE, router.value());
    }

    public static RouterAnnotation resolve(PatchRouter router) {
        return new RouterAnnotation(HttpMethod.PATCH, router.value());
    }

    public static RouterAnnotation resolve(HeadRouter router) {
        return new RouterAnnotation(HttpMethod.HEAD, router.value());
    }

    public static RouterAnnotation resolve(OptionsRouter router) {
        return new RouterAnnotation(HttpMethod.OPTIONS, router.value());
    }

    public static RouterAnnotation resolve(ConnectRouter router) {
        return new RouterAnnotation(HttpMethod.CONNECT, router.value());
    }

    public static RouterAnnotation resolve(TraceRouter router) {
        return new RouterAnnotation(HttpMethod.TRACE, router.value());
    }

    /**
     * 从方法上解析路由注解
     */
    public static RouterAnnotation resolve(Method method) {
        if (method == null) {
            return null;
        }

        for (Annotation annotation : method.getAnnotations()) {
            RouterAnnotation result = resolve(annotation);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * 从类上解析路由路径
     */
    public static String resolve(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }

        Router router = clazz.getAnnotation(Router.class);
        if (router != null) {
            String path = router.path();
            return path.isBlank() ? router.value() : path;
        }

        return "";
    }

    /**
     * 检查类是否为控制器
     */
    public static boolean isController(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return clazz.isAnnotationPresent(Controller.class) ||
               clazz.isAnnotationPresent(RestController.class);
    }

    /**
     * 路由注解的解析结果
     */
    public record RouterAnnotation(HttpMethod httpMethod, String path) {
    }
}
