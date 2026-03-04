package org.moper.cap.bean.annotation;

import java.lang.annotation.*;

/**
 * 标记一个字段或构造函数参数，由容器自动注入依赖 Bean。
 *
 * <p>替代旧的 {@link Inject} 注解，遵循 Spring Framework 命名规范以降低学习成本。
 * 可应用于：
 * <ul>
 *   <li>字段（{@link ElementType#FIELD}）：容器在 Bean 初始化后自动注入对应的依赖</li>
 *   <li>构造函数参数（{@link ElementType#PARAMETER}）：标记构造函数以指定注入入口，
 *       参数依赖由容器在实例化时解析</li>
 * </ul>
 *
 * <p><b>构造函数注入规则：</b>若一个类存在多个构造函数，必须有且仅有一个构造函数
 * 标注 {@code @Autowired}，否则容器将抛出异常。
 * 若只有一个构造函数，则无需标注 {@code @Autowired}。
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Component
 * public class OrderService {
 *
 *     // 字段注入
 *     @Autowired
 *     private UserService userService;
 *
 *     // 构造函数注入（多构造函数时必须指定）
 *     @Autowired
 *     public OrderService(UserService userService, ProductService productService) {
 *         ...
 *     }
 * }
 * }</pre>
 *
 * @see Qualifier
 * @see Component
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {

    /**
     * 是否为必须注入。
     *
     * <p>默认为 {@code true}，表示若找不到匹配的 Bean 则抛出异常。
     * 设置为 {@code false} 时，找不到匹配的 Bean 则跳过注入（字段保持 null）。
     *
     * @return {@code true} 表示必须注入，{@code false} 表示可选注入
     */
    boolean required() default true;
}
