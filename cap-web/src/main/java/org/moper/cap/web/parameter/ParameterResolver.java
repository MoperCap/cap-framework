package org.moper.cap.web.parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * 方法参数解析器接口。
 *
 * <p>每个实现负责将 HTTP 请求中的特定数据（查询参数、路径变量、请求体等）
 * 解析并转换为方法参数值。
 */
public interface ParameterResolver {

    /**
     * 判断此解析器是否支持给定的参数。
     *
     * @param metadata 参数元数据
     * @return 如果支持则返回 {@code true}
     */
    boolean supports(ParameterMetadata metadata);

    /**
     * 解析参数值。
     *
     * @param metadata      参数元数据
     * @param request       HTTP 请求
     * @param response      HTTP 响应
     * @param pathVariables 已提取的路径变量
     * @return 解析出的参数值（可为 null）
     * @throws Exception 解析过程中可能抛出的异常
     */
    Object resolve(ParameterMetadata metadata,
                   HttpServletRequest request,
                   HttpServletResponse response,
                   Map<String, String> pathVariables) throws Exception;
}
