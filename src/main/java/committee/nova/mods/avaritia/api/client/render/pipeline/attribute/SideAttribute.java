package committee.nova.mods.avaritia.api.client.render.pipeline.attribute;

import committee.nova.mods.avaritia.api.client.render.CCRenderState;
import committee.nova.mods.avaritia.api.client.render.pipeline.VertexAttribute;
import committee.nova.mods.avaritia.api.client.util.VectorUtils;

/**
 * Sets the side state in CCRS based on the provided model. If the model does not have side data it requires normals.
 */
public class SideAttribute extends VertexAttribute<int[]> {

    public static final AttributeKey<int[]> attributeKey = AttributeKey.create("side", int[]::new);

    private int[] sideRef;

    public SideAttribute() {
        super(attributeKey);
    }

    @Override
    public boolean load(CCRenderState ccrs) {
        sideRef = ccrs.model.getAttribute(attributeKey);
        if (ccrs.model.hasAttribute(attributeKey)) {
            return sideRef != null;
        }

        ccrs.pipeline.addDependency(ccrs.normalAttrib);
        return true;
    }

    @Override
    public void operate(CCRenderState ccrs) {
        if (sideRef != null) {
            ccrs.side = sideRef[ccrs.vertexIndex];
        } else {
            ccrs.side = VectorUtils.findSide(ccrs.normal);
        }
    }
}
