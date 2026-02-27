package org.moper.cap.boot.application;

import org.moper.cap.context.context.RuntimeContext;

public interface CapApplication{
    RuntimeContext run() throws Exception;
}
