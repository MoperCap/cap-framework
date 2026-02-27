package org.moper.cap.core.resource;

/**
 * 可析构资源接口 </br>
 *
 * 表示一种允许析构的资源 </br>
 */
public interface CloseableResource extends Resource{

    boolean isClosed();

    void close() throws Exception;
}
