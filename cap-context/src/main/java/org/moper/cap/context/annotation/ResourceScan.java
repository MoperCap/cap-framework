package org.moper.cap.context.annotation;

import java.lang.annotation.*;

/**
 * 声明资源文件的扫描路径 </br>
 * 若未标注该注解，则默认扫描根路径 ""
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface ResourceScan {

    /**
     * 框架扫描的资源路径
     */
    String[] value() default { "" };
}
