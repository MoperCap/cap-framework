package org.moper.cap.bean.annotation;

import java.lang.annotation.*;

/**
 * 标记一个 Bean 为懒加载 Bean。
 *
 * <p>标记了 {@code @Lazy} 的 Bean 不会在容器启动时立即创建，
 * 而是在第一次被请求（注入或通过 {@code getBean} 获取）时才创建实例。
 * 该行为仅对单例（SINGLETON）作用域的 Bean 有意义。
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
 * @Lazy
 * public class HeavyService { ... }
 *
 * // 方法级别
 * @Configuration
 * public class AppConfig {
 *     @Bean
 *     @Lazy
 *     public ExpensiveResource expensiveResource() { ... }
 * }
 * }</pre>
 *
 * @see Component
 * @see Bean
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Lazy {
}
