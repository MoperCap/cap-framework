package org.moper.cap.web.util;

import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.annotation.route.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class RouterAnnotationResolver {

    /**
     * 解析任意注解，返回 RouterAnnotation 或 null
     */
    public static RouterAnnotation resolve(Annotation annotation) {
        return switch (annotation){
            case GetRouter getRouter -> resolve(getRouter);
            case PostRouter postRouter -> resolve(postRouter);
            case PutRouter putRouter -> resolve(putRouter);
            case DeleteRouter deleteRouter -> resolve(deleteRouter);
            case PatchRouter patchRouter -> resolve(patchRouter);
            case HeadRouter headRouter -> resolve(headRouter);
            case OptionsRouter optionsRouter -> resolve(optionsRouter);
            case TraceRouter traceRouter -> resolve(traceRouter);
            case ConnectRouter connectRouter -> resolve(connectRouter);
            case Router router -> resolve(router);
            default -> null;
        };
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
     * 路由注解的解析结果
     */
    public record RouterAnnotation(HttpMethod httpMethod, String path) {

        public RouterAnnotation{
            if(httpMethod == null){
                throw new NullPointerException("httpMethod is null");
            }

            if(path == null || path.isBlank()){
                throw new NullPointerException("path is null or blank");
            }
        }

    }
}
