package com.avaritia.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.avaritia.Avaritia;
import com.avaritia.client.model.entity.InfinityShieldModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InfinityShieldRender {

    /** 模型层位置 — 注册层定义将在 AvaritiaModClient 中关联到此常量。 */
    public static final ModelLayerLocation INFINITY_SHIELD = new ModelLayerLocation(Identifier.of(Avaritia.MOD_ID, "infinity_shield"), "main");

    private final InfinityShieldModel model;

    public InfinityShieldRender(EntityModelSet entityModelSet) {
        this.model = new InfinityShieldModel(entityModelSet.bakeLayer(INFINITY_SHIELD));
    }

    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(Identifier.of(Avaritia.MOD_ID, "textures/item/tools/infinity_shield/layer_0.png")));
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
