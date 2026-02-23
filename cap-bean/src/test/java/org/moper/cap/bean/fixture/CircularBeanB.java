package org.moper.cap.bean.fixture;

/** 用于循环依赖测试：B 依赖 A */
public class CircularBeanB {
    public CircularBeanB(CircularBeanA a) {}
}