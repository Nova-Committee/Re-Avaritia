package com.avaritia.api.iface.transform;


import com.avaritia.api.client.model.PerspectiveModelState;
import com.avaritia.api.client.util.TransformUtils;

public interface IToolTransform {

    default PerspectiveModelState getToolTransform() {
        return TransformUtils.DEFAULT_TOOL;
    }

}
