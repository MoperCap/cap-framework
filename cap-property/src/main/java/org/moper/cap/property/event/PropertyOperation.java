package org.moper.cap.property.event;

public sealed interface PropertyOperation extends PropertyEvent permits PropertySetOperation, PropertyRemoveOperation{
}
