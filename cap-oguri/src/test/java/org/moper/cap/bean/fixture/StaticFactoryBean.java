package org.moper.cap.bean.fixture;

import lombok.Getter;

@Getter
public class StaticFactoryBean {
    private final String source;

    private StaticFactoryBean(String source) { this.source = source; }

    public static StaticFactoryBean create() {
        return new StaticFactoryBean("static-factory");
    }

    // 参数改为 ConfigBean，避免 String 类型歧义
    public static StaticFactoryBean createWithArg(ConfigBean config) {
        return new StaticFactoryBean(config.getValue());
    }

}