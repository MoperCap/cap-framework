package org.moper.cap.property.util;

/**
 * 可析构资源接口 </br>
 *
 * 表示一种允许析构的资源 </br>
 */
public interface PropertyLifecycle {

    /**
     * 资源名称
     */
    String name();

    boolean isClosed();

    void close() throws Exception;
}
