package org.moper.cap.property.officer.impl;

import lombok.Builder;
import org.moper.cap.property.PropertyDefinition;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.event.PublisherManifest;
import org.moper.cap.property.exception.PropertyManifestVersionException;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.result.PropertyOperationResult;
import org.moper.cap.property.result.PublisherManifestResult;
import org.moper.cap.property.subscriber.PropertySelector;
import org.moper.cap.property.subscriber.PropertySubscription;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 默认的属性官管理平台实现 </br>
 *
 * 该实现提供了基本的属性官管理平台功能，包括接收事件清单、处理属性发布者离线事件、
 * 订阅和取消订阅属性更新事件等。满足线程安全的要求，适用于多线程环境下的属性管理平台 </br>
 *
 * 核心特性：
 * <ul>
 *   <li><strong>线程安全</strong>：使用 ConcurrentHashMap、CopyOnWriteArraySet 等并发数据结构</li>
 *   <li><strong>版本管理</strong>：追踪每个 Publisher 的版本号，支持版本冲突检测和事件拉取恢复</li>
 *   <li><strong>订阅者缓存</strong>：维护每个订阅者感兴趣的属性键缓存，避免重复的选择器匹配</li>
 *   <li><strong>异步处理</strong>：支持异步接收事件清单，不阻塞异步线程</li>
 *   <li><strong>初始化通知</strong>：订阅时自动向订阅者发送当前系统中所有其感兴趣的属性</li>
 * </ul>
 *
 * 关闭行为说明：
 * <ul>
 *   <li>receive() - 返回"Officer已关闭"的失败结果</li>
 *   <li>receiveAsync() - 返回已完成的失败 Future</li>
 *   <li>subsribe() - 忽略请求，不做任何处理</li>
 *   <li>unsubscribe() - 忽略请求，不做任何处理</li>
 *   <li>onPublisherOffline() - 忽略请求，不做任何处理</li>
 * </ul>
 *
 * 事务性说明： </br>
 * 该版本的Officer在接收事件清单时，并不保证单一属性清单的事务性。即在处理事件清单时，
 * 如果其中的某些操作失败了，并不会回滚已经成功的操作。同时，执行成功的所有属性操作会被
 * officer通知给所有的订阅者进行相应的更新，而不会因为某些操作失败了就放弃通知。 </br>
 */
@Builder
public final class DefaultPropertyOfficer implements PropertyOfficer {

    private final String name;

    @Builder.Default
    private final AtomicInteger version = new AtomicInteger(0);

    /**
     * 发布者版本映射表 </br>
     * 键：PropertyPublisher </br>
     * 值：该发布者的下一个期望版本号 </br>
     */
    @Builder.Default
    private final Map<PropertyPublisher, Integer> publishers = new ConcurrentHashMap<>();

    /**
     * 属性核心池 </br>
     * 键：属性键（String） </br>
     * 值：属性定义（PropertyDefinition） </br>
     */
    @Builder.Default
    private final Map<String, PropertyDefinition> core = new ConcurrentHashMap<>();

    /**
     * 订阅者缓存映射 </br>
     * 键：PropertySubscription（订阅者客户端）</br>
     * 值：该订阅者感兴趣的属性键集合（缓存）</br>
     *
     * 该缓存用于在通知订阅者时，快速过滤出该订阅者关心的属性操作
     */
    @Builder.Default
    private final Map<PropertySubscription, Set<String>> subscriptions = new ConcurrentHashMap<>();

    /**
     * 异步事件处理线程池
     */
    @Builder.Default
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Builder.Default
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * 获取当前属性管理平台的名称
     *
     * @return 当前属性管理平台的名称
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * 获取当前属性管理平台的版本号
     *
     * @return 当前属性管理平台的版本号
     */
    @Override
    public int currentVersion() {
        return version.get();
    }

