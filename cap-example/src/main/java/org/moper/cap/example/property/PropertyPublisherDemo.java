package org.moper.cap.example.property;

import org.moper.cap.context.environment.Environment;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.impl.DefaultPropertyPublisher;
import org.moper.cap.property.event.PropertySetOperation;

public class PropertyPublisherDemo {
    public static void publishDemo(Environment environment) {
        DefaultPropertyPublisher publisher = DefaultPropertyPublisher.builder().name("demo").build();
        publisher.contract(environment.getOfficer());
        environment.registerPublisher(publisher);
        publisher.publish(new PropertySetOperation("welcome", "你好！"));
    }
}