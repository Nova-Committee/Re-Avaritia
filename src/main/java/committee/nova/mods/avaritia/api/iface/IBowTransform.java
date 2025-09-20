package committee.nova.mods.avaritia.api.iface;

import committee.nova.mods.avaritia.api.client.model.PerspectiveModelState;
import committee.nova.mods.avaritia.api.client.util.TransformUtils;

public interface IBowTransform {
    default PerspectiveModelState getToolTransform() {
        return TransformUtils.DEFAULT_BOW;
    }
}
