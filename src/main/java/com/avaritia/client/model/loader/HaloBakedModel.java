package com.avaritia.client.model.loader;

import com.mojang.blaze3d.vertex.PoseStack;
import com.avaritia.api.client.model.bakedmodels.WrappedItemModel;
import com.avaritia.api.client.render.buffer.AlphaOverrideVertexConsumer;
import com.avaritia.api.client.util.TransformUtils;
import com.avaritia.api.iface.transform.IToolTransform;
import com.avaritia.client.model.loader.base.HaloSetting;
import com.avaritia.client.model.loader.base.HaloUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * Name: Avaritia-forge / HaloBakedModel
 * Author: cnlimiter
 * CreateTime: 2023/9/24 22:35
 * Description:
 */

public class HaloBakedModel extends WrappedItemModel {
    private final Random random;
    private final BakedQuad haloQuad;
    private final HaloSetting setting;

    public HaloBakedModel(ItemModel wrapped, TextureAtlasSprite sprite, HaloSetting setting) {
        super(wrapped);
        this.random = new Random();
        this.haloQuad = HaloUtils.generateHaloQuad(sprite, setting.size(), setting.color());
        this.setting = setting;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack pPoseStack, MultiBufferSource bufferSource,
                           int packedLight, int packedOverlay) {
        if (stack.getItem() instanceof IToolTransform) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        } else {
            this.parentState = TransformUtils.DEFAULT_ITEM;
        }
        if (itemDisplayContext == ItemDisplayContext.GUI) {

            renderQuadLayer(pPoseStack, bufferSource.getBuffer(RenderType.translucent()), List.of(this.haloQuad), packedLight, packedOverlay);
            if (this.setting.pulse()) {
                pPoseStack.pushPose();
                double scale = random.nextDouble() * 0.15D + 0.95D;
                double trans = (1.0D - scale) / 2.0D;
                pPoseStack.translate(trans, trans, 0.0D);
                pPoseStack.scale((float) scale, (float) scale, 1.0001F);
                this.renderWrapped(stack, pPoseStack, bufferSource, packedLight, packedOverlay, true, (e) -> new AlphaOverrideVertexConsumer(e, 0.6000000238418579D));
                pPoseStack.popPose();
            }

        }
        this.renderWrapped(stack, pPoseStack, bufferSource, packedLight, packedOverlay, true);
    }
}
