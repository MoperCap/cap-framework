package org.moper.cap.property.officer.impl;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.property.PropertyDefinition;
import org.moper.cap.property.event.PropertyManifest;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.exception.PropertyConflictException;
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
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Slf4j
public final class DefaultPropertyOfficer implements PropertyOfficer {

    private final static int DEFAULT_THREAD_POOL_SIZE = 4;

    private final Map<String, PropertyDefinition> core = new ConcurrentHashMap<>();

    private final Map<String, PropertyPublisher> publishers = new ConcurrentHashMap<>();

    private final Set<PropertySubscription> subscriptions = new CopyOnWriteArraySet<>();

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final ExecutorService executorService;

    private final PropertyResolver resolver = new DefaultPropertyResolver();

    public DefaultPropertyOfficer() {
        this.executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
    }

    public DefaultPropertyOfficer(int threadPoolSize) {
        if (threadPoolSize <= 0) {
            throw new IllegalArgumentException("Thread pool size must be greater than 0");
        }
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    @Override
    public void receive(PropertyManifest manifest) {
        checkClosed();
        checkManifest(manifest);

        String operator = manifest.operator();
        log.info("PropertyOfficer Received property manifest, operator: {}, operations: {}", operator, manifest.operations().size());

        for (PropertyOperation operation : manifest.operations()) {
            switch (operation) {
                case PropertySetOperation(String propertyKey, Object newValue) -> {
                    PropertyDefinition oldDef = core.get(propertyKey);
                    if (oldDef != null && !oldDef.publisher().equals(operator)) {
                        log.warn("Property [{}] already exists and owned by [{}], forbidden to update by [{}]", propertyKey, oldDef.publisher(), operator);
                        throw new PropertyConflictException("Property key " + propertyKey + " already exists and is owned by publisher " + oldDef.publisher());
                    }
                    PropertyDefinition newDef = oldDef.withValue(newValue);
                    core.put(propertyKey, newDef);
                    log.info("Property [{}] set/updated by [{}], newValue: {}", propertyKey, operator, newValue);

                    for (PropertySubscription subscription : subscriptions) {
                        for (@SuppressWarnings("rawtypes") PropertySubscriber subscriber : subscription) {
                            if (!subscriber.selector().matches(propertyKey)) continue;
                            subscriber.onSet(resolver.resolve(newValue, subscriber.getSubscribeType()));
                            log.debug("Subscriber [{}] notified of property [{}] set", subscriber, propertyKey);
                        }
                    }
                }

                case PropertyRemoveOperation(String propertyKey) -> {
                    PropertyDefinition oldDef = core.get(propertyKey);
                    if (oldDef != null && !oldDef.publisher().equals(operator)) {
                        log.warn("Property [{}] already exists and owned by [{}], forbidden to remove by [{}]", propertyKey, oldDef.publisher(), operator);
                        throw new PropertyConflictException("Property key " + propertyKey + " already exists and is owned by publisher " + oldDef.publisher());
                    }
                    core.remove(propertyKey);
                    log.info("Property [{}] removed by [{}]", propertyKey, operator);
                    for (PropertySubscription subscription : subscriptions) {
                        for (@SuppressWarnings("rawtypes") PropertySubscriber subscriber : subscription) {
                            if (!subscriber.selector().matches(propertyKey)) continue;
                            subscriber.onRemoved();
                            log.debug("Subscriber [{}] notified of property [{}] remove", subscriber, propertyKey);
                        }
                    }
                }

            }
        }
        log.info("Property manifest from [{}] processed, total subscriptions: {}", operator, subscriptions.size());
    }

    @Override
    public void receiveAsync(PropertyManifest manifest) {
        executorService.submit(() -> {
            receive(manifest);
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
        return Optional.of(resolver.resolve(value, type));
    }

    @Override
    public boolean containsProperty(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Property key cannot be null or blank");
        }

        return core.containsKey(key);
    }

    @Override
    public Set<String> getAllPropertyKey() {
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
    public PropertySubscription createSubscription(Supplier<PropertySubscription> supplier) {
        checkClosed();

        PropertySubscription subscription = supplier.get();
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription supplier cannot return null");
        }
        if (subscriptions.add(subscription)) return subscription;
        else throw new IllegalArgumentException("Subscription already exists");
    }

    @Override
    public boolean containsSubscription(PropertySubscription subscription) {
        checkClosed();

        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }
        return subscriptions.contains(subscription);
    }

    @Override
    public void destroySubscription(PropertySubscription subscription) {
        checkClosed();

        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }

        if (!subscriptions.contains(subscription)) return;

        subscriptions.remove(subscription);
        try {
            subscription.close();
        } catch (Exception ex) {
            log.warn("Failed to close subscription {}", subscription, ex);
        }

    }

    @Override
    public Collection<PropertySubscription> getAllSubscriptions() {
        checkClosed();

        return Set.copyOf(subscriptions);
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
        publishers.clear();

        for(PropertySubscription subscription : subscriptions) {
            try {
                subscription.close();
            } catch (Exception e) {
                log.warn("Failed to close subscription {}", subscription, e);
            }
        }
        subscriptions.clear();

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
}