    /**
     * 以同步的方式接收Publisher发布的事件清单。</br>
     * 属性管理平台将根据事件清单更新内部状态，并通知相关Subscriber进行相应的更新。
     *
     * <p>版本处理逻辑：</p>
     * <ul>
     *   <li>若发布者不在已知发布者列表中，返回错误结果</li>
     *   <li>若事件清单版本号过旧（小于期望版本号），返回跳过结果</li>
     *   <li>若事件清单版本号符合期望，进行更新并返回处理结果</li>
     *   <li>若事件清单版本号超过期望，拉取版本范围内的所有清单并进行批量处理</li>
     * </ul>
     *
     * @param manifest 事件清单
     * @return 关于事件清单的处理结果。若 Officer 已关闭，返回失败结果。
     */
    @Override
    public PublisherManifestResult receive(PublisherManifest manifest) {
        if(isClosed()) return PublisherManifestResult.error(this, manifest, "Officer is closed");

        PropertyPublisher publisher = manifest.publisher();

        // 发布者不存在检查
        if (!publishers.containsKey(publisher)) {
            return PublisherManifestResult.publisherNotFound(this, manifest, publisher.name());
        }

        int expectedVersion = publishers.get(publisher);
        int actualVersion = manifest.version();

        // 版本过旧则跳过
        if (expectedVersion > actualVersion) {
            return PublisherManifestResult.versionConflict(this, manifest, expectedVersion, actualVersion);
        }

        // 版本符合则直接处理
        if (expectedVersion == actualVersion) {
            return processSingleManifestAndNotify(manifest);
        }

        // 版本超前则拉取并处理多个清单
        return processVersionGapAndNotify(manifest, publisher, expectedVersion, actualVersion);
    }

    /**
     * 以异步的方式接收Publisher发布的事件清单。</br>
     * 属性管理平台将根据事件清单更新内部状态，并通知相关Subscriber进行相应的更新。</br>
     *
     * 该方法有专门的异步实现，不会阻塞异步线程：
     * <ul>
     *   <li>快速路径（发布者不存在或版本过旧）直接返回已完成的 CompletableFuture</li>
     *   <li>处理路径在线程池中异步执行，不占用调用线程</li>
     * </ul>
     *
     * @param manifest 事件清单
     * @return 关于事件清单的处理结果的异步包装。 若 Officer 已关闭，返回已完成的失败 Future。
     */
    @Override
    public CompletableFuture<PublisherManifestResult> receiveAsync(PublisherManifest manifest) {
        if(isClosed())
            return CompletableFuture.completedFuture(
                    PublisherManifestResult.error(this, manifest, "Officer is closed")
            );

        PropertyPublisher publisher = manifest.publisher();

        // 快速检查：发布者不存在，立即返回错误结果
        if (!publishers.containsKey(publisher)) {
            return CompletableFuture.completedFuture(
                    PublisherManifestResult.publisherNotFound(this, manifest, publisher.name())
            );
        }

        int expectedVersion = publishers.get(publisher);
        int actualVersion = manifest.version();

        // 快速检查：版本过旧，立即返回跳过结果
        if (expectedVersion > actualVersion) {
            return CompletableFuture.completedFuture(
                    PublisherManifestResult.versionConflict(this, manifest, expectedVersion, actualVersion)
            );
        }

        // 异步处理：版本符合则异步处理单个清单
        if (expectedVersion == actualVersion) {
            return CompletableFuture.supplyAsync(
                    () -> processSingleManifestAndNotify(manifest),
                    executorService
            );
        }

        // 异步处理：版本超前则异步拉取并处理多个清单
        return CompletableFuture.supplyAsync(
                () -> processVersionGapAndNotify(manifest, publisher, expectedVersion, actualVersion),
                executorService
        );
    }

