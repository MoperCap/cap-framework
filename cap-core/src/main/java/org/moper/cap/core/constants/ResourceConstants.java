package org.moper.cap.core.constants;

import java.util.Arrays;
import java.util.List;

public final class ResourceConstants {

    /**
     * 系统内部属性管理平台名称
     */
    public static final String PROPERTY_OFFICER = "base-property-officer";

    /**
     * 系统内部命令行参数属性发布者名称
     */
    public static final String COMMAND_PUBLISHER = "commandline-property-publisher";

    /**
     * 系统内部系统属性发布者名称
     */
    public static final String SYSTEM_PROPERTY_PUBLISHER = "system-property-publisher";

    /**
     * 系统内部资源文件属性发布者名称
     */
    public static final String RESOURCE_PUBLISHER_PREFIX = "resource-property-publisher";

    /**
     * 框架内部支持的资源文件前缀
     */
    public static final String SUPPORTED_RESOURCE_PREFIX = "application";

    /**
     * 框架内部支持的资源文件后缀列表
     */
    public static final List<String> SUPPORTED_RESOURCE_SUFFIXES = Arrays.asList(".yaml", ".yml", ".properties");

    /**
     * 框架内部支持的活动环境属性键
     */
    public static final String SUPPORTED_ACTIVE_PROFILE_PROPERTY_KEY = "application.profiles.active";

    /**
     * 生成资源文件属性发布者名称
     *
     * @param publisherName 资源发布者名称，不能为空或空字符串
     * @return 生成的资源文件属性发布者名称，格式为 "resource-property-publisher[<publisherName>]"
     */
    public static String getResourcePublisherNam(String publisherName) {
        if (publisherName == null || publisherName.isEmpty()) {
            throw new IllegalArgumentException("Publisher name cannot be null or empty");
        }

        return RESOURCE_PUBLISHER_PREFIX + "[" + publisherName + "]";
    }

    /**
     * 生成基于活动环境的资源文件属性发布者名称
     *
     * @param profile 活动环境名称，不能为空或空字符串
     * @return 生成的基于活动环境的资源文件属性发布者名称，格式为 "<profile>-resource-property-publisher"
     */
    public static String getActiveProfileResourceName(String profile) {
        if (profile == null || profile.isEmpty()) {
            throw new IllegalArgumentException("Profile cannot be null or empty");
        }

        return profile + "-" + RESOURCE_PUBLISHER_PREFIX;
    }

    private ResourceConstants() {
    }
}
