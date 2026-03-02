package org.moper.cap.example.model;

import lombok.Getter;

/**
 * 应用配置，由工厂方法创建。
 */
@Getter
public class AppConfig {

    private final String appName;
    private final String version;

    public AppConfig(String appName, String version) {
        this.appName = appName;
        this.version = version;
    }

    @Override
    public String toString() {
        return "AppConfig{appName='" + appName + "', version='" + version + "'}";
    }
}
