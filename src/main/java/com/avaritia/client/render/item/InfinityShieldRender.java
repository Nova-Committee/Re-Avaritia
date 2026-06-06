package com.avaritia.client.render.item;

import com.avaritia.Const;
import com.avaritia.client.model.entity.InfinityShieldModel;

import com.mojang.serialization.MapCodec;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import org.joml.Vector3fc;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class InfinityShieldRender implements NoDataSpecialModelRenderer {

    /** 模型层位置 — 注册层定义将在 AvaritiaModClient 中关联到此常量。 */
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Const.MOD_ID, "textures/item/tools/infinity_shield/layer_0.png");
    public static final ModelLayerLocation INFINITY_SHIELD = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Const.MOD_ID, "infinity_shield"), "main");

    private final InfinityShieldModel model;

    public InfinityShieldRender(EntityModelSet entityModelSet) {
        this.model = new InfinityShieldModel(entityModelSet.bakeLayer(INFINITY_SHIELD));
    }

    @Override
    public void submit(@NotNull PoseStack poseStack, @NotNull SubmitNodeCollector output, int packedLight, int packedOverlay, boolean hasFoilType, int outlineColor) {
        output.submitCustomGeometry(poseStack, this.model.renderType(TEXTURE), (pose, vertexConsumer) -> {
            PoseStack modelPose = new PoseStack();
            modelPose.last().set(pose);
            modelPose.scale(1.0F, -1.0F, -1.0F);
            this.model.renderToBuffer(modelPose, vertexConsumer, packedLight, packedOverlay);
        });
    }

    @Override
    public void getExtents(@NotNull Consumer<Vector3fc> output) {
    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<InfinityShieldRender.Unbaked> MAP_CODEC = MapCodec.unit(new InfinityShieldRender.Unbaked());

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Void>> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new InfinityShieldRender(context.entityModelSet());
        }
    }
}
