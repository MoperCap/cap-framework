package org.moper.cap.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.handler.IllegalArgumentExceptionHandler;
import org.moper.cap.common.priority.PriorityUtils;

import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异常处理器注册表 </br>
 * 通过ServiceLoader自动发现所有{@link ExceptionHandler}实现，
 * 使用{@link org.moper.cap.common.priority.Priority}注解按优先级选取处理器。
 */
@Slf4j
public class ExceptionResolverRegistry {

    private final Map<Class<?>, ExceptionHandler<?>> handlers;

    public ExceptionResolverRegistry() {
        Map<Class<? extends Throwable>, ExceptionHandler<?>> map = new ConcurrentHashMap<>();

        @SuppressWarnings("rawtypes")
        ServiceLoader<ExceptionHandler> loader = ServiceLoader.load(ExceptionHandler.class);
        for (ExceptionHandler<?> handler : loader) {
            int hPriority = PriorityUtils.getPriority(handler.getClass());
            Class<? extends Throwable> key = handler.getExceptionType();
            ExceptionHandler<? extends Throwable> prev = map.get(key);
            if (prev == null) {
                log.debug("注册异常处理器 [{}]: {} (priority={})", key.getName(), handler.getClass().getName(), hPriority);
                map.put(key, handler);
            } else {
                int prevPriority = PriorityUtils.getPriority(prev.getClass());
                if (hPriority < prevPriority) {
                    log.debug("覆盖异常处理器 [{}]: {} <== {} ({} > {})",
                            key.getName(), prev.getClass().getName(), handler.getClass().getName(), prevPriority, hPriority);
                    map.put(key, handler);
                } else {
                    log.debug("忽略较低优先级的异常处理器 [{}]: {} (priority={}), 被 {} (priority={}) 覆盖",
                            key.getName(), handler.getClass().getName(), hPriority, prev.getClass().getName(), prevPriority);
                }
            }
        }
        this.handlers = Collections.unmodifiableMap(map);
        log.info("ExceptionResolverRegistry 共注册 {} 种异常处理器", handlers.size());
    }



    /**
     * 处理异常。若找到对应类型（或父类型）的处理器则委托给它，否则重新抛出。
     *
     * @param exception 待处理的异常
     * @throws E        若无对应处理器则将原异常重新抛出
     * @param <E>       异常类型
     */
    @SuppressWarnings("unchecked")
    public <E extends Throwable> void resolve(E exception) throws E {
        if (exception == null) {
            return;
        }
        ExceptionHandler<E> handler = (ExceptionHandler<E>) findHandlerByClass(exception.getClass());
        if (handler != null) {
            log.info("异常已处理 [{}] by {}", exception.getClass().getSimpleName(), handler.getClass().getSimpleName());
            handler.handle(exception);
        } else {
            log.warn("未找到异常处理器 [{}]，重新抛出", exception.getClass().getName());
            throw exception;
        }
    }

    /**
     * 判断是否存在对应异常类型（或其父类型）的处理器。
     *
     * @param exceptionType 异常类型
     * @return 是否存在处理器
     */
    public boolean hasHandler(Class<? extends Throwable> exceptionType) {
        return findHandlerByClass(exceptionType) != null;
    }

    /**
     * 获取对应异常类型的处理器（递归查找父类）。
     *
     * @param exceptionType 异常类型
     * @return 找到的处理器，或 null
     */
    public ExceptionHandler<?> getHandler(Class<? extends Throwable> exceptionType) {
        return findHandlerByClass(exceptionType);
    }

    /**
     * 递归查找异常类型对应的处理器，沿继承链向上查找直到 Throwable。
     *
     * @param clazz 异常类型
     * @return 找到的处理器，或 null
     */
    private ExceptionHandler<?> findHandlerByClass(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }

        if (clazz == Throwable.class) {
            return null;
        }

        log.debug("递归查找处理器 [{}]", clazz.getSimpleName());
        ExceptionHandler<?> handler = handlers.get(clazz);
        if (handler != null) {
            log.debug("找到处理器 [{}]", handler.getClass().getSimpleName());
            return handler;
        }
        log.debug("未找到精确处理器，查找父类 [{}]", clazz.getSuperclass() != null ? clazz.getSuperclass().getSimpleName() : "null");
        return findHandlerByClass(clazz.getSuperclass());
    }
}
