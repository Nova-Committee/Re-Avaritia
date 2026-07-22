package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.init.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class InfinityElytraLayer<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    private final ElytraModel adultModel;
    private final ElytraModel babyModel;
    private final EquipmentLayerRenderer equipmentRenderer;

    public InfinityElytraLayer(RenderLayerParent<S, M> renderer, EntityModelSet modelSet,
                               EquipmentLayerRenderer equipmentRenderer) {
        super(renderer);
        this.adultModel = new ElytraModel(modelSet.bakeLayer(ModelLayers.ELYTRA));
        this.babyModel = new ElytraModel(modelSet.bakeLayer(ModelLayers.ELYTRA_BABY));
        this.equipmentRenderer = equipmentRenderer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, S state,
                       float yRot, float xRot) {
        ItemStack stack = state.chestEquipment;
        if (!stack.is(ModItems.infinity_elytra.get())) {
            return;
        }

        ElytraModel model = state.isBaby ? this.babyModel : this.adultModel;
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        this.equipmentRenderer.renderLayers(
                EquipmentClientInfo.LayerType.WINGS,
                EquipmentAssets.ELYTRA,
                model,
                state,
                stack,
                poseStack,
                submitNodeCollector,
                packedLight,
                Res.INFINITY_ELYTRA,
                state.outlineColor,
                0
        );
        poseStack.popPose();
    }
}
