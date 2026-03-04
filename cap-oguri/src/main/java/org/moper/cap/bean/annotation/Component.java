package org.moper.cap.bean.annotation;

import java.lang.annotation.*;

/**
 * 标记一个类为由容器管理的 Bean 组件。
 *
 * <p>替代旧的 {@link Capper} 注解，遵循 Spring Framework 命名规范以降低学习成本。
 * 被该注解标记的类会被 {@code ComponentBeanRegisterBootstrapRunner} 自动发现并注册到 Bean 容器中。
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 使用默认 Bean 名称（类名首字母小写）
 * @Component
 * public class UserService { ... }
 *
 * // 显式指定 Bean 名称
 * @Component("myUserService")
 * public class UserService { ... }
 * }</pre>
 *
 * @see Configuration
 * @see Autowired
 * @see PostConstruct
 * @see PreDestroy
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

    /**
     * Bean 名称，默认为空字符串，表示使用类名首字母小写作为 Bean 名称。
     *
     * @return Bean 名称
     */
    String value() default "";
}
