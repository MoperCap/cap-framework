package org.moper.cap.transaction.manager;

import java.sql.Connection;

/**
 * 数据库连接提供者接口，为数据访问层提供获取当前连接的标准入口。
 *
 * <p>在事务上下文中，连接由事务管理器绑定到当前线程；
 * 在无事务上下文中，连接来自数据源（调用方负责关闭）。
 */
@FunctionalInterface
public interface ConnectionProvider {

    /**
     * 获取当前线程的数据库连接。
     *
     * <p>若当前存在活跃事务，返回事务连接（事务管理器负责关闭）；
     * 否则返回一个自动提交的新连接（<b>调用方负责关闭</b>）。
     *
     * @return 数据库连接，永不为 {@code null}
     * @throws Exception 获取连接失败
     */
    Connection getConnection() throws Exception;
}
