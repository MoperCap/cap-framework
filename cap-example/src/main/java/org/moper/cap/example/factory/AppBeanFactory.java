package org.moper.cap.example.factory;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.example.model.AppConfig;
import org.moper.cap.example.model.DatabaseConnection;

/**
 * 工厂类，演示静态和非静态 {@link Capper} 工厂方法。
 *
 * <ul>
 *   <li><b>静态工厂方法</b>：{@link #databaseConnection()} —— 返回名称默认为方法名
 *       {@code "databaseConnection"}，与 {@link DatabaseConnection} 类注册的同名 Bean 冲突，
 *       工厂方法优先级更高，会覆盖类级别的 Bean 定义。</li>
 *   <li><b>非静态工厂方法</b>：{@link #appConfig()} —— 使用显式名称 {@code "appConfig"}
 *       注册应用配置 Bean，工厂类本身由容器自动管理。</li>
 * </ul>
 */
@Slf4j
public class AppBeanFactory {

    /**
     * 静态工厂方法，创建带特定连接 URL 的 {@link DatabaseConnection}。
     *
     * <p>由于方法名为 {@code databaseConnection}，解析后的 Bean 名称也为
     * {@code databaseConnection}，与 {@link DatabaseConnection} 类级别的 Bean 同名，
     * 因此该工厂方法的定义会覆盖类级别的 Bean 定义。
     */
    @Capper
    public static DatabaseConnection databaseConnection() {
        log.info("Creating DatabaseConnection via static factory method");
        return new DatabaseConnection("jdbc:example://localhost:5432/example");
    }

    /**
     * 非静态工厂方法，创建 {@link AppConfig} Bean。
     */
    @Capper(names = {"appConfig"}, description = "应用配置Bean")
    public AppConfig appConfig() {
        log.info("Creating AppConfig via instance factory method");
        return new AppConfig("cap-example", "1.0-SNAPSHOT");
    }
}
