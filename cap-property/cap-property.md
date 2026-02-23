# cap-property 文档

### 1. 属性系统整体介绍

cap-property 提供**事件驱动、订阅可变、发布异步、实时回调**的属性中心，支持细粒度属性订阅与管理。

### 2. 核心接口

- **PropertyPublisher**  
  发布属性变更事件，签约 PropertyOfficer。
- **PropertyOfficer**  
  属性管理平台，负责聚合变更、决策推送订阅。
- **PropertySubscription**  
  客户端订阅器，管理 PropertySubscriber 列表。
- **PropertySubscriber**  
  单属性订阅者，实现 onSet/onRemoved 回调。
- **PropertyViewPool**  
  只读属性视图缓存。

### 3. 入门示例

```java
PropertyOfficer officer = DefaultPropertyOfficer.builder().name("main").build();
PropertyPublisher publisher = DefaultPropertyPublisher.builder().name("pub1").build();
publisher.contract(officer);

publisher.publish(
    new PropertySetOperation("featureX.enabled", true),
    new PropertySetOperation("cache.ttl", 30)
);

PropertySubscriber subscriber = new PropertySubscriber() {
    public String getPropertyKey() { return "featureX.enabled"; }
    public void onSet(Object v) { System.out.println("Feature enabled state: " + v); }
    public void onRemoved() { System.out.println("Feature config removed!"); }
};
PropertySubscription subscription = new DefaultPropertySubscription(
    "featureSub", Set.of(subscriber)
);
officer.subscribe(subscription);
```
### 4. 细粒度订阅与属性回收

- 支持属性移除/离线清理/动态订阅与解订阅，无需任何异常处理。

### 5. 进阶

- 支持 `PropertyViewPool` 自动同步属性变更，直接用作配置源只读接口。

### 6. FAQ

- 订阅/解订阅不抛异常？
  > 是。所有驱动为事件回调模型，任何中断都不会引发框架级异常。
