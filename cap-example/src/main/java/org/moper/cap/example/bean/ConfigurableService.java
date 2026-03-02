package org.moper.cap.example.bean;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.property.annotation.Value;

/**
 * 属性注入示例，演示 {@link Value} 注解的各种使用方式。
 *
 * <ul>
 *   <li>{@code ${app.name}} — 直接注入属性值</li>
 *   <li>{@code ${app.port:8080}} — 带默认值的属性注入（属性存在时使用属性值）</li>
 *   <li>{@code ${db.url}} — 注入数据库 URL</li>
 *   <li>{@code ${app.version:1.0.0}} — 使用默认值（属性存在时使用属性值）</li>
 *   <li>{@code ${missing.key:default}} — 属性不存在时使用默认值</li>
 * </ul>
 */
@Slf4j
@Getter
@Capper
public class ConfigurableService {

    @Value("${app.name}")
    private String appName;

    @Value("${app.port:8080}")
    private Integer appPort;

    @Value("${db.url}")
    private String dbUrl;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${missing.key:defaultFallback}")
    private String missingKeyWithDefault;

    public String getServiceInfo() {
        String info = "ConfigurableService{appName='" + appName + "', appPort=" + appPort
                + ", dbUrl='" + dbUrl + "', appVersion='" + appVersion + "'}";
        log.info(info);
        return info;
    }
}
