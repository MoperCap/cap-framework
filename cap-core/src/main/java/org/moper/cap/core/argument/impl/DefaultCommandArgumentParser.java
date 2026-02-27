package org.moper.cap.core.argument.impl;

import org.moper.cap.core.argument.CommandArgumentParser;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 默认的命令行参数解析器 </br>
 *
 * 支持的参数格式：
 * <ul>
 *   <li><code>--key=value</code></li>
 *   <li><code>--key value</code></li>
 *   <li><code>--flag</code> (解析为 Boolean.TRUE)</li>
 *   <li><code>-Dkey=value</code></li>
 *   <li><code>-k=value</code></li>
 *   <li><code>-k value</code></li>
 * </ul>
 */
public class DefaultCommandArgumentParser implements CommandArgumentParser {

    private final String[] args;

    public DefaultCommandArgumentParser(String[] args) {
        this.args = args;
    }

    /**
     * 将命令行参数解析为属性Map </br>
     *
     * @return 解析后的属性Map，键为属性名，值为属性值
     */
    @Override
    public Map<String, Object> parse() {
        Map<String, Object> result = new LinkedHashMap<>();
        if (args == null) return result;

        int i = 0;
        while (i < args.length) {
            String arg = args[i];

            // 1. 处理 -Dkey=value
            if (arg.startsWith("-D")) {
                String pair = arg.substring(2);
                int eq = pair.indexOf('=');
                if (eq > 0) {
                    String key = pair.substring(0, eq);
                    String value = pair.substring(eq + 1);
                    result.put(key, value);
                }
                i++;
                continue;
            }

            // 2. 处理 --key=value 或 -k=value
            if ((arg.startsWith("--") || arg.startsWith("-")) && arg.contains("=")) {
                String trimmed = arg.replaceFirst("^-+", "");
                int eq = trimmed.indexOf('=');
                if (eq > 0) {
                    String key = trimmed.substring(0, eq);
                    String value = trimmed.substring(eq + 1);
                    result.put(key, value);
                }
                i++;
                continue;
            }

            // 3. 处理 --key value 或 -k value
            if ((arg.startsWith("--") || arg.startsWith("-")) && (i + 1) < args.length && !args[i + 1].startsWith("-")) {
                String key = arg.replaceFirst("^-+", "");
                String value = args[i + 1];
                result.put(key, value);
                i += 2;
                continue;
            }

            // 4. 处理 --flag 或 -flag，视为布尔true
            if (arg.startsWith("--") || arg.startsWith("-")) {
                String flag = arg.replaceFirst("^-+", "");
                result.put(flag, Boolean.TRUE);
                i++;
                continue;
            }

            // 其它情况跳过（如位置参数，可扩展专属逻辑）
            i++;
        }
        return result;
    }
}
