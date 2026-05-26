package com.avaritia.api.client.model.bakedmodels;

import com.avaritia.api.client.model.PerspectiveModelState;
import com.avaritia.client.model.loader.base.AvaritiaCustomItemModel;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/14 22:58
 * @Description:
 */
public abstract class WrappedItemModel extends AvaritiaCustomItemModel {

    public static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    public static final FaceBakery FACE_BAKERY = new FaceBakery();
    protected final ItemModel wrapped;
    protected PerspectiveModelState parentState = PerspectiveModelState.IDENTITY;
    protected boolean cosmic = false;

    public WrappedItemModel(ItemModel wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }


    public static List<BakedQuad> bakeItem(final List<TextureAtlasSprite> sprites) {
        final LinkedList<BakedQuad> quads = new LinkedList<>();
        for (final TextureAtlasSprite sprite : sprites) {
            final List<CuboidModelElement> unbaked = ITEM_MODEL_GENERATOR.processFrames(sprites.indexOf(sprite), "layer" + sprites.indexOf(sprite), sprite.contents());
            for (final CuboidModelElement element : unbaked) {
                for (final Map.Entry<Direction, CuboidFace> entry : element.faces.entrySet()) {
                    quads.add(FACE_BAKERY.bakeQuad(element.from, element.to, entry.getValue(), sprite, entry.getKey(), new PerspectiveModelState(ImmutableMap.of()), element.rotation, element.shade));
                }
            }
        }
        return quads;
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
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 1.0F, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay, true);
        }
    }
}
