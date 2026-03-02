package org.moper.cap.example.service;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.example.model.DatabaseConnection;

/**
 * 用户服务，演示构造函数注入和初始化/销毁生命周期。
 *
 * <ul>
 *   <li>使用 {@link Inject} 标注构造函数，注入 {@link DatabaseConnection}。</li>
 *   <li>使用 {@code initMethod} 在 Bean 就绪后执行初始化逻辑。</li>
 *   <li>使用 {@code destroyMethod} 在容器关闭时执行清理逻辑。</li>
 * </ul>
 */
@Slf4j
@Capper(initMethod = "init", destroyMethod = "destroy")
public class UserService {

    private final DatabaseConnection dbConnection;
    private boolean initialized = false;

    @Inject
    public UserService(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    private void init() {
        this.initialized = true;
        log.info("UserService initialized, db connected={}", dbConnection.isConnected());
    }

    private void destroy() {
        this.initialized = false;
        log.info("UserService destroyed");
    }

    public String findUser(long id) {
        return "User#" + id + " (via " + dbConnection.getUrl() + ")";
    }

    public boolean isInitialized() {
        return initialized;
    }
}
