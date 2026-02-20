package org.moper.cap.property.result;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.property.event.PublisherManifest;
import org.moper.cap.property.officer.PropertyOfficer;

import java.time.Instant;
import java.util.List;

public record PublisherManifestResult(
        @NotNull PropertyOfficer officer,
        @NotNull PublisherManifest manifest,
        @NotEmpty List<PropertyOperationResult> operationResults,
        @NotNull Status status,
        @NotNull String description,
        @NotNull Instant timestamp
) {

    /**
     * 创建完全成功的清单结果
     *
     * @param officer 属性官
     * @param manifest 事件清单
     * @param operationResults 操作结果列表
     * @return 清单处理结果
     */
    public static PublisherManifestResult totalSuccess(PropertyOfficer officer, PublisherManifest manifest,
                                                       List<PropertyOperationResult> operationResults) {
        return new PublisherManifestResult(officer, manifest, operationResults, Status.TOTAL_SUCCESS, "", Instant.now());
    }

    /**
     * 创建完全成功的清单结果（带描述）
     *
     * @param officer 属性官
     * @param manifest 事件清单
     * @param operationResults 操作结果列表
     * @param description 描述信息
     * @return 清单处理结果
     */
    public static PublisherManifestResult totalSuccess(PropertyOfficer officer, PublisherManifest manifest,
                                                       List<PropertyOperationResult> operationResults, String description) {
        return new PublisherManifestResult(officer, manifest, operationResults, Status.TOTAL_SUCCESS, description, Instant.now());
    }

    /**
     * 创建部分成功的清单结果
     *
     * @param officer 属性官
     * @param manifest 事件清单
     * @param operationResults 操作结果列表
     * @return 清单处理结果
     */
    public static PublisherManifestResult partialSuccess(PropertyOfficer officer, PublisherManifest manifest,
                                                         List<PropertyOperationResult> operationResults) {
        return new PublisherManifestResult(officer, manifest, operationResults, Status.PARTIAL_SUCCESS, "", Instant.now());
    }

    /**
     * 创建部分成功的清单结果（带描述）
     *
     * @param officer 属性官
     * @param manifest 事件清单
     * @param operationResults 操作结果列表
     * @param description 描述信息
     * @return 清单处理结果
     */
    public static PublisherManifestResult partialSuccess(PropertyOfficer officer, PublisherManifest manifest,
                                                         List<PropertyOperationResult> operationResults, String description) {
        return new PublisherManifestResult(officer, manifest, operationResults, Status.PARTIAL_SUCCESS, description, Instant.now());
    }

    /**
     * 创建完全跳过的清单结果
     *
     * @param officer 属性官
     * @param manifest 事件清单
     * @param reason 跳过的原因
     * @return 清单处理结果
     */
    public static PublisherManifestResult skip(PropertyOfficer officer, PublisherManifest manifest, String reason) {
        return new PublisherManifestResult(officer, manifest, List.of(), Status.SKIP, reason, Instant.now());
    }

    /**
     * 创建完全失败的清单结果
     *
     * @param officer 属性官
     * @param manifest 事件清单
     * @param reason 失败的原因
     * @return 清单处理结果
     */
    public static PublisherManifestResult error(PropertyOfficer officer, PublisherManifest manifest, String reason) {
        return new PublisherManifestResult(officer, manifest, List.of(), Status.ERROR, reason, Instant.now());
    }

    /**
     * 创建发布者未找到的清单结果
     *
     * @param officer 属性官
     * @param manifest 事件清单
     * @param publisherName 未找到的发布者名称
     * @return 清单处理结果
     */
    public static PublisherManifestResult publisherNotFound(PropertyOfficer officer, PublisherManifest manifest, String publisherName) {
        return error(officer, manifest, "Publisher:" + publisherName + " not found");
    }

    /**
     * 创建版本冲突的清单结果
     *
     * @param officer 属性官
     * @param manifest 事件清单
     * @param expectedVersion 期望的版本号
     * @param actualVersion 实际接收到的版本号
     * @return 清单处理结果
     */
    public static PublisherManifestResult versionConflict(PropertyOfficer officer, PublisherManifest manifest,
                                                          int expectedVersion, int actualVersion) {
        return skip(officer, manifest,
                "Version conflict for publisher:" + manifest.publisher().name() +
                        ", expected version: " + expectedVersion + ", but got: " + actualVersion);
    }

    /**
     * 创建拉取失败的清单结果
     *
     * @param officer 属性官
     * @param manifest 事件清单
     * @param publisherName 发布者名称
     * @param beginVersion 起始版本号
     * @param endVersion 结束版本号
     * @return 清单处理结果
     */
    public static PublisherManifestResult pullFailed(PropertyOfficer officer, PublisherManifest manifest,
                                                     String publisherName, int beginVersion, int endVersion) {
        return error(officer, manifest,
                "Failed to pull manifests for publisher:" + publisherName +
                        ", version range: [" + beginVersion + ", " + endVersion + "]");
    }

    /**
     * 创建拉取结果为空的清单结果
     *
     * @param officer 属性官
     * @param manifest 事件清单
     * @param publisherName 发布者名称
     * @param beginVersion 起始版本号
     * @param endVersion 结束版本号
     * @return 清单处理结果
     */
    public static PublisherManifestResult pullEmpty(PropertyOfficer officer, PublisherManifest manifest,
                                                    String publisherName, int beginVersion, int endVersion) {
        return error(officer, manifest,
                "Failed to pull manifests for publisher:" + publisherName +
                        ", version range: [" + beginVersion + ", " + endVersion + "], error: no manifests returned");
    }

    public enum Status {

        /**
         * 完全成功 </br>
         *
         * 事件清单本身没有问题且其中的所有操作都执行成功 </br>
         */
        TOTAL_SUCCESS,

        /**
         * 部分成功 </br>
         *
         * 事件清单本身没有问题，但其中的某些操作执行失败了 </br>
         * 注意：这里所说的操作指的是PropertyOperation，而不是事件清单本身。 </br>
         * 同时，不保证属性操作结果列表results中一定存在成功的操作结果
         */
        PARTIAL_SUCCESS,

        /**
         * 完全跳过 </br>
         *
         * 事件清单本身存在问题，导致事件操作无法得到执行，但事件清单的处理结果被认为是成功的 </br>
         * 比如说事件清单的版本过旧，理应不该得到执行
         */
        SKIP,

        /**
         * 完全失败 </br>
         *
         * 事件清单本身存在问题，导致事件操作无法得到执行
         */
        ERROR,
    }

}
