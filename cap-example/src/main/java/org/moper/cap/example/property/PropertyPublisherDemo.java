package org.moper.cap.example.property;

import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.event.PropertySetOperation;

public class PropertyPublisherDemo {
    public static void publishDemo(PropertyOfficer officer) {

        PropertyPublisher publisher = officer.getPublisher("demo");
        publisher.publish(new PropertySetOperation("welcome", "你好！"));
    }
}