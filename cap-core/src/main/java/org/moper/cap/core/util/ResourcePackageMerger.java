package org.moper.cap.core.util;

import java.util.*;

/**
 * 软件包整合器 </br>
 *
 * 用于将多个软件包路径进行整合，去除重复和子路径，返回一个简化的软件包列表。 </br>
 */
public final class ResourcePackageMerger {

    public static List<String> merge(Collection<String> rawPackages){
        // 去空、去重、标准化
        Set<String> pkgs = new LinkedHashSet<>();
        for (String pkg : rawPackages) {
            if (pkg != null) {
                String s = pkg.trim();
                if (!s.isEmpty()) pkgs.add(s);
            }
        }
        List<String> pkgList = new ArrayList<>(pkgs);
        pkgList.sort(Comparator.naturalOrder()); // 保证顺序

        List<String> result = new ArrayList<>();
        for (String candidate : pkgList) {
            boolean isSub = false;
            for (String chosen : result) {
                if (isSubPackage(candidate, chosen)) {
                    isSub = true;
                    break;
                }
            }
            if (!isSub) {
                result.add(candidate);
            }
        }
        return result;
    }

    /**
     * 判断 candidate 是否为 base 的子包或自身，如 org.a.b 是 org.a 的子包
     */
    private static boolean isSubPackage(String candidate, String base) {
        if (candidate.equals(base)) {
            return false; // 自身不是其子包
        }
        return candidate.startsWith(base + ".");
    }
}
