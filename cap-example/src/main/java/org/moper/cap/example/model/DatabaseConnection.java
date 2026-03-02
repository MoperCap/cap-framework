package org.moper.cap.example.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;

/**
 * 数据库连接，演示资源的初始化和销毁生命周期。
 *
 * <p>使用 {@code initMethod} 和 {@code destroyMethod} 声明初始化和销毁回调方法。
 */
@Getter
@Slf4j
public class DatabaseConnection {

    private final String url;
    private boolean connected = false;

    public DatabaseConnection() {
        this.url = "jdbc:default://localhost/default";
    }

    public DatabaseConnection(String url) {
        this.url = url;
    }

    private void connect() {
        this.connected = true;
        log.info("Connected to database: {}", url);
    }

    private void disconnect() {
        this.connected = false;
        log.info("Disconnected from database: {}", url);
    }

}
