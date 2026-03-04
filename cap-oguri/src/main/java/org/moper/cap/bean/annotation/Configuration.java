package org.moper.cap.bean.annotation;

import java.lang.annotation.*;

/**
 * 标记一个类为配置类，用于包含 {@link Bean} 工厂方法。
 *
 * <p>该注解本身带有 {@link Component} 元注解，因此配置类本身也会作为 Bean 注册到容器中。
 * {@code ConfigurationBeanRegisterBootstrapRunner} 会扫描所有 {@code @Configuration} 类
 * 并将其中的 {@link Bean} 方法注册为独立的 BeanDefinition。
 *
 * <p><b>注意：</b>{@code @ComponentScan} 和 {@code @ResourceScan} 注解由 {@code cap-core}
 * 模块处理，不属于本注解的职责范围。
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Configuration
 * public class AppConfig {
 *
 *     @Bean
 *     public DataSource dataSource() {
 *         return new HikariDataSource(...);
 *     }
 *
 *     @Bean("myService")
 *     @Primary
 *     public MyService myService(DataSource dataSource) {
 *         return new MyServiceImpl(dataSource);
 *     }
 * }
 * }</pre>
 *
 * @see Component
 * @see Bean
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Configuration {

    /**
     * Bean 名称，默认为空字符串，表示使用类名首字母小写作为 Bean 名称。
     *
     * @return Bean 名称
     */
    String value() default "";
}
