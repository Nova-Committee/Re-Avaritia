package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.client.model.entity.InfinityArmorModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class InfinityArmorRender<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
    private static final RenderType WING_RENDER_TYPE = RenderTypes.armorCutoutNoCull(Res.WING_TEX);

    private final InfinityArmorModel<S> model;

    public InfinityArmorRender(RenderLayerParent<S, M> renderer, EntityModelSet modelSet, boolean isSlim) {
        super(renderer);
        this.model = new InfinityArmorModel<>(modelSet.bakeLayer(ModelLayers.PLAYER), isSlim);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, S state, float yRot, float xRot) {
        this.model.setupAnim(state);
        poseStack.pushPose();
        this.model.render(state, poseStack, submitNodeCollector, WING_RENDER_TYPE, packedLight, LivingEntityRenderer.getOverlayCoords(state, 0.0F));
        poseStack.popPose();
    }
}
