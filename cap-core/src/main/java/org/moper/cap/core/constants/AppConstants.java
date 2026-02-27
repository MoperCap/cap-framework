package org.moper.cap.core.constants;

public final class AppConstants {

    /**
     * 系统内部属性管理平台名称
     */
    public static final String PROPERTY_OFFICER = "base-property-officer";

    /**
     * 系统内部命令行参数属性发布者名称
     */
    public static final String COMMAND_PUBLISHER = "commandline-property-publisher";

    /**
     * 系统内部资源文件属性发布者名称
     */
    public static final String RESOURCE_PUBLISHER = "base-resource-property-publisher";

    /**
     * 框架内部支持的基础资源配置文件列表
     */
    public static final String[] SUPPORTED_BASE_RESOURCES = { "application.yaml", "application.yml", "application.properties" };

    private AppConstants() {}
}
