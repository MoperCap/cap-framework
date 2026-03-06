package org.moper.cap.transaction.context;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.util.Stack;

/**
 * 事务上下文 - 使用 ThreadLocal 存储当前线程的事务信息
 *
 * <p>支持嵌套事务：使用 Stack 存储事务堆栈，实现嵌套事务的支持。
 * <p>支持事务挂起/恢复（用于 REQUIRES_NEW 和 NOT_SUPPORTED 传播性）。
 */
@Slf4j
public class TransactionContext {

    private static final ThreadLocal<Stack<TransactionInfo>> TRANSACTION_STACK =
            ThreadLocal.withInitial(Stack::new);

    /**
     * 事务信息
     */
    @Getter
    @Setter
    public static class TransactionInfo {
        private Connection connection;
        private boolean isNew;
        private boolean readOnly;
        private long startTime;
        private int isolationLevel;

        public TransactionInfo(Connection connection, boolean isNew, boolean readOnly, int isolationLevel) {
            this.connection = connection;
            this.isNew = isNew;
            this.readOnly = readOnly;
            this.isolationLevel = isolationLevel;
            this.startTime = System.currentTimeMillis();
        }
    }

    /**
     * 开始事务
     */
    public static void beginTransaction(Connection connection, boolean isNew, boolean readOnly, int isolationLevel) {
        Stack<TransactionInfo> stack = TRANSACTION_STACK.get();
        TransactionInfo info = new TransactionInfo(connection, isNew, readOnly, isolationLevel);
        stack.push(info);
        log.debug("开始事务: depth={}, isNew={}", stack.size(), isNew);
    }

    /**
     * 获取当前事务信息
     */
    public static TransactionInfo getCurrentTransaction() {
        Stack<TransactionInfo> stack = TRANSACTION_STACK.get();
        if (stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }

    /**
     * 是否在事务中
     */
    public static boolean inTransaction() {
        return !TRANSACTION_STACK.get().isEmpty();
    }

    /**
     * 结束事务（弹出堆栈）
     */
    public static TransactionInfo endTransaction() {
        Stack<TransactionInfo> stack = TRANSACTION_STACK.get();
        if (!stack.isEmpty()) {
            TransactionInfo info = stack.pop();
            log.debug("结束事务: remaining depth={}", stack.size());

            // 如果堆栈为空，清理 ThreadLocal
            if (stack.isEmpty()) {
                TRANSACTION_STACK.remove();
            }

            return info;
        }
        return null;
    }

    /**
     * 挂起当前事务（弹出堆栈并返回，供 REQUIRES_NEW / NOT_SUPPORTED 使用）。
     *
     * @return 被挂起的事务信息，若当前无活跃事务则返回 {@code null}
     */
    public static TransactionInfo suspendTransaction() {
        Stack<TransactionInfo> stack = TRANSACTION_STACK.get();
        if (!stack.isEmpty()) {
            TransactionInfo suspended = stack.pop();
            log.debug("挂起事务: remaining depth={}", stack.size());
            if (stack.isEmpty()) {
                TRANSACTION_STACK.remove();
            }
            return suspended;
        }
        return null;
    }

    /**
     * 恢复之前挂起的事务（压回堆栈，供 REQUIRES_NEW / NOT_SUPPORTED 使用）。
     *
     * <p>如果 {@link #suspendTransaction()} 清除了 ThreadLocal（因堆栈已空），
     * {@code TRANSACTION_STACK.get()} 将通过 {@code withInitial} 初始化器返回一个新的空堆栈，
     * 保证不会发生 {@link NullPointerException}。
     *
     * @param txInfo 之前挂起的事务信息；若为 {@code null} 则不执行任何操作
     */
    public static void resumeTransaction(TransactionInfo txInfo) {
        if (txInfo != null) {
            TRANSACTION_STACK.get().push(txInfo);
            log.debug("恢复事务: depth={}", TRANSACTION_STACK.get().size());
        }
    }

    /**
     * 清空所有事务信息
     */
    public static void clear() {
        TRANSACTION_STACK.remove();
    }

    /**
     * 获取事务深度（嵌套层级）
     */
    public static int getTransactionDepth() {
        return TRANSACTION_STACK.get().size();
    }
}
