package org.moper.cap.example;

import org.moper.cap.core.annotation.ComponentScan;
import org.moper.cap.core.annotation.ResourceScan;

/**
 * 示例应用配置类。
 *
 * <p>作为 {@link org.moper.cap.boot.application.impl.DefaultCapApplication} 的
 * {@code primarySource} 传入，用于声明组件扫描路径和资源扫描路径。
 *
 * <ul>
 *   <li>{@link ComponentScan} 指定扫描 {@code org.moper.cap.example} 包下所有
 *       标注了 {@link org.moper.cap.bean.annotation.Capper} 的类和方法。</li>
 *   <li>{@link ResourceScan} 默认扫描根路径，加载 {@code application.yaml}。</li>
 * </ul>
 */
@ComponentScan({"org.moper.cap.example", "org.moper.cap.data.config"})
@ResourceScan
public class ExampleApplication {
}
