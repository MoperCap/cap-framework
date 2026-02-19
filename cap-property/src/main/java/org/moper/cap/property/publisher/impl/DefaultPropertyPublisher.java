package org.moper.cap.property.publisher.impl;

import lombok.Builder;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PublisherManifest;
import org.moper.cap.property.exception.PropertyException;
import org.moper.cap.property.exception.PropertyManifestVersionException;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.result.PublisherManifestResult;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 默认的属性发布者实现 </br>
 * 该实现使用内存数据结构来存储已发布的事件清单和已签约的Officer </br>
 * 满足线程安全，但不保证事件清单的持久化和跨实例共享
 */
@Builder
public final class DefaultPropertyPublisher implements PropertyPublisher {

    private final String name;

    @Builder.Default
    private final AtomicInteger version = new AtomicInteger(0);

    @Builder.Default
    private final Set<PropertyOfficer> officers = new CopyOnWriteArraySet<>();

    @Builder.Default
    private final Map<Integer, PublisherManifest> history = new ConcurrentHashMap<>();

    @Builder.Default
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Builder.Default
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * 获取当前发布者的名称
     *
     * @return 当前发布者的名称
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * 获取当前发布者的版本号
     *
     * @return 当前发布者的版本号
     */
    @Override
    public int currentVersion() {
        return version.get();
    }

    /**
     * 以同步的方式向所有已签约的Officer发布事件清单 </br>
     *
     * @param operations 发布的事件列表
     * @return 每个已签约的Officer针对该事件列表的处理结果。若publisher已关闭，则返回一个空列表
     */
    @Override
    public List<PublisherManifestResult> publish(PropertyOperation... operations) {
        // 关闭后静默失败
        if (closed.get()) return Collections.emptyList();

        PublisherManifest manifest = createAndSaveManifest(version.getAndIncrement(), operations);

        return officers.stream()
                .map(officer -> officer.receive(manifest))
                .collect(Collectors.toList());
    }

    /**
     * 以异步的方式向所有已签约的Officer发布事件清单 </br>
     *
     * @param operations 发布的事件列表
     * @return 每个已签约的Officer针对该事件列表的处理结果。若publisher已关闭，则返回一个完成的、包含空列表的CompletableFuture
     */
    @Override
    public List<CompletableFuture<PublisherManifestResult>> publishAsync(PropertyOperation... operations) {
        if(closed.get()) return Collections.emptyList();

        PublisherManifest manifest = createAndSaveManifest(version.getAndIncrement(), operations);

        return officers.stream()
                .map(officer -> officer.receiveAsync(manifest))
                .collect(Collectors.toList());
    }

    /**
     * 拉取指定版本号的事件清单 </br>
     *
     * @param versionID 版本号
     * @return 指定版本号的事件清单
     * @throws PropertyManifestVersionException 若版本号小于0或者版本号超出当前版本号范围，则抛出异常。同时，如果指定版本号的事件清单不存在，也抛出异常，不过该情况几乎不会发生
     */
    @Override
    public PublisherManifest pull(int versionID) throws PropertyManifestVersionException {
        if(versionID < 0)
            throw new PropertyManifestVersionException("Invalid version: versionID must be non-negative");
        else if (versionID >= currentVersion())
            throw new PropertyManifestVersionException("Invalid version: versionID must be less than Publisher current version");

        PublisherManifest result = history.get(versionID);
        if(result == null)
            throw new PropertyManifestVersionException("Manifest not found for versionID: " + versionID);
        return result;
    }

    /**
     * 拉取指定版本范围 [beginVersionID, endVersionID) 内的事件清单 </br>
     *
     * @param beginVersionID 起始版本号（包含）
     * @param endVersionID   结束版本号（不包含）
     * @return 指定版本范围的事件清单列表
     * @throws PropertyManifestVersionException 若beginVersionID小于0或者beginVersionID不小于endVersionID或者endVersionID超出当前版本号范围，则抛出异常。
     * 同时，如果指定版本范围内的事件清单不存在，也抛出异常，不过该情况几乎不会发生
     */
    @Override
    public List<PublisherManifest> pull(int beginVersionID, int endVersionID) throws PropertyManifestVersionException {
        if(beginVersionID < 0)
            throw new PropertyManifestVersionException("Invalid version range: beginVersionID must be non-negative");
        if(beginVersionID >= endVersionID)
            throw new PropertyManifestVersionException("Invalid version range: beginVersionID must be less than endVersionID");
        else if (endVersionID > currentVersion())
            throw new PropertyManifestVersionException("Invalid version range: endVersionID must be less than HistoryManifest Size");


        List<PublisherManifest> results = new ArrayList<>();
        for(int i = beginVersionID; i < endVersionID; i++){
            PublisherManifest manifest = history.get(i);
            if(manifest == null)
                throw new PropertyManifestVersionException("Manifest not found for versionID: " + i);
            results.add(manifest);
        }
        return results;
    }

    /**
     * 与Officer签约，允许该Officer接收本Publisher发布的事件
     *
     * @param officer 签约的Officer
     * @exception PropertyException 若当前publisher已关闭，则抛出异常。
     */
    @Override
    public void contract(PropertyOfficer officer) throws PropertyException {
        if(closed.get())
            throw new PropertyException("Cannot contract officer: Publisher is closed");

        officers.add(officer);
    }

    /**
     * 与Officer解约，禁止该Officer接收本Publisher发布的事件 </br>
     * 解约后，Publisher保证不再向该Officer发布事件，但不保证该Officer已接收的事件不会被处理
     *
     * @param officer 解约的Officer
     */
    @Override
    public void uncontract(PropertyOfficer officer) {
        if(closed.get()) return;

        officer.offPublisher(this);
        officers.remove(officer);
    }

    /**
     * 获取当前已签约的Officer数量
     *
     * @return 当前已签约的Officer数量
     */
    @Override
    public int getOfficerCount() {
        return officers.size();
    }

    /**
     * 获取当前已签约的Officer集合
     *
     * @return 当前已签约的Officer集合
     */
    @Override
    public Set<PropertyOfficer> getOfficers() {
        return Set.copyOf(officers);
    }

    /**
     * 判断Publisher是否已与指定Officer签约
     *
     * @param officer 指定Officer
     * @return 若已签约，则返回true；否则返回false
     */
    @Override
    public boolean isContractOfficer(PropertyOfficer officer) {
        return officers.contains(officer);
    }

    /**
     * 判断发布者是否已关闭 </br>
     *
     * @return 若发布者已关闭，则返回true；否则返回false
     */
    @Override
    public boolean isClosed() {
        return closed.get();
    }

    /**
     * 关闭属性发布者 </br>
     * 关闭后，Publisher保证不再发布事件，但不保证已发布的事件不会被处理。</br>
     * 同时，Publisher保证不再与新的Officer签约，但不保证已签约的Officer不会继续接收事件。
     */
    @Override
    public void close() {
        if(!closed.compareAndSet(false, true)) return;

        for(PropertyOfficer officer : officers) {
            officer.offPublisher(this);
        }

        executorService.shutdown();
        try{
            if(!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 根据属性操作事件列表创建事件清单并保存到历史记录中 </br>
     *
     * @param operations 属性操作事件列表
     * @return 创建的事件清单
     */
    private PublisherManifest createAndSaveManifest(int version, PropertyOperation... operations) {
        PublisherManifest manifest = new PublisherManifest(this, version, List.of(operations), Instant.now());
        history.put(version, manifest);
        return manifest;
    }
}
