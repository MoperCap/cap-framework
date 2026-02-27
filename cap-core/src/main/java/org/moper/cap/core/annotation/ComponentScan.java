package org.moper.cap.core.annotation;

import org.moper.cap.bean.annotation.Component;

import java.lang.annotation.*;

/**
 * 声明软件包根路径 </br>
 * 若未标注该注解，则默认使用配置类所在的软件包作为扫描路径
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ComponentScan {

    /**
     * 要扫描的软件包根路径
     */
    String[] value();
}
