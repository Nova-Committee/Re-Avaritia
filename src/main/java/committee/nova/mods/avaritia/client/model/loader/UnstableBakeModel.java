package committee.nova.mods.avaritia.client.model.loader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.avaritia.api.client.render.CosmicRenderCall;
import committee.nova.mods.avaritia.api.client.render.CosmicRenderQueue;
import committee.nova.mods.avaritia.api.client.util.TransformUtils;
import committee.nova.mods.avaritia.api.iface.transform.CosmicRenderable;
import committee.nova.mods.avaritia.api.iface.transform.IBowTransform;
import committee.nova.mods.avaritia.api.iface.transform.IToolTransform;
import committee.nova.mods.avaritia.client.AvaritiaForgeClient;
import committee.nova.mods.avaritia.client.compat.IrisCompat;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.common.item.resources.MatterClusterItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static committee.nova.mods.avaritia.client.shader.AvaritiaShaders.COSMIC_UVS;
import static committee.nova.mods.avaritia.client.shader.AvaritiaShaders.ETERNAL_UVS;


public class UnstableBakeModel extends WrappedItemModel implements CosmicRenderable {
    private final List<ResourceLocation> maskSprite;

    public UnstableBakeModel(final BakedModel wrapped, final List<ResourceLocation> maskSprite) {
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
        if (AvaritiaForgeClient.inventoryRender || transformType == ItemDisplayContext.GUI) {
            scale = 100.0F;
        } else {
            yaw = (float) (mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -(float) (mc.player.getXRot() * 2.0f * Math.PI / 360.0);
        }


        AvaritiaShaders.unstableTime.set(mc.level.getGameTime() % Integer.MAX_VALUE);
        AvaritiaShaders.unstableYaw.set(yaw);
        AvaritiaShaders.unstablePitch.set(pitch);
        AvaritiaShaders.unstableExternalScale.set(scale);

        if (stack.getItem() == ModItems.matter_cluster.get()) {
            AvaritiaShaders.unstableOpacity.set(MatterClusterItem.getClusterSize(MatterClusterItem.getClusterItems(stack)) / (float) MatterClusterItem.CAPACITY);
        } else {
            AvaritiaShaders.unstableOpacity.set(1.5F);
        }

        if (AvaritiaShaders.unstableUVs != null) {
            AvaritiaShaders.unstableUVs.set(ETERNAL_UVS);
        }

        final VertexConsumer cons = source.getBuffer(AvaritiaRenderTypes.UNSTABLE);
        List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
        for (ResourceLocation res : maskSprite) {
            atlasSprite.add(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }
        mc.getItemRenderer().renderQuadList(pStack, cons, bakeItem(atlasSprite), stack, packedLight, packedOverlay);
        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(AvaritiaRenderTypes.UNSTABLE);
        }
    }
}
