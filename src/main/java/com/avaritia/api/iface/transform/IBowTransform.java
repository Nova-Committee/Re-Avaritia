package com.avaritia.api.iface.transform;

import com.avaritia.api.client.model.PerspectiveModelState;
import com.avaritia.api.client.util.TransformUtils;

public interface IBowTransform {
    default PerspectiveModelState getBowTransform() {
        return TransformUtils.DEFAULT_BOW;
    }
}
