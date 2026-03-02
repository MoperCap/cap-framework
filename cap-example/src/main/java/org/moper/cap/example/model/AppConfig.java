package org.moper.cap.example.model;

/**
 * 应用配置，由工厂方法创建。
 */
public class AppConfig {

    private final String appName;
    private final String version;

    public AppConfig(String appName, String version) {
        this.appName = appName;
        this.version = version;
    }

    public String getAppName() {
        return appName;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "AppConfig{appName='" + appName + "', version='" + version + "'}";
    }
}
