package com.avaritia.api.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.avaritia.api.client.model.PerspectiveModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public interface IItemRenderer extends PerspectiveModel {

    /**
     * Called to render your item with complete control. Bypasses all vanilla rendering of your model.
     *
     * @param stack         The {@link ItemStack} being rendered.
     * @param ctx           The {@link ItemDisplayContext} of where we are rendering.
     * @param mStack        The {@link PoseStack} to get / add transformations to.
     * @param source        The {@link MultiBufferSource} to retrieve buffers from.
     * @param packedLight   The packed light coords.
     * @param packedOverlay The {@link OverlayTexture} packed coords.
     */
    void renderItem(ItemStack stack, ItemDisplayContext ctx, PoseStack mStack, MultiBufferSource source, int packedLight, int packedOverlay);
}
