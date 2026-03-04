package org.moper.cap.bean.annotation;

import java.lang.annotation.*;

/**
 * 标记一个方法在 Bean 初始化完成后调用。
 *
 * <p>替代旧的 {@code @Capper(initMethod="...")} 方式，被标记的方法会在 Bean 实例创建
 * 并完成所有依赖注入后，由容器自动调用。
 *
 * <p><b>方法签名要求：</b>
 * <ul>
 *   <li>访问修饰符：必须为 {@code public}</li>
 *   <li>返回类型：必须为 {@code void}</li>
 *   <li>参数列表：必须为空（无参）</li>
 * </ul>
 *
 * <p>每个类只能有一个 {@code @PostConstruct} 方法，若存在多个则抛出异常。
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Component
 * public class CacheService {
 *
 *     @Autowired
 *     private DataSource dataSource;
 *
 *     @PostConstruct
 *     public void init() {
 *         // 依赖注入完成后执行初始化逻辑
 *         log.info("CacheService initialized");
 *     }
 * }
 * }</pre>
 *
 * @see PreDestroy
 * @see Component
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstruct {
}
