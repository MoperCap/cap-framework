package org.moper.cap.bean.fixture;

/** 最简单的无参构造 Bean，用于基础实例化测试 */
public class SimpleBean {
    private String value;

    public SimpleBean() {}

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}