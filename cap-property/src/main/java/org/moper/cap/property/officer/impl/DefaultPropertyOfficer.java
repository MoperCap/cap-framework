package org.moper.cap.property.officer.impl;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.property.PropertyDefinition;
import org.moper.cap.property.event.PropertyManifest;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.exception.PropertyConflictException;
import org.moper.cap.property.exception.PropertyNotFoundException;
import org.moper.cap.property.exception.PropertyValidationException;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.publisher.impl.DefaultPropertyPublisher;
import org.moper.cap.property.resolver.PropertyResolver;
import org.moper.cap.property.resolver.impl.DefaultPropertyResolver;
import org.moper.cap.property.subscriber.PropertySubscriber;
import org.moper.cap.property.subscriber.PropertySubscription;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Slf4j
public final class DefaultPropertyOfficer implements PropertyOfficer {

    private final static int DEFAULT_THREAD_POOL_SIZE = 4;

    private final String name;

    private final Map<String, PropertyDefinition> core = new ConcurrentHashMap<>();

    private final Map<String, PropertyPublisher> publishers = new ConcurrentHashMap<>();

    private final Map<String, PropertySubscription> subscriptions = new ConcurrentHashMap<>();

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final ExecutorService executorService;

    private final PropertyResolver resolver = new DefaultPropertyResolver();

    public DefaultPropertyOfficer(String name) {
        this(name, DEFAULT_THREAD_POOL_SIZE);
    }

