package org.moper.cap.web.handler.result;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 响应实体，封装状态码、响应头和响应体。
 *
 * <p>提供常用 HTTP 状态码的工厂方法，方便控制器方法直接返回。
 *
 * <p>使用示例：
 * <pre>{@code
 * @GetMapping("/users/{id}")
 * public ResponseEntity<User> getUser(@PathVariable Long id) {
 *     User user = userService.findById(id);
 *     if (user == null) {
 *         return ResponseEntity.notFound();
 *     }
 *     return ResponseEntity.ok(user);
 * }
 * }</pre>
 *
 * @param <T>     响应体类型
 * @param status  HTTP 状态码
 * @param headers 响应头（不可变 Map）
 * @param body    响应体（可为 null）
 */
public record ResponseEntity<T>(
        int status,
        Map<String, String> headers,
        T body
) {

    public ResponseEntity {
        headers = headers != null ? Collections.unmodifiableMap(new HashMap<>(headers)) : Collections.emptyMap();
    }

    /**
     * 创建 200 OK 响应。
     *
     * @param body 响应体
     * @param <T>  响应体类型
     * @return 200 OK 响应实体
     */
    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(200, null, body);
    }

    /**
     * 创建 200 OK 无响应体的响应。
     *
     * @return 200 OK 响应实体
     */
    public static ResponseEntity<Void> ok() {
        return new ResponseEntity<>(200, null, null);
    }

    /**
     * 创建 201 Created 响应。
     *
     * @param body     响应体
     * @param location 新资源的 URI
     * @param <T>      响应体类型
     * @return 201 Created 响应实体
     */
    public static <T> ResponseEntity<T> created(T body, String location) {
        Map<String, String> headers = new HashMap<>();
        if (location != null) {
            headers.put("Location", location);
        }
        return new ResponseEntity<>(201, headers, body);
    }

    /**
     * 创建 201 Created 响应（无 Location 头）。
     *
     * @param body 响应体
     * @param <T>  响应体类型
     * @return 201 Created 响应实体
     */
    public static <T> ResponseEntity<T> created(T body) {
        return new ResponseEntity<>(201, null, body);
    }

    /**
     * 创建 404 Not Found 响应。
     *
     * @param <T> 响应体类型
     * @return 404 Not Found 响应实体
     */
    public static <T> ResponseEntity<T> notFound() {
        return new ResponseEntity<>(404, null, null);
    }

    /**
     * 创建 400 Bad Request 响应。
     *
     * @param body 错误信息
     * @param <T>  响应体类型
     * @return 400 Bad Request 响应实体
     */
    public static <T> ResponseEntity<T> badRequest(T body) {
        return new ResponseEntity<>(400, null, body);
    }

    /**
     * 创建 400 Bad Request 响应（无响应体）。
     *
     * @param <T> 响应体类型
     * @return 400 Bad Request 响应实体
     */
    public static <T> ResponseEntity<T> badRequest() {
        return new ResponseEntity<>(400, null, null);
    }

    /**
     * 创建 500 Internal Server Error 响应。
     *
     * @param body 错误信息
     * @param <T>  响应体类型
     * @return 500 Internal Server Error 响应实体
     */
    public static <T> ResponseEntity<T> internalServerError(T body) {
        return new ResponseEntity<>(500, null, body);
    }

    /**
     * 创建 500 Internal Server Error 响应（无响应体）。
     *
     * @param <T> 响应体类型
     * @return 500 Internal Server Error 响应实体
     */
    public static <T> ResponseEntity<T> internalServerError() {
        return new ResponseEntity<>(500, null, null);
    }
}
