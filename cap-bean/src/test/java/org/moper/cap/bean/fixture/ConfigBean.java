package org.moper.cap.bean.fixture;

import lombok.Getter;

/** 专用配置类，避免用 String 等通用类型作为依赖导致按类型查找歧义 */
@Getter
public class ConfigBean {
    private final String value;
    public ConfigBean(String value) { this.value = value; }
}