package org.moper.cap.property.event;

import org.moper.cap.core.event.Event;

public sealed interface PropertyEvent extends Event permits PropertyOperation, PropertyManifest {
}
