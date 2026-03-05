package org.moper.cap.core.util;

import java.util.*;

/**
 * 路径整合器 </br>
 *
 * 用于将多个路径进行整合，去除重复和子路径，返回一个简化的路径列表。 </br>
 */
public final class ResourcePathMerger {

    /**
     * 路径整合方法 </br>
     *
     * 将输入的路径集合进行整合，去除重复和子路径，返回一个简化的路径列表。 </br>
     *
     * @param rawPaths 要整合的原始路径集合，路径可以是相对路径或绝对路径
     * @return 整合后的路径列表，包含唯一的绝对路径，且不包含任何子路径
     */
    public static List<String> merge(Collection<String> rawPaths) {
        List<String> allPaths =rawPaths.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(ResourcePathMerger::normalize)
                .toList();
        List<String> merged = new ArrayList<>();

        for(String candidate : allPaths){
            boolean isSub = false;
            for(String chosen : merged){
                if(isSubPath(candidate, chosen)){
                    isSub = true;
                    break;
                }
            }
            if(!isSub) merged.add(candidate);
        }

        return merged.stream().map(ResourcePathMerger::restore).toList();
    }

    /**
     * 规范化单一路径：所有分隔替换成 /，开头不加 /，尾部强制加 /
     */
    private static String normalize(String path) {
        String p = path.replace("\\", "/");
        // 清空多余的分隔符
        while (p.contains("//")) {
            p = p.replace("//", "/");
        }
        if (!p.endsWith("/")) p = p + "/";
        // 可选：全部转小写（如不敏感场景）
        return p;
    }

    /**
     * 判断 candidate 是否是 base 的子路径或自身（即 base/foo/xxx 等）
     */
    private static boolean isSubPath(String candidate, String base) {
        // 父路径一定是候选路径的前缀
        return !candidate.equals(base) && candidate.startsWith(base);
    }

    /**
     * 还原结尾（去掉末尾/，但根路径和空字符串不处理）
     */
    private static String restore(String path) {
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
