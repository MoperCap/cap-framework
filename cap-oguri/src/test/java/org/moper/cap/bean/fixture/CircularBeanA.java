package org.moper.cap.bean.fixture;

/** 用于循环依赖测试：A 依赖 B */
public class CircularBeanA {
    public CircularBeanA(CircularBeanB b) {}
}