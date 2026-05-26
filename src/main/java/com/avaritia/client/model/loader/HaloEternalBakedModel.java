package com.avaritia.client.model.loader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.avaritia.api.client.model.bakedmodels.WrappedItemModel;
import com.avaritia.api.client.render.CosmicRenderCall;
import com.avaritia.api.client.render.CosmicRenderQueue;
import com.avaritia.api.client.render.buffer.AlphaOverrideVertexConsumer;
import com.avaritia.api.client.util.TransformUtils;
import com.avaritia.api.iface.transform.CosmicRenderable;
import com.avaritia.api.iface.transform.IBowTransform;
import com.avaritia.api.iface.transform.IToolTransform;
import com.avaritia.client.AvaritiaClient;
import com.avaritia.client.compat.IrisCompat;
import com.avaritia.client.model.loader.base.HaloSetting;
import com.avaritia.client.model.loader.base.HaloUtils;
import com.avaritia.client.shader.AvaritiaRenderTypes;
import com.avaritia.client.shader.AvaritiaShaders;
import com.avaritia.init.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.avaritia.client.shader.AvaritiaShaders.ETERNAL_UVS;


public class HaloEternalBakedModel extends WrappedItemModel implements CosmicRenderable {
    private final Random random;
    private final BakedQuad haloQuad;
    private final HaloSetting setting;
    private final List<Identifier> maskSprite;

    public HaloEternalBakedModel(ItemModel wrapped, TextureAtlasSprite sprite, HaloSetting setting, List<Identifier> maskSprite) {
        super(wrapped);
        this.random = new Random();
        this.haloQuad = HaloUtils.generateHaloQuad(sprite, setting.size(), setting.color());
        this.setting = setting;
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

        // 渲染Halo效果
        if (transformType == ItemDisplayContext.GUI) {
            renderQuadLayer(pStack, source.getBuffer(RenderType.translucent()), List.of(this.haloQuad), packedLight, packedOverlay);
            if (this.setting.pulse()) {
                pStack.pushPose();
                double scale = random.nextDouble() * 0.15D + 0.95D;
                double trans = (1.0D - scale) / 2.0D;
                pStack.translate(trans, trans, 0.0D);
                pStack.scale((float) scale, (float) scale, 1.0001F);
                this.renderWrapped(stack, pStack, source, packedLight, packedOverlay, true, (e) -> new AlphaOverrideVertexConsumer(e, 0.6000000238418579D));
                pStack.popPose();
            }
        }

        // 渲染基础模型
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

        AvaritiaShaders.eternalTime.set(mc.level.getGameTime() % Integer.MAX_VALUE);
        AvaritiaShaders.eternalYaw.set(yaw);
        AvaritiaShaders.eternalPitch.set(pitch);
        AvaritiaShaders.eternalExternalScale.set(scale);

        if (stack.getItem() == ModItems.matter_cluster.get()) {
            AvaritiaShaders.eternalOpacity.set(1.0F);
        } else {
            AvaritiaShaders.eternalOpacity.set(1.5F);
        }

        if (AvaritiaShaders.eternalUVs != null) {
            AvaritiaShaders.eternalUVs.set(ETERNAL_UVS);
        }

        final VertexConsumer cons = source.getBuffer(AvaritiaRenderTypes.ETERNAL);
        List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
        for (Identifier res : maskSprite) {
            atlasSprite.add(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }
        renderQuadLayer(pStack, cons, bakeItem(atlasSprite), packedLight, packedOverlay);
    }
}
