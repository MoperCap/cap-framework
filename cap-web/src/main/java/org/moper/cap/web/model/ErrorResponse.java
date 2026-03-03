package org.moper.cap.web.model;

import java.time.LocalDateTime;

/**
 * 标准错误响应体，用于统一封装异常处理结果。
 *
 * <p>使用示例：
 * <pre>{@code
 * @ExceptionHandler(IllegalArgumentException.class)
 * public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
 *     return ResponseEntity.badRequest(ErrorResponse.of(400, ex.getMessage()));
 * }
 * }</pre>
 *
 * @param code      错误码（通常与 HTTP 状态码一致）
 * @param message   错误描述
 * @param timestamp 发生时间
 * @param data      附加数据（可为 null）
 */
public record ErrorResponse(
        int code,
        String message,
        LocalDateTime timestamp,
        Object data
) {

    /**
     * 创建只含错误码和消息的 ErrorResponse。
     *
     * @param code    错误码
     * @param message 错误描述
     * @return ErrorResponse 实例
     */
    public static ErrorResponse of(int code, String message) {
        return new ErrorResponse(code, message, LocalDateTime.now(), null);
    }

    /**
     * 创建含附加数据的 ErrorResponse。
     *
     * @param code    错误码
     * @param message 错误描述
     * @param data    附加数据
     * @return ErrorResponse 实例
     */
    public static ErrorResponse of(int code, String message, Object data) {
        return new ErrorResponse(code, message, LocalDateTime.now(), data);
    }
}
