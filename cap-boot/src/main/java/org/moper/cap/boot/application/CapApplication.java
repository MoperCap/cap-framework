package org.moper.cap.boot.application;

import org.moper.cap.core.context.RuntimeContext;

public interface CapApplication{
    RuntimeContext run() throws Exception;
}
