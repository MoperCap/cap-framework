package org.moper.cap.web.view;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 响应实体
 *
 * 用于封装：
 * - 状态码
 * - 响应头
 * - 响应体
 */
public class ResponseEntity<T> {

    private final int statusCode;
    private final Map<String, String> headers;
    private final T body;

    public ResponseEntity(int statusCode, Map<String, String> headers, T body) {
        this.statusCode = statusCode;
        this.headers = headers != null ? headers : new HashMap<>();
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public T getBody() {
        return body;
    }

    public static <T> Builder<T> ok() {
        return new Builder<>(200);
    }

    public static <T> Builder<T> created() {
        return new Builder<>(201);
    }

    public static <T> Builder<T> badRequest() {
        return new Builder<>(400);
    }

    public static <T> Builder<T> notFound() {
        return new Builder<>(404);
    }

    public static <T> Builder<T> status(int statusCode) {
        return new Builder<>(statusCode);
    }

    public static class Builder<T> {
        private final int statusCode;
        private final Map<String, String> headers = new HashMap<>();
        private T body;

        public Builder(int statusCode) {
            this.statusCode = statusCode;
        }

        public Builder<T> header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder<T> body(T body) {
            this.body = body;
            return this;
        }

        public ResponseEntity<T> build() {
            return new ResponseEntity<>(statusCode, headers, body);
        }
    }
}
