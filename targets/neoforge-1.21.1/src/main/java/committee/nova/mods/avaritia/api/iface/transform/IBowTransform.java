package committee.nova.mods.avaritia.api.iface.transform;

import committee.nova.mods.avaritia.api.client.model.PerspectiveModelState;
import committee.nova.mods.avaritia.api.client.util.TransformUtils;

public interface IBowTransform {
    default PerspectiveModelState getBowTransform() {
        return TransformUtils.DEFAULT_BOW;
    }
}
