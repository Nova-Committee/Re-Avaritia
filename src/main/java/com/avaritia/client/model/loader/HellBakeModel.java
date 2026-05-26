package com.avaritia.client.model.loader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.avaritia.api.client.model.bakedmodels.WrappedItemModel;
import com.avaritia.api.client.render.CosmicRenderCall;
import com.avaritia.api.client.render.CosmicRenderQueue;
import com.avaritia.api.client.util.TransformUtils;
import com.avaritia.api.iface.transform.CosmicRenderable;
import com.avaritia.api.iface.transform.IBowTransform;
import com.avaritia.api.iface.transform.IToolTransform;
import com.avaritia.client.AvaritiaClient;
import com.avaritia.client.compat.IrisCompat;
import com.avaritia.client.shader.AvaritiaRenderTypes;
import com.avaritia.client.shader.AvaritiaShaders;
import com.avaritia.init.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.avaritia.client.shader.AvaritiaShaders.COSMIC_UVS;

public class HellBakeModel extends WrappedItemModel implements CosmicRenderable {
    private final List<Identifier> maskSprite;

    public HellBakeModel(final ItemModel wrapped, final List<Identifier> maskSprite) {
        super(wrapped);
        this.maskSprite = maskSprite;
        this.cosmic = true;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source,
                           int packedLight, int packedOverlay) {
        if (stack.getItem() instanceof IToolTransform) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        } else if (stack.getItem() instanceof IBowTransform) {
            this.parentState = TransformUtils.DEFAULT_BOW;
        } else {
            this.parentState = TransformUtils.DEFAULT_ITEM;
        }
        this.renderWrapped(stack, pStack, source, packedLight, packedOverlay, true);
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
                            packedOverlay,
                            RenderSystem.getProjectionMatrix(),
                            RenderSystem.getModelViewMatrix()
                    )
            );
            return;
        }

        // 非 Iris 直接渲染
        renderCosmicLayer(stack, transformType, pStack, source, packedLight, packedOverlay);
    }

    @Override
    public void renderCosmicLayer(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source,
                                  int packedLight, int packedOverlay) {
        final Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0f;
        float pitch = 0.0f;
        float scale = 1f;
        if (AvaritiaClient.inventoryRender || transformType == ItemDisplayContext.GUI) {
            scale = 100.0F;
        } else {
            yaw = (float) (mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -(float) (mc.player.getXRot() * 2.0f * Math.PI / 360.0);
        }

        AvaritiaShaders.hellTime.set(mc.level.getGameTime() % Integer.MAX_VALUE);
        AvaritiaShaders.hellYaw.set(yaw);
        AvaritiaShaders.hellPitch.set(pitch);
        AvaritiaShaders.hellExternalScale.set(scale);

        if (stack.getItem() == ModItems.matter_cluster.get()) {
            AvaritiaShaders.hellOpacity.set(1.0F);
        } else {
            AvaritiaShaders.hellOpacity.set(1.0F);
        }

        if (AvaritiaShaders.hellUVs != null) {
            AvaritiaShaders.hellUVs.set(COSMIC_UVS);
        }

        final VertexConsumer cons = source.getBuffer(AvaritiaRenderTypes.HELL);
        List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
        for (Identifier res : maskSprite) {
            atlasSprite.add(mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }
        renderQuadLayer(pStack, cons, bakeItem(atlasSprite), packedLight, packedOverlay);
        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(AvaritiaRenderTypes.HELL);
        }
    }
}
