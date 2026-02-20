package org.moper.cap.property.result;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.exception.PropertyException;

public record PropertyOperationResult(
        @NotNull PropertyOperation operation,
        @NotNull Status status,
        @Nullable PropertyException exception,
        @NotNull String message
) {

    /**
     * 创建成功的操作结果
     *
     * @param operation 属性操作
     * @return 成功的操作结果
     */
    public static PropertyOperationResult success(PropertyOperation operation) {
        return new PropertyOperationResult(operation, Status.SUCCESS, null, "");
    }

    /**
     * 创建成功的操作结果（带消息）
     *
     * @param operation 属性操作
     * @param message 成功消息
     * @return 成功的操作结果
     */
    public static PropertyOperationResult success(PropertyOperation operation, String message) {
        return new PropertyOperationResult(operation, Status.SUCCESS, null, message);
    }

    /**
     * 创建发布者未找到的操作结果
     *
     * @param operation 属性操作
     * @param publisherName 未找到的发布者名称
     * @return 操作结果
     */
    public static PropertyOperationResult publisherNotFound(PropertyOperation operation, String publisherName) {
        return new PropertyOperationResult(operation, Status.PUBLISHER_NOT_FOUND, null,
                "Publisher not found: " + publisherName);
    }

    /**
     * 创建权限冲突的操作结果
     *
     * @param operation 属性操作
     * @param key 冲突的属性键
     * @param conflictingPublisher 冲突的发布者
     * @return 操作结果
     */
    public static PropertyOperationResult permissionConflict(PropertyOperation operation, String key, String conflictingPublisher) {
        return new PropertyOperationResult(operation, Status.PERMISSION_CONFLICT, null,
                "Property:" + key + " already exists and is published by another publisher: " + conflictingPublisher);
    }

    /**
     * 创建属性键未找到的操作结果
     *
     * @param operation 属性操作
     * @param key 未找到的属性键
     * @return 操作结果
     */
    public static PropertyOperationResult keyNotFound(PropertyOperation operation, String key) {
        return new PropertyOperationResult(operation, Status.KEY_NOT_FOUND, null,
                "Property:" + key + " does not exist");
    }

    /**
     * 创建版本冲突的操作结果
     *
     * @param operation 属性操作
     * @param expectedVersion 期望的版本号
     * @param actualVersion 实际的版本号
     * @return 操作结果
     */
    public static PropertyOperationResult versionConflict(PropertyOperation operation, int expectedVersion, int actualVersion) {
        return new PropertyOperationResult(operation, Status.VERSION_CONFLICT, null,
                "Version conflict: expected " + expectedVersion + ", but got " + actualVersion);
    }

    /**
     * 创建校验失败的操作结果
     *
     * @param operation 属性操作
     * @param validationMessage 校验失败的原因
     * @return 操作结果
     */
    public static PropertyOperationResult validationError(PropertyOperation operation, String validationMessage) {
        return new PropertyOperationResult(operation, Status.VALIDATION_ERROR, null,
                "Validation error: " + validationMessage);
    }

    /**
     * 创建未知错误的操作结果
     *
     * @param operation 属性操作
     * @param exception 异常信息
     * @return 操作结果
     */
    public static PropertyOperationResult unknownError(PropertyOperation operation, PropertyException exception) {
        return new PropertyOperationResult(operation, Status.UNKNOWN_ERROR, exception,
                "Unknown error: " + exception.getMessage());
    }

    /**
     * 创建未知错误的操作结果（不带异常）
     *
     * @param operation 属性操作
     * @param message 错误消息
     * @return 操作结果
     */
    public static PropertyOperationResult unknownError(PropertyOperation operation, String message) {
        return new PropertyOperationResult(operation, Status.UNKNOWN_ERROR, null,
                "Unknown error: " + message);
    }

    /**
     * 创建被跳过的操作结果
     *
     * @param operation 属性操作
     * @param reason 被跳过的原因
     * @return 操作结果
     */
    public static PropertyOperationResult skipped(PropertyOperation operation, String reason) {
        return new PropertyOperationResult(operation, Status.SKIPPED, null,
                "Operation skipped: " + reason);
    }

    @Getter
    public enum Status {

        /**
         * 操作成功
         */
        SUCCESS("操作成功"),

        /**
         * 发布者未找到（操作失败） </br>
         *
         * 在本系统中，通常表示操作涉及的属性发布者不存在或已离线，导致无法完成操作
         */
        PUBLISHER_NOT_FOUND("发布者未找到"),

        /**
         * 权限冲突（操作失败） </br>
         *
         * 在本系统中，通常表示已存在来自不同属性源的同名属性键，导致冲突
         */
        PERMISSION_CONFLICT("权限冲突"),

        /**
         * 属性键未找到（操作失败）
         */
        KEY_NOT_FOUND("属性键未找到"),

        /**
         * 版本冲突（操作失败）
         */
        VERSION_CONFLICT("版本冲突"),

        /**
         * 校验失败（操作失败）
         */
        VALIDATION_ERROR("验证错误"),

        /**
         * 发生了尚未定义的错误（操作失败）
         */
        UNKNOWN_ERROR("未知错误"),

        /**
         * 操作被跳过 </br>
         *
         * 非错误状态，表示由于某些条件未满足，操作未执行，但不视为失败
         */
        SKIPPED("操作被跳过");

        private final String description;

        Status(String description) {
            this.description = description;
        }

        public boolean isSuccess() {
            return this == SUCCESS;
        }

        public boolean isSkipped() {
            return this == SKIPPED;
        }

        public boolean isFailed(){
            return this == PERMISSION_CONFLICT
                    || this == KEY_NOT_FOUND
                    || this == VERSION_CONFLICT
                    || this == VALIDATION_ERROR
                    || this == UNKNOWN_ERROR;
        }
    }
}
