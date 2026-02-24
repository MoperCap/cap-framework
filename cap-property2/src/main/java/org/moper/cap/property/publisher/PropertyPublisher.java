package org.moper.cap.property.publisher;

import org.moper.cap.property.event.PropertyManifest;
import org.moper.cap.property.event.PropertyOperation;

import java.util.List;

public interface PropertyPublisher {

    String name();

    int currentVersion();

    void publish(PropertyOperation... operations);

    void publishAsync(PropertyOperation... operations);

    PropertyManifest pull(int versionID);

    List<PropertyManifest> pull(int beginVersionID, int endVersionID);
}
