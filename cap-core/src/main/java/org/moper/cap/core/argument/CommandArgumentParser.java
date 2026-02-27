package org.moper.cap.core.argument;

import java.util.Map;

public interface CommandArgumentParser {

    /**
     * 将命令行参数解析为属性Map
     *
     * @return 解析后的属性Map，键为属性名，值为属性值
     */
    Map<String, Object> parse();
}
