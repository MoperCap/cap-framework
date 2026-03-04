package org.moper.cap.bean.annotation;

import java.lang.annotation.*;

/**
 * 标记一个 Bean 为同类型候选者中的首选 Bean。
 *
 * <p>当容器中存在多个相同类型的 Bean 时，按类型注入会产生歧义。
 * 标记了 {@code @Primary} 的 Bean 将作为首选项被优先注入，无需通过 {@link Qualifier} 显式指定名称。
 *
 * <p>该注解可应用于：
 * <ul>
 *   <li>{@link Component} 或 {@link Configuration} 标注的类（类级别）</li>
 *   <li>{@link Bean} 标注的工厂方法（方法级别）</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 类级别
 * @Component
 * @Primary
 * public class PrimaryUserService implements UserService { ... }
 *
 * // 方法级别
 * @Configuration
 * public class DataSourceConfig {
 *     @Bean
 *     @Primary
 *     public DataSource primaryDataSource() { ... }
 * }
 * }</pre>
 *
 * @see Component
 * @see Bean
 * @see Qualifier
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Primary {
}