    /**
     * 当属性发布者不在线时，属性管理平台将接收到通知，并进行相应的处理。</br>
     *
     * 处理流程：
     * <ol>
     *   <li>增加平台版本号</li>
     *   <li>移除该发布者发布的所有属性</li>
     *   <li>从已知发布者列表中删除该发布者</li>
     *   <li>通知所有订阅者客户端关于移除的属性</li>
     * </ol>
     *
     * 若 Officer 已关闭，忽略该请求。</br>
     *
     * @param publisher 被销毁的属性发布者
     */
    @Override
    public void offPublisher(PropertyPublisher publisher) {
        if(isClosed()) return;

        version.incrementAndGet();

        // 收集该发布者发布的所有属性键
        Set<String> removedKeys = core.entrySet().stream()
                .filter(entry -> entry.getValue().publisher().equals(publisher))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // 从属性池中移除这些属性
        removedKeys.forEach(core::remove);

        // 从发布者映射中移除该发布者
        publishers.remove(publisher);

        // 通知订阅者
        notifySubscriptionsOnRemoval(removedKeys);
    }

    /**
     * 订阅属性更新事件。</br>
     *
     * 当属性管理平台接收到新的事件清单并更新状态后，将通知所有相关的订阅者进行相应的处理。
     * 同时，Officer将自动对该订阅者进行一次初始化通知，发送所有该订阅者感兴趣的现有属性。</br>
     *
     * 初始化流程：
     * <ol>
     *   <li>初始化该订阅者的属性键缓存</li>
     *   <li>扫描属性池，收集订阅者感兴趣的属性</li>
     *   <li>生成初始化通知，仅发送存在的属性（PropertySetOperation）</li>
     * </ol>
     *
     * 若 Officer 已关闭，忽略该请求。</br>
     *
     * @param subscription 订阅者客户端
     */
    @Override
    public void subscribe(PropertySubscription subscription) {
        if (isClosed()) return;

        PropertySelector selector = subscription.selector();

        // 初始化该订阅者的缓存集合
        Set<String> interestedKeys = new CopyOnWriteArraySet<>();
        subscriptions.put(subscription, interestedKeys);

        // 收集所有该订阅者感兴趣的现有属性
        List<PropertyOperation> initOperations = new ArrayList<>();
        for (Map.Entry<String, PropertyDefinition> entry : core.entrySet()) {
            String key = entry.getKey();
            PropertyDefinition definition = entry.getValue();

            if (selector.matches(key)) {
                interestedKeys.add(key);
                initOperations.add(new PropertySetOperation(key, definition.value()));
            }
        }

        // 仅当存在感兴趣的属性时才进行初始化通知
        if (!initOperations.isEmpty()) {
            subscription.dispatch(initOperations.toArray(new PropertyOperation[0]));
        }
    }

    /**
     * 取消订阅属性更新事件。</br>
     *
     * 取消订阅后，属性管理平台将不再通知该订阅者相关属性的更新事件。</br>
     *
     * 若 Officer 已关闭，忽略该请求。</br>
     *
     * @param subscription 取消订阅的订阅者客户端
     */
    @Override
    public void unsubscribe(PropertySubscription subscription) {
        if (isClosed()) return;
        subscriptions.remove(subscription);
    }

    /**
     * 判断当前属性管理平台是否已关闭。
     *
     * @return 如果订阅客户端已关闭则返回 true，否则返回 false
     */
    @Override
    public boolean isClosed() {
        return closed.get();
    }

