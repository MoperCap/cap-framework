package org.moper.cap.property.event;

import java.io.Serializable;

/**
 * 属性操作相关基接口
 */
public sealed interface PropertyOperation extends Serializable permits PropertySetOperation, PropertyRemoveOperation {
}
