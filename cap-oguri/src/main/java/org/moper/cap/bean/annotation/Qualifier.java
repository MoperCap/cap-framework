package org.moper.cap.bean.annotation;

import java.lang.annotation.*;

/**
 * 在存在多个同类型 Bean 候选者时，显式指定要注入的 Bean 名称。
 *
 * <p>当容器中存在多个相同类型的 Bean，且没有 {@link Primary} 标记时，
 * 使用 {@code @Qualifier} 可以精确指定要注入的 Bean 名称，消除歧义。
 *
 * <p>该注解可应用于：
 * <ul>
 *   <li>字段（{@link ElementType#FIELD}）：与 {@link Autowired} 配合使用</li>
 *   <li>构造函数参数（{@link ElementType#PARAMETER}）：精确指定参数对应的 Bean</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Component
 * public class PaymentService {
 *
 *     // 字段注入时指定 Bean 名称
 *     @Autowired
 *     @Qualifier("mysqlDataSource")
 *     private DataSource dataSource;
 *
 *     // 构造函数参数注入时指定 Bean 名称
 *     public PaymentService(@Qualifier("primaryDataSource") DataSource ds) {
 *         ...
 *     }
 * }
 * }</pre>
 *
 * @see Autowired
 * @see Primary
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Qualifier {

    /**
     * 要注入的 Bean 名称。
     *
     * @return Bean 名称字符串，不能为空
     */
    String value();
}
