package org.moper.cap.bean.annotation;

import java.lang.annotation.*;

/**
 * 标记一个方法为 Bean 工厂方法，每个 {@code @Bean} 方法生成一个独立的 BeanDefinition。
 *
 * <p>该注解用于 {@link Configuration} 类中，方法的返回类型即为 Bean 的类型。
 * Bean 实例由该方法负责创建并返回，方法的参数由容器自动注入。
 *
 * <p>Bean 名称解析规则（按优先级）：
 * <ol>
 *   <li>{@link #value()} 属性中指定的名称（支持多个，第一个为主名称，其余为别名）</li>
 *   <li>{@link #name()} 属性中指定的名称（与 {@code value} 等价）</li>
 *   <li>方法名（首字母小写）作为默认 Bean 名称</li>
 * </ol>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Configuration
 * public class AppConfig {
 *
 *     // 使用方法名 "dataSource" 作为 Bean 名称
 *     @Bean
 *     public DataSource dataSource() {
 *         return new HikariDataSource();
 *     }
 *
 *     // 显式指定多个名称，"primary" 为主名称，"ds" 为别名
 *     @Bean({"primary", "ds"})
 *     @Primary
 *     public DataSource primaryDataSource() {
 *         return new HikariDataSource();
 *     }
 * }
 * }</pre>
 *
 * @see Configuration
 * @see Primary
 * @see Lazy
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {

    /**
     * Bean 名称列表，第一个为主名称，其余为别名。
     * 默认为空数组，表示使用方法名（首字母小写）作为 Bean 名称。
     *
     * @return Bean 名称数组
     */
    String[] value() default {};

    /**
     * Bean 名称列表，与 {@link #value()} 等价（互为别名属性）。
     * 若同时指定 {@code value} 和 {@code name}，则合并两者（去重）。
     *
     * @return Bean 名称数组
     */
    String[] name() default {};
}
