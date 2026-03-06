package org.moper.cap.transaction.context;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 基于 {@link ThreadLocal} 栈的事务上下文，每个线程独立维护自己的事务栈。
 *
 * <p>通过栈结构支持嵌套事务：每次 {@link #beginTransaction} 压栈，
 * {@link #endTransaction} 出栈。{@link #getCurrentTransaction()} 始终返回栈顶的事务信息。
 *
 * <p>{@link #suspendTransaction()} 和 {@link #resumeTransaction(TransactionInfo)} 用于
 * {@link org.moper.cap.transaction.annotation.Propagation#REQUIRES_NEW} 和
 * {@link org.moper.cap.transaction.annotation.Propagation#NOT_SUPPORTED} 传播性，
 * 实现将当前事务暂时移出线程上下文并在之后恢复的语义。
 */
public final class TransactionContext {

    private static final ThreadLocal<Deque<TransactionInfo>> TX_STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

    private TransactionContext() {}

    /**
     * 将新事务信息压入当前线程的事务栈。
     *
     * @param connection     事务使用的数据库连接
     * @param isNew          是否是新开启的事务（false 表示复用外层连接）
     * @param readOnly       是否只读事务
     * @param isolationLevel JDBC 隔离级别值
     */
    public static void beginTransaction(Connection connection, boolean isNew,
                                        boolean readOnly, int isolationLevel) {
        TX_STACK.get().push(new TransactionInfo(connection, isNew, readOnly, isolationLevel));
    }

    /**
     * 从当前线程的事务栈中弹出栈顶事务信息，并在栈为空时清理 ThreadLocal。
     */
    public static void endTransaction() {
        Deque<TransactionInfo> stack = TX_STACK.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            TX_STACK.remove();
        }
    }

    /**
     * 返回当前线程的栈顶事务信息，若无活跃事务则返回 {@code null}。
     */
    public static TransactionInfo getCurrentTransaction() {
        Deque<TransactionInfo> stack = TX_STACK.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    /**
     * 返回当前线程的事务嵌套深度（栈大小）。
     */
    public static int getTransactionDepth() {
        return TX_STACK.get().size();
    }

    /**
     * 挂起当前事务：将栈顶事务信息弹出并返回，后续代码将运行在无事务（或外层事务）中。
     *
     * @return 被挂起的事务信息；若当前无活跃事务则返回 {@code null}
     */
    public static TransactionInfo suspendTransaction() {
        Deque<TransactionInfo> stack = TX_STACK.get();
        if (stack.isEmpty()) {
            return null;
        }
        TransactionInfo suspended = stack.pop();
        if (stack.isEmpty()) {
            TX_STACK.remove();
        }
        return suspended;
    }

    /**
     * 恢复之前挂起的事务：将挂起的事务信息重新压入栈顶。
     *
     * @param txInfo 之前由 {@link #suspendTransaction()} 返回的事务信息；
     *               传入 {@code null} 时本方法为空操作
     */
    public static void resumeTransaction(TransactionInfo txInfo) {
        if (txInfo != null) {
            TX_STACK.get().push(txInfo);
        }
    }

    /**
     * 当前事务的上下文信息，由事务管理器负责创建和维护。
     */
    public static class TransactionInfo {

        private final Connection connection;
        private final boolean isNew;
        private final boolean readOnly;
        private final int isolationLevel;
        private boolean rollbackOnly = false;
        private Savepoint savepoint;

        public TransactionInfo(Connection connection, boolean isNew,
                               boolean readOnly, int isolationLevel) {
            this.connection = connection;
            this.isNew = isNew;
            this.readOnly = readOnly;
            this.isolationLevel = isolationLevel;
        }

        public Connection getConnection() {
            return connection;
        }

        public boolean isNew() {
            return isNew;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public int getIsolationLevel() {
            return isolationLevel;
        }

        public boolean isRollbackOnly() {
            return rollbackOnly;
        }

        public void setRollbackOnly(boolean rollbackOnly) {
            this.rollbackOnly = rollbackOnly;
        }

        public Savepoint getSavepoint() {
            return savepoint;
        }

        public void setSavepoint(Savepoint savepoint) {
            this.savepoint = savepoint;
        }
    }
}
