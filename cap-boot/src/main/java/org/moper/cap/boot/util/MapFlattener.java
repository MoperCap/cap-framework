package org.moper.cap.boot.util;

import java.util.*;

/**
 * Map扁平化工具类 </br>
 *
 * 将嵌套的Map结构转换为扁平化的Map结构，键表示原始嵌套结构中的路径，值表示对应的值。 </br>
 */
public final class MapFlattener {

    /**
     * 将嵌套的Map结构转换为扁平化的Map结构 </br>
     *
     * @param source 要扁平化的嵌套Map结构
     * @return 扁平化后的Map结构，其中键表示原始嵌套结构中的路径，值表示对应的值
     */
    public static Map<String, Object> flatten(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        flatten(null, source, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void flatten(String path, Object value, Map<String, Object> to) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = (path == null || path.isEmpty())
                        ? entry.getKey()
                        : path + "." + entry.getKey();
                flatten(key, entry.getValue(), to);
            }
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            for (int i = 0; i < list.size(); i++) {
                String key = String.format("%s[%d]", path, i);
                flatten(key, list.get(i), to);
            }
        } else {
            to.put(path, value);
        }
    }
}