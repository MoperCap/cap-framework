package org.moper.cap.property.event;

import java.io.Serializable;

public sealed interface PropertyOperation extends Serializable permits PropertySetOperation, PropertyRemoveOperation {
}