    /**
     * 关闭属性管理平台。</br>
     *
     * 关闭后，Officer将不再接收新的事件清单和订阅请求。该方法会优雅地关闭内部线程池。
     */
    @Override
    public void close() {
        if(!closed.compareAndSet(false, true)) {
            return;
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理单一事件清单并通知订阅者。</br>
     *
     * 处理流程：
     * <ol>
     *   <li>更新发布者版本号</li>
     *   <li>增加平台版本号</li>
     *   <li>逐个处理事件清单中的所有操作</li>
     *   <li>收集成功的操作</li>
     *   <li>通知订阅者成功的操作</li>
     * </ol>
     *
     * @param manifest 事件清单
     * @return 处理结果
     */
    private PublisherManifestResult processSingleManifestAndNotify(PublisherManifest manifest) {
        PropertyPublisher publisher = manifest.publisher();
        int manifestVersion = manifest.version();

        // 更新发布者的下一个期望版本号
        publishers.put(publisher, manifestVersion + 1);
        version.incrementAndGet();

        // 处理所有操作
        List<PropertyOperationResult> operationResults = new ArrayList<>();
        List<PropertyOperation> successfulOperations = new ArrayList<>();

        for (PropertyOperation operation : manifest.operations()) {
            PropertyOperationResult result = processOperation(publisher, operation);
            operationResults.add(result);

            if (result.status().isSuccess()) {
                successfulOperations.add(operation);
            }
        }

        // 通知订阅者成功的操作
        if (!successfulOperations.isEmpty()) {
            notifySubscriptions(successfulOperations);
        }

        // 判断整体结果
        boolean allSuccess = operationResults.stream()
                .allMatch(result -> result.status().isSuccess());

        return allSuccess ?
                PublisherManifestResult.totalSuccess(this, manifest, operationResults) :
                PublisherManifestResult.partialSuccess(this, manifest, operationResults);
    }

    /**
     * 处理版本间隙（版本号超前的情况）。</br>
     *
     * 当接收到的事件清单版本号超过期望版本号时，需要从发布者拉取版本范围内的所有清单，
     * 然后批量处理这些清单中的所有操作。</br>
     *
     * 处理流程：
     * <ol>
     *   <li>拉取版本范围 [expectedVersion, actualVersion) 内的所有清单</li>
     *   <li>若拉取失败或结果为空，返回错误结果</li>
     *   <li>批量处理多个清单中的所有操作</li>
     *   <li>通知订阅者成功的操作</li>
     * </ol>
     *
     * @param originalManifest 原始接收到的事件清单
     * @param publisher 发布者
     * @param expectedVersion 期望的版本号
     * @param actualVersion 实际接收到的版本号
     * @return 处理结果
     */
    private PublisherManifestResult processVersionGapAndNotify(PublisherManifest originalManifest,
                                                               PropertyPublisher publisher,
                                                               int expectedVersion,
                                                               int actualVersion) {
        // 拉取版本范围内的所有清单
        List<PublisherManifest> manifests = pullManifests(publisher, expectedVersion, actualVersion);

        // 检查拉取结果
        if (manifests == null) {
            return PublisherManifestResult.pullFailed(this, originalManifest, publisher.name(), expectedVersion, actualVersion);
        }
        if (manifests.isEmpty()) {
            return PublisherManifestResult.pullEmpty(this, originalManifest, publisher.name(), expectedVersion, actualVersion);
        }

        // 处理多个清单
        return processMultiManifestAndNotify(manifests);
    }

    /**
     * 处理多个事件清单并通知订阅者。</br>
     *
     * 该方法用于处理从发布者拉取的多个事件清单。这些清单来自同一发布者，需要按顺序处理。</br>
     *
     * 处理流程：
     * <ol>
     *   <li>更新发布者的期望版本号（最后一个清单的下一个版本）</li>
     *   <li>增加平台版本号</li>
     *   <li>逐个清单、逐个操作地进行处理</li>
     *   <li>收集成功的操作</li>
     *   <li>通知订阅者成功的操作</li>
     * </ol>
     *
     * @param manifests 多个事件清单列表
     * @return 处理结果（manifest 字段为列表中的第一个清单）
     */
    private PublisherManifestResult processMultiManifestAndNotify(List<PublisherManifest> manifests) {
        PropertyPublisher publisher = manifests.get(0).publisher();
        PublisherManifest firstManifest = manifests.get(0);
        PublisherManifest lastManifest = manifests.get(manifests.size() - 1);

        // 更新发布者的下一个期望版本号
        publishers.put(publisher, lastManifest.version() + 1);
        version.incrementAndGet();

        // 处理所有清单中的所有操作
        List<PropertyOperationResult> operationResults = new ArrayList<>();
        List<PropertyOperation> successfulOperations = new ArrayList<>();

        for (PublisherManifest manifest : manifests) {
            for (PropertyOperation operation : manifest.operations()) {
                PropertyOperationResult result = processOperation(publisher, operation);
                operationResults.add(result);

                if (result.status().isSuccess()) {
                    successfulOperations.add(operation);
                }
            }
        }

        // 通知订阅者成功的操作
        if (!successfulOperations.isEmpty()) {
            notifySubscriptions(successfulOperations);
        }

        // 判断整体结果
        boolean allSuccess = operationResults.stream()
                .allMatch(result -> result.status().isSuccess());

        return allSuccess ?
                PublisherManifestResult.totalSuccess(this, firstManifest, operationResults) :
                PublisherManifestResult.partialSuccess(this, firstManifest, operationResults);
    }

    /**
     * 处理单个属性操作。</br>
     *
     * 根据操作类型调用相应的处理方法。</br>
     *
     * @param publisher 发布者
     * @param operation 属性操作
     * @return 操作结果
     */
    private PropertyOperationResult processOperation(PropertyPublisher publisher, PropertyOperation operation) {
        if (operation instanceof PropertySetOperation setOp) {
            return processSetOperation(publisher, setOp);
        } else if (operation instanceof PropertyRemoveOperation removeOp) {
            return processRemoveOperation(publisher, removeOp);
        } else {
            return PropertyOperationResult.unknownError(operation,
                    "Unsupported operation type: " + operation.getClass().getName());
        }
    }

    /**
     * 处理属性设置操作。</br>
     *
     * 设置或更新属性值。如果属性已存在但来自不同的发布者，则返回权限冲突错误。</br>
     *
     * 处理流程：
     * <ol>
     *   <li>检查属性是否已存在且来自不同发布者（权限冲突）</li>
     *   <li>创建或更新属性定义</li>
     *   <li>更新订阅者缓存</li>
     * </ol>
     *
     * @param publisher 发布者
     * @param operation 设置操作
     * @return 操作结果
     */
    private PropertyOperationResult processSetOperation(PropertyPublisher publisher, PropertySetOperation operation) {
        String key = operation.key();
        Object value = operation.value();
        PropertyDefinition existingDefinition = core.get(key);

        // 权限冲突检查：属性已存在但来自不同发布者
        if (existingDefinition != null && !existingDefinition.publisher().equals(publisher)) {
            return PropertyOperationResult.permissionConflict(operation, key, existingDefinition.publisher().name());
        }

        // 创建或更新属性
        PropertyDefinition newDefinition = new PropertyDefinition(key, value, publisher, Instant.now());
        core.put(key, newDefinition);
        updateSubscriptionCache(key);

        return PropertyOperationResult.success(operation);
    }

    /**
     * 处理属性移除操作。</br>
     *
     * 移除属性。如果属性不存在或来自不同的发布者，则返回相应的错误。</br>
     *
     * 处理流程：
     * <ol>
     *   <li>检查属性是否存在</li>
     *   <li>检查发布者是否与属性的发布者一致</li>
     *   <li>从属性池中移除属性</li>
     *   <li>更新订阅者缓存</li>
     * </ol>
     *
     * @param publisher 发布者
     * @param operation 移除操作
     * @return 操作结果
     */
    private PropertyOperationResult processRemoveOperation(PropertyPublisher publisher, PropertyRemoveOperation operation) {
        String key = operation.key();
        PropertyDefinition definition = core.get(key);

        // 属性不存在检查
        if (definition == null) {
            return PropertyOperationResult.keyNotFound(operation, key);
        }

        // 权限检查：发布者必须与属性的发布者一致
        if (!definition.publisher().equals(publisher)) {
            return PropertyOperationResult.permissionConflict(operation, key, definition.publisher().name());
        }

        // 移除属性
        core.remove(key);
        removeFromSubscriptionCache(key);

        return PropertyOperationResult.success(operation);
    }

    /**
     * 通知所有订阅者指定的属性操作。</br>
     *
     * 该方法遍历所有订阅者，根据其缓存的属性键过滤操作，并仅通知该订阅者关心的操作。</br>
     *
     * @param operations 成功的属性操作列表
     */
    private void notifySubscriptions(List<PropertyOperation> operations) {
        for (Map.Entry<PropertySubscription, Set<String>> entry : subscriptions.entrySet()) {
            PropertySubscription subscription = entry.getKey();
            Set<String> cachedKeys = entry.getValue();

            // 过滤出该订阅者关心的操作
            List<PropertyOperation> filteredOperations = operations.stream()
                    .filter(operation -> matchesOperation(operation, cachedKeys))
                    .toList();

            // 仅当存在匹配的操作时才通知
            if (!filteredOperations.isEmpty()) {
                subscription.dispatch(filteredOperations.toArray(new PropertyOperation[0]));
            }
        }
    }

    /**
     * 通知订阅者属性被移除。</br>
     *
     * 该方法在发布者离线时调用，通知订阅者其感兴趣的属性被移除。
     * 同时会更新订阅者的属性键缓存。</br>
     *
     * @param removedKeys 被移除的属性键集合
     */
    private void notifySubscriptionsOnRemoval(Set<String> removedKeys) {
        for (Map.Entry<PropertySubscription, Set<String>> entry : subscriptions.entrySet()) {
            PropertySubscription subscription = entry.getKey();
            Set<String> cachedKeys = entry.getValue();

            // 获取该订阅者感兴趣的、已被移除的属性
            List<PropertyOperation> removalOperations = removedKeys.stream()
                    .filter(cachedKeys::contains)
                    .peek(cachedKeys::remove)  // 从缓存中移除
                    .map(PropertyRemoveOperation::new)
                    .collect(Collectors.toList());

            if (!removalOperations.isEmpty()) {
                subscription.dispatch(removalOperations.toArray(new PropertyOperation[0]));
            }
        }
    }

    /**
     * 更新所有订阅者的缓存。</br>
     *
     * 当新属性被添加或更新时，检查所有订阅者是否关心该属性，
     * 如果关心则将其添加到缓存中。</br>
     *
     * @param key 属性键
     */
    private void updateSubscriptionCache(String key) {
        for (Map.Entry<PropertySubscription, Set<String>> entry : subscriptions.entrySet()) {
            PropertySubscription subscription = entry.getKey();
            Set<String> cachedKeys = entry.getValue();

            if (subscription.selector().matches(key)) {
                cachedKeys.add(key);
            }
        }
    }

    /**
     * 从所有订阅者的缓存中移除指定属性。</br>
     *
     * 当属性被移除时，需要从所有订阅者的缓存中删除该属性键。</br>
     *
     * @param key 属性键
     */
    private void removeFromSubscriptionCache(String key) {
        subscriptions.values().forEach(cachedKeys -> cachedKeys.remove(key));
    }

    /**
     * 判断属性操作是否与缓存的属性键匹配。</br>
     *
     * 该方法用于快速判断某个操作是否与订阅者的缓存属性键匹配，
     * 避免了调用 selector.matches() 的开销。</br>
     *
     * @param operation 属性操作
     * @param cachedKeys 缓存的属性键集合
     * @return 是否匹配
     */
    private boolean matchesOperation(PropertyOperation operation, Set<String> cachedKeys) {
        if (operation instanceof PropertySetOperation setOp) {
            return cachedKeys.contains(setOp.key());
        } else if (operation instanceof PropertyRemoveOperation removeOp) {
            return cachedKeys.contains(removeOp.key());
        }
        return false;
    }

    /**
     * 拉取指定版本范围的事件清单。</br>
     *
     * 该方法从发布者拉取指定版本范围 [beginVersion, endVersion) 内的所有清单。
     * 如果拉取过程中发生异常，则返回 null。</br>
     *
     * @param publisher 发布者
     * @param beginVersion 起始版本号（包含）
     * @param endVersion 结束版本号（不包含）
     * @return 拉取到的事件清单列表，如果拉取失败则返回 null
     */
    private List<PublisherManifest> pullManifests(PropertyPublisher publisher, int beginVersion, int endVersion) {
        try {
            return publisher.pull(beginVersion, endVersion);
        } catch (PropertyManifestVersionException e) {
            return null;
        }
    }
}