package org.moper.cap.boot.application;

import org.moper.cap.context.context.ApplicationContext;

public interface CapApplication{
    ApplicationContext run() throws Exception;
}
