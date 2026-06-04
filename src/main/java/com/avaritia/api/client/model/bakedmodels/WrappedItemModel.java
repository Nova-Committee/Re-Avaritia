package com.avaritia.api.client.model.bakedmodels;

import com.avaritia.api.client.model.ItemQuadBakery;
import com.avaritia.api.client.model.PerspectiveModelState;
import com.avaritia.client.model.loader.base.AvaritiaCustomItemModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/14 22:58
 * @Description:
 */
public abstract class WrappedItemModel extends AvaritiaCustomItemModel {

    protected final ItemModel wrapped;
    protected PerspectiveModelState parentState = PerspectiveModelState.IDENTITY;
    protected boolean cosmic = false;

    public WrappedItemModel(ItemModel wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }


    public static List<BakedQuad> bakeItem(final List<TextureAtlasSprite> sprites) {
        return ItemQuadBakery.bakeItem(sprites.toArray(TextureAtlasSprite[]::new));
    }

    @Override
    public void renderCustom(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack,
                             MultiBufferSource source, int packedLight, int packedOverlay) {
        renderItem(stack, ctx, poseStack, source, packedLight, packedOverlay);
    }

    public abstract void renderItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack,
                                    MultiBufferSource source, int packedLight, int packedOverlay);

    /**
     * Render the wrapped model.
     * <p>
     * The 1.26 item renderer is driven by {@link ItemModel#update}; subclasses that need to include the
     * vanilla/wrapped model should let {@link #update} delegate to {@link #wrapped} before adding custom layers.
     * This hook is intentionally empty to avoid depending on removed baked-model rendering APIs.
     *
     * @param stack         The stack.
     * @param pStack        The pose stack.
     * @param buffers       The {@link MultiBufferSource}.
     * @param packedLight   The packed light coords. See {@link Lightmap}.
     * @param packedOverlay The packed Overlay coords. See {@link OverlayTexture}.
     * @param fabulous      If fabulous is required.
     */
    protected void renderWrapped(ItemStack stack, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay, boolean fabulous) {
    }

    protected void renderWrapped(ItemStack stack, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay,
                                 boolean fabulous,
                                 java.util.function.Function<com.mojang.blaze3d.vertex.VertexConsumer, com.mojang.blaze3d.vertex.VertexConsumer> consOverride) {
        renderWrapped(stack, pStack, buffers, packedLight, packedOverlay, fabulous);
    }

    protected static void renderQuadLayer(PoseStack poseStack, VertexConsumer consumer, List<BakedQuad> quads, int packedLight, int packedOverlay) {
        PoseStack.Pose pose = poseStack.last();
        var instance = new QuadInstance();
        instance.getLightCoords(packedLight);
        instance.setOverlayCoords(packedOverlay);
        for (BakedQuad quad : quads) {
            consumer.putBakedQuad(pose, quad, instance);
        }
    }
}
