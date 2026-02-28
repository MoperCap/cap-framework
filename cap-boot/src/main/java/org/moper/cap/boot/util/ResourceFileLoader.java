package org.moper.cap.boot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.*;

/**
 * 资源加载工具类 </br>
 *
 * 提供加载YAML和Properties格式的资源文件内容的方法，并支持将嵌套结构扁平化为键值对。 </br>
 */
public final class ResourceFileLoader {
    private static final ObjectMapper YamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * 加载YAML格式的资源文件内容，并根据需要将嵌套结构扁平化为键值对 </br>
     *
     * @param is 资源文件输入流
     * @param isFlatten 是否将嵌套结构扁平化为键值对，true表示扁平化，false表示保持原始嵌套结构
     * @return 加载并解析后的资源内容，返回一个Map对象，其中键表示属性名称，值表示属性值。根据isFlatten参数的不同，返回的Map结构可能是嵌套的也可能是扁平化的。
     * @throws Exception 加载或解析过程中可能抛出的异常，例如输入流读取错误、YAML格式错误等。
     */
    public static Map<String, Object> loadYaml(InputStream is, boolean isFlatten) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = YamlMapper.readValue(is, Map.class);
        if(result == null) return Collections.emptyMap();

        if(isFlatten){
            result = flatten(result);
        }

        return result;
    }

    /**
     * 加载Properties格式的资源文件内容 </br>
     *
     * @param is 资源文件输入流
     * @return 加载并解析后的资源内容，返回一个Map对象，其中键表示属性名称，值表示属性值。由于Properties文件本身是键值对结构，因此返回的Map结构是扁平化的。
     * @throws Exception 加载或解析过程中可能抛出的异常，例如输入流读取错误、Properties格式错误等。
     */
    public static Map<String, Object> loadProperties(InputStream is) throws Exception {
        Properties props = new Properties();
        props.load(is);
        Map<String, Object> map = new LinkedHashMap<>();
        for(String key : props.stringPropertyNames()) {
            map.put(key, props.getProperty(key));
        }
        return map;
    }

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
