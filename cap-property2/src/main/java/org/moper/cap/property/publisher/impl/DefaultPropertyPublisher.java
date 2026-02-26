package org.moper.cap.property.publisher.impl;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.property.event.PropertyManifest;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.exception.PropertyException;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class DefaultPropertyPublisher implements PropertyPublisher {

    private final String name;

    private final PropertyOfficer officer;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DefaultPropertyPublisher(String name, PropertyOfficer officer) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("PropertyPublisher name cannot be null or empty");
        }

        if(officer == null) {
            throw new IllegalArgumentException("PropertyPublisher officer cannot be null");
        }

        this.name = name;
        this.officer = officer;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void publish(PropertyOperation... operations) {
        if(isClosed()) {
            throw new PropertyException("Publisher is closed");
        }

        for(PropertyOperation operation : operations) {
            if(operation == null) {
                throw new IllegalArgumentException("PropertyOperation cannot be null");
            }
        }

        PropertyManifest manifest = PropertyManifest.of(name(), List.of(operations));

        officer.receive(manifest);
    }

    @Override
    public void publishAsync(PropertyOperation... operations) {
        if(isClosed()) {
            throw new PropertyException("Publisher is closed");
        }

        for(PropertyOperation operation : operations) {
            if(operation == null) {
                throw new IllegalArgumentException("PropertyOperation cannot be null");
            }
        }

        PropertyManifest manifest = PropertyManifest.of(name(), List.of(operations));

        officer.receiveAsync(manifest);
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() throws Exception {
        if(!closed.compareAndSet(false, true)){
            return;
        }
        log.info("PropertyPublisher {} closed", name());
    }
}
