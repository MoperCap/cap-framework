package org.moper.cap.example.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.property.annotation.Value;

/**
 * 属性注入示例 Bean，演示 {@link Value} 注解从配置文件中读取属性。
 *
 * <p>对应 {@code application.yaml} 中的 {@code app.*} 配置项。
 */
@Slf4j
@Getter
@Capper
public class AppPropertyConfig {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Value("${app.description}")
    private String appDescription;
}
