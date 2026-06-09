package committee.nova.mods.avaritia.api.iface.transform;


import committee.nova.mods.avaritia.api.client.model.PerspectiveModelState;
import committee.nova.mods.avaritia.api.client.util.TransformUtils;

public interface IToolTransform {

    default PerspectiveModelState getToolTransform() {
        return TransformUtils.DEFAULT_TOOL;
    }

}
