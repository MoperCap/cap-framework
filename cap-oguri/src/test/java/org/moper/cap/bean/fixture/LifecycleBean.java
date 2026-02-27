package org.moper.cap.bean.fixture;

import org.moper.cap.bean.lifecycle.BeanLifecycle;

import java.util.ArrayList;
import java.util.List;

/** 实现了 BeanLifecycle 的 Bean，记录生命周期调用日志 */
public class LifecycleBean implements BeanLifecycle {

    public final List<String> callLog = new ArrayList<>();
    public boolean initShouldThrow   = false;
    public boolean destroyShouldThrow = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (initShouldThrow) {
            throw new RuntimeException("init failed intentionally");
        }
        callLog.add("afterPropertiesSet");
    }

    @Override
    public void destroy() throws Exception {
        if (destroyShouldThrow) {
            throw new RuntimeException("destroy failed intentionally");
        }
        callLog.add("destroy");
    }
}