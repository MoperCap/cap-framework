package org.moper.cap.core.context;

public interface ResourceContext {

    boolean isClosed();

    void close() throws Exception;
}