    public DefaultPropertyOfficer(String name, int threadPoolSize) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("PropertyOfficer name cannot be null or blank");
        }

        if (threadPoolSize <= 0) {
            throw new IllegalArgumentException("Thread pool size must be greater than 0");
        }
        this.name = name;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * 获取属性管理平台名称
     */
    @Override
    public String name() {
        return name;
    }

    @Override
    public void receive(PropertyManifest manifest) {
        checkClosed();
        checkManifest(manifest);

        String operator = manifest.operator();
        log.info("PropertyOfficer Received property manifest, operator: {}, operations: {}", operator, manifest.operations().size());

        processAllPropertyOperations(operator, manifest.operations());
        log.info("Property manifest from [{}] processed, total subscriptions: {}", operator, subscriptions.size());
    }

    @Override
    public void receiveAsync(PropertyManifest manifest) {
        checkClosed();
        checkManifest(manifest);

        String operator = manifest.operator();
        log.info("PropertyOfficer Received property manifest, operator: {}, operations: {}", operator, manifest.operations().size());

        executorService.submit(() -> {
            try {
                processAllPropertyOperations(operator, manifest.operations());
                log.info("Property manifest from [{}] processed asynchronously, total subscriptions: {}", operator, subscriptions.size());
            } catch (Exception e) {
                log.error("Failed to process property manifest from [{}] asynchronously", operator, e);
            }
        });
    }

    @Override
    public Object getRawPropertyValue(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Property key cannot be null or blank");
        }

        PropertyDefinition def = core.get(key);
        if (def == null) return null;
        else return def.value();
    }

    @Override
    public <T> T getPropertyValue(String key, Class<T> type) {
        Object value = getRawPropertyValue(key);
        return resolver.resolve(value, type);
    }

    @Override
    public <T> T getPropertyValueOrDefault(String key, Class<T> type, T defaultValue) {
        if (defaultValue == null) {
            throw new IllegalArgumentException("Default value cannot be null");
        }

        Object value = getRawPropertyValue(key);
        if (value == null) return defaultValue;
        else return resolver.resolve(value, type);
    }

    @Override
    public <T> Optional<T> getPropertyValueOptional(String key, Class<T> type) {
        Object value = getRawPropertyValue(key);
        if(value == null) return Optional.empty();
        else return Optional.of(resolver.resolve(value, type));
    }

    @Override
    public boolean containsProperty(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Property key cannot be null or blank");
        }

        return core.containsKey(key);
    }

    @Override
    public Set<String> getAllPropertyKeys() {
        return Set.copyOf(core.keySet());
    }

    @Override
    public PropertyPublisher getPublisher(String name) {
        return getPublisher(name, () -> {
            PropertyPublisher publisher = new DefaultPropertyPublisher(name, this);
            publishers.put(name, publisher);
            return publisher;
        });
    }

    @Override
    public PropertyPublisher getPublisher(String name, Supplier<PropertyPublisher> supplier) {
        checkClosed();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Publisher name cannot be null or blank");
        }

        if(supplier == null) {
            throw new IllegalArgumentException("Publisher supplier cannot be null");
        }

        if (publishers.containsKey(name)) {
            return publishers.get(name);
        }
        PropertyPublisher publisher = supplier.get();
        if (publisher == null) {
            throw new IllegalArgumentException("Publisher supplier cannot return null");
        }
        publishers.put(name, publisher);
        return publisher;
    }

    @Override
    public boolean containsPublisher(String name) {
        checkClosed();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Publisher name cannot be null or blank");
        }

        return publishers.containsKey(name);
    }

    @Override
    public void destroyPublisher(String name) {
        checkClosed();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Publisher name cannot be null or blank");
        }

        if (!publishers.containsKey(name)) return;

        PropertyPublisher publisher = publishers.remove(name);
        try {
            publisher.close();
        } catch (Exception e) {
            log.warn("Failed to close publisher {}", publisher.name(), e);
        }
    }

    @Override
    public Collection<PropertyPublisher> getAllPublishers() {
        checkClosed();

        return Set.copyOf(publishers.values());
    }

    @Override
    public PropertySubscription getSubscription(String name) {
        checkClosed();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Subscription name cannot be null or blank");
        }

        return subscriptions.get(name);
    }

    @Override
    public PropertySubscription getSubscription(String name, Supplier<PropertySubscription> supplier) {
        checkClosed();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Subscription name cannot be null or blank");
        }

        if(supplier == null) {
            throw new IllegalArgumentException("Subscription supplier cannot be null");
        }

        if(subscriptions.containsKey(name)) {
            return subscriptions.get(name);
        }

        PropertySubscription subscription = supplier.get();
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription supplier cannot return null");
        }

        subscriptions.put(name, subscription);

        return subscription;
    }

    @Override
    public boolean containsSubscription(String name) {
        checkClosed();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Subscription name cannot be null or blank");
        }
        return subscriptions.containsKey(name);
    }

    @Override
    public void destroySubscription(String name) {
        checkClosed();

        if (name == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }

        if (!subscriptions.containsKey(name)) return;

        PropertySubscription subscription = subscriptions.remove(name);
        try {
            subscription.close();
        } catch (Exception ex) {
            log.warn("Failed to close subscription {}", name, ex);
        }

    }

    @Override
    public Collection<PropertySubscription> getAllSubscriptions() {
        checkClosed();

        return Set.copyOf(subscriptions.values());
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() throws Exception {
        if(!closed.compareAndSet(false, true)) {
            return;
        }

        for(PropertyPublisher publisher : publishers.values()) {
            try {
                publisher.close();
            } catch (Exception e) {
                log.warn("Failed to close publisher {}", publisher.name(), e);
            }
        }

        for(PropertySubscription subscription : subscriptions.values()) {
            try {
                subscription.close();
            } catch (Exception e) {
                log.warn("Failed to close subscription {}", subscription, e);
            }
        }

        executorService.shutdown();

        log.info("PropertyOfficer closed");
    }

    private void checkClosed() {
        if (isClosed()) {
            throw new IllegalStateException("PropertyOfficer is closed");
        }
    }

    private void checkManifest(PropertyManifest manifest) {
        if (manifest == null) {
            throw new IllegalArgumentException("Property manifest cannot be null");
        }

        if (!publishers.containsKey(manifest.operator())) {
            throw new PropertyValidationException("Publisher {} does not exist", manifest.operator());
        }
    }

    private void processAllPropertyOperations(String operator, List<PropertyOperation> operations) {
        for (PropertyOperation operation : operations) {
            switch (operation) {
                case PropertySetOperation(String propertyKey, Object newValue) -> processPropertySetOperation(operator, propertyKey, newValue);
                case PropertyRemoveOperation(String propertyKey) -> processPropertyRemoveOperation(operator, propertyKey);
            }
        }
    }

    private void processPropertySetOperation(String operator, String propertyKey, Object newValue) {
        PropertyDefinition oldDef = core.get(propertyKey);
        if(oldDef != null && !oldDef.publisher().equals(operator)){
            log.warn("Property [{}] already exists and owned by [{}], forbidden to update by [{}]", propertyKey, oldDef.publisher(), operator);
            throw new PropertyConflictException("Property key " + propertyKey + " already exists and is owned by publisher " + oldDef.publisher());
        }

        PropertyDefinition newDef = oldDef == null ? PropertyDefinition.of(propertyKey, newValue, operator) : oldDef.withValue(newValue);
        core.put(propertyKey, newDef);
        log.info("Property [{}] set by [{}], newValue: {}", propertyKey, operator, newValue);
        notifyAllSubscriberSetOperation(propertyKey, newValue);
    }

    private void processPropertyRemoveOperation(String operator, String propertyKey) {
        PropertyDefinition oldDef = core.get(propertyKey);
        if (oldDef == null) {
            log.warn("Property [{}] does not exist, cannot be removed by [{}]", propertyKey, operator);
            throw new PropertyNotFoundException("Property key " + propertyKey + " does not exist");
        } else if (!oldDef.publisher().equals(operator)) {
            log.warn("Property [{}] already exists and owned by [{}], forbidden to remove by [{}]", propertyKey, oldDef.publisher(), operator);
            throw new PropertyConflictException("Property key " + propertyKey + " already exists and is owned by publisher " + oldDef.publisher());
        } else {
            core.remove(propertyKey);
            log.info("Property [{}] removed by [{}]", propertyKey, operator);
            notifyAllSubscriberRemoveOperation(propertyKey);
        }
    }

    private void notifyAllSubscriberSetOperation(String propertyKey, Object newValue) {
        for (PropertySubscription subscription : subscriptions.values()) {
            for (@SuppressWarnings("rawtypes") PropertySubscriber subscriber : subscription) {
                if (!subscriber.selector().matches(propertyKey)) continue;
                subscriber.onSet(resolver.resolve(newValue, subscriber.getSubscribeType()));
                log.debug("Subscriber [{}] notified of property [{}] set", subscriber, propertyKey);
            }
        }
    }

    private void notifyAllSubscriberRemoveOperation(String propertyKey) {
        for (PropertySubscription subscription : subscriptions.values()) {
            for (@SuppressWarnings("rawtypes") PropertySubscriber subscriber : subscription) {
                if (!subscriber.selector().matches(propertyKey)) continue;
                subscriber.onRemoved();
                log.debug("Subscriber [{}] notified of property [{}] remove", subscriber, propertyKey);
            }
        }
    }

}
