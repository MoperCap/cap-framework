package org.moper.cap.bean.annotation;

import java.lang.annotation.*;

/**
 * 标记一个方法在 Bean 销毁前调用。
 *
 * <p>替代旧的 {@code @Capper(destroyMethod="...")} 方式，被标记的方法会在容器关闭
 * 或 Bean 作用域结束时，由容器自动调用，用于执行资源释放等清理工作。
 *
 * <p><b>方法签名要求：</b>
 * <ul>
 *   <li>访问修饰符：必须为 {@code public}</li>
 *   <li>返回类型：必须为 {@code void}</li>
 *   <li>参数列表：必须为空（无参）</li>
 * </ul>
 *
 * <p>每个类只能有一个 {@code @PreDestroy} 方法，若存在多个则抛出异常。
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Component
 * public class DatabaseConnection {
 *
 *     private Connection connection;
 *
 *     @PreDestroy
 *     public void cleanup() {
 *         // 容器关闭前执行清理逻辑
 *         if (connection != null) {
 *             connection.close();
 *         }
 *     }
 * }
 * }</pre>
 *
 * @see PostConstruct
 * @see Component
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreDestroy {
}
