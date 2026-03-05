package org.moper.cap.common.exception;

import lombok.extern.slf4j.Slf4j;
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
        Map<Class<?>, ExceptionHandler<?>> map = new ConcurrentHashMap<>();

        @SuppressWarnings("rawtypes")
        ServiceLoader<ExceptionHandler> loader = ServiceLoader.load(ExceptionHandler.class);
        for (ExceptionHandler<?> handler : loader) {
            int hPriority = PriorityUtils.getPriority(handler.getClass());
            Class<?> key = handler.getExceptionType();
            ExceptionHandler<?> prev = map.get(key);
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
     * 处理异常。若找到对应类型的处理器则委托给它，否则重新抛出。
     *
     * @param exception 待处理的异常
     * @throws E        若无对应处理器则将原异常重新抛出
     * @param <E>       异常类型
     */
    @SuppressWarnings("unchecked")
    public <E extends Exception> void resolve(E exception) throws E {
        if (exception == null) {
            return;
        }
        ExceptionHandler<E> handler = (ExceptionHandler<E>) handlers.get(exception.getClass());
        if (handler != null) {
            log.debug("使用处理器 [{}] 处理异常: {}", handler.getClass().getName(), exception.getMessage());
            handler.handle(exception);
        } else {
            log.warn("未找到异常处理器 [{}]，重新抛出", exception.getClass().getName());
            throw exception;
        }
    }

    /**
     * 判断是否存在对应异常类型的处理器。
     *
     * @param exceptionType 异常类型
     * @return 是否存在处理器
     */
    public boolean hasHandler(Class<? extends Exception> exceptionType) {
        return handlers.containsKey(exceptionType);
    }

    /**
     * 判断候选处理器是否优先于已注册处理器（较小的 priority 值代表更高优先级）。
     *
     * @param candidate 候选处理器
     * @param existing  已注册处理器
     * @return 若候选处理器优先级更高则返回 true
     */
    protected static boolean hasHigherPriority(ExceptionHandler<?> candidate, ExceptionHandler<?> existing) {
        return PriorityUtils.getPriority(candidate.getClass()) < PriorityUtils.getPriority(existing.getClass());
    }
}
