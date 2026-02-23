package org.moper.cap.environment;

import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.officer.impl.DefaultPropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.subscriber.PropertyViewPool;
import org.moper.cap.property.subscriber.view.DefaultPropertyViewPool;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class DefaultEnvironment implements Environment {

    private final PropertyOfficer officer;
    private final PropertyViewPool viewPool;
    private final Set<PropertyPublisher> publishers = new CopyOnWriteArraySet<>();

    public DefaultEnvironment(String name) {
        this.officer = DefaultPropertyOfficer.builder().name(name + "-officer").build();
        this.viewPool = new DefaultPropertyViewPool(name + "-viewPool");
        this.officer.subscribe(viewPool);
    }

    @Override
    public PropertyOfficer getOfficer() { return officer; }

    @Override
    public PropertyViewPool getViewPool() { return viewPool; }

    @Override
    public void registerPublisher(PropertyPublisher publisher) {
        publishers.add(publisher);

    }

    @Override
    public void unregisterPublisher(PropertyPublisher publisher) {
        publisher.uncontract(officer);
        publishers.remove(publisher);
    }

    @Override
    public void close() {
        for (PropertyPublisher publisher : publishers) {
            publisher.uncontract(officer);
            publisher.close();
        }
        publishers.clear();
        officer.close();
    }
}
