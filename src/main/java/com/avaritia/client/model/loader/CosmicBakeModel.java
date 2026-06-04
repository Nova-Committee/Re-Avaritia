package com.avaritia.client.model.loader;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.avaritia.api.client.render.CosmicRenderCall;
import com.avaritia.api.client.render.CosmicRenderQueue;
import com.avaritia.api.iface.transform.CosmicRenderable;
import com.avaritia.client.AvaritiaClient;
import com.avaritia.client.compat.IrisCompat;
import com.avaritia.api.client.model.bakedmodels.WrappedItemModel;
import com.avaritia.api.client.util.TextureUtils;
import com.avaritia.api.client.util.TransformUtils;
import com.avaritia.api.iface.transform.IBowTransform;
import com.avaritia.api.iface.transform.IToolTransform;
import com.avaritia.client.shader.AvaritiaRenderTypes;
import com.avaritia.client.shader.AvaritiaShaders;
import com.avaritia.init.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.avaritia.client.shader.AvaritiaShaders.COSMIC_UVS;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/14 22:58
 * @Description:
 */
public class CosmicBakeModel extends WrappedItemModel implements CosmicRenderable {
    private final List<Identifier> maskSprite;

    public CosmicBakeModel(final ItemModel wrapped, final List<Identifier> maskSprite) {
        super(wrapped);
        this.maskSprite = maskSprite;
        this.cosmic = true;
    }

    @Override
    public void renderItem(
            ItemStack stack,
            ItemDisplayContext transformType,
            PoseStack pStack,
            MultiBufferSource source,
            int packedLight,
            int packedOverlay
    ) {

        if (stack.getItem() instanceof IToolTransform) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        } else if (stack.getItem() instanceof IBowTransform) {
            this.parentState = TransformUtils.DEFAULT_BOW;
        } else {
            this.parentState = TransformUtils.DEFAULT_ITEM;
        }

        // 普通模型
        this.renderWrapped(
                stack,
                pStack,
                source,
                packedLight,
                packedOverlay,
                true
        );

        // flush vanilla layer
        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }

        // Iris 延迟渲染
        if (IrisCompat.shouldDefer(transformType)) {

            CosmicRenderQueue.enqueue(
                    new CosmicRenderCall(
                            this,
                            stack,
                            transformType,
                            pStack,
                            packedLight,
                            packedOverlay
                    )
            );

            return;
        }

        // 非 Iris 直接渲染
        renderCosmicLayer(
                stack,
                transformType,
                pStack,
                source,
                packedLight,
                packedOverlay
        );
    }

    @Override
    public void renderCosmicLayer(
            ItemStack stack,
            ItemDisplayContext transformType,
            PoseStack pStack,
            MultiBufferSource source,
            int packedLight,
            int packedOverlay
    ) {

        final Minecraft mc = Minecraft.getInstance();

        float yaw = 0.0f;
        float pitch = 0.0f;
        float scale = 1f;

        if (AvaritiaClient.inventoryRender
                || transformType == ItemDisplayContext.GUI) {

            scale = 100.0F;

        } else {

            yaw = (float) (mc.player.getYRot()
                    * 2.0f * Math.PI / 360.0);

            pitch = -(float) (mc.player.getXRot()
                    * 2.0f * Math.PI / 360.0);
        }

        AvaritiaShaders.cosmicTime.set(
                mc.level.getGameTime() % Integer.MAX_VALUE
        );

        AvaritiaShaders.cosmicYaw.set(yaw);

        AvaritiaShaders.cosmicPitch.set(pitch);

        AvaritiaShaders.cosmicExternalScale.set(scale);

        if (stack.getItem() == ModItems.matter_cluster.get()) {

            AvaritiaShaders.cosmicOpacity.set(1.0F);

        } else {

            AvaritiaShaders.cosmicOpacity.set(1.0F);
        }

        if (AvaritiaShaders.cosmicUVs != null) {
            AvaritiaShaders.cosmicUVs.set(COSMIC_UVS);
        }

        final VertexConsumer cons =
                source.getBuffer(AvaritiaRenderTypes.COSMIC);

        List<TextureAtlasSprite> atlasSprite = new ArrayList<>();

        for (Identifier res : maskSprite) {

            atlasSprite.add(TextureUtils.getTexture(res));
        }

        renderQuadLayer(pStack, cons, bakeItem(atlasSprite), packedLight, packedOverlay);

        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(AvaritiaRenderTypes.COSMIC);
        }
    }
}
