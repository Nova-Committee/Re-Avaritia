package committee.nova.mods.avaritia.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.client.model.PerspectiveModelState;
import committee.nova.mods.avaritia.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.avaritia.api.client.render.item.IItemRenderer;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.client.TransformUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Name: Avaritia-forge / CosmicbakeModel
 * Author: cnlimiter
 * CreateTime: 2023/9/24 4:41
 * Description:
 */

public class CosmicBakeModel extends WrappedItemModel implements IItemRenderer {
    private final List<ResourceLocation> maskSprite;
    public CosmicBakeModel(final BakedModel wrapped, final List<ResourceLocation> maskSprite) {
        super(wrapped);
        this.maskSprite = maskSprite;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source, int light, int overlay) {
        if (stack.getItem() == ModItems.infinity_sword.get()) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        }
        if (stack.getItem() == ModItems.infinity_bow.get()) {
            this.parentState = TransformUtils.DEFAULT_BOW;
        }
        if (stack.getItem() != ModItems.infinity_bow.get() && stack.getItem() != ModItems.infinity_sword.get()) {
            this.parentState = TransformUtils.DEFAULT_ITEM;
        }
        this.renderWrapped(stack, pStack, source, light, overlay, true);
        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }
        final Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0f;
        float pitch = 0.0f;
        float scale = 1f;
        if (AvaritiaShaders.inventoryRender || transformType == ItemDisplayContext.GUI) {
            scale = 100.0F;
        } else {
            yaw = (float)(mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -(float)(mc.player.getXRot() * 2.0f * Math.PI / 360.0);
        }

        AvaritiaShaders.cosmicTime
                .set((System.currentTimeMillis() - AvaritiaShaders.renderTime) / 2000.0F);
        AvaritiaShaders.cosmicYaw.set(yaw);
        AvaritiaShaders.cosmicPitch.set(pitch);
        AvaritiaShaders.cosmicExternalScale.set(scale);

        if (stack.getItem() == ModItems.matter_cluster.get()) {
            AvaritiaShaders.cosmicOpacity.set(MatterClusterItem.getClusterSize(stack) / (float)MatterClusterItem.CAPACITY);
        } else {
            AvaritiaShaders.cosmicOpacity.set(1.0F);
        }

        for (int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(Static.rl("misc/cosmic_" + i));
            AvaritiaShaders.COSMIC_UVS[i * 4] = sprite.getU0();
            AvaritiaShaders.COSMIC_UVS[i * 4 + 1] = sprite.getV0();
            AvaritiaShaders.COSMIC_UVS[i * 4 + 2] = sprite.getU1();
            AvaritiaShaders.COSMIC_UVS[i * 4 + 3] = sprite.getV1();
        }
        if (AvaritiaShaders.cosmicUVs != null) {
            AvaritiaShaders.cosmicUVs.set(AvaritiaShaders.COSMIC_UVS);
        }

        final VertexConsumer cons = source.getBuffer(AvaritiaShaders.COSMIC_RENDER_TYPE);
        List<TextureAtlasSprite> atlasSprite  = new ArrayList<>();
        for (ResourceLocation res : maskSprite){
            atlasSprite.add(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }
        mc.getItemRenderer().renderQuadList(pStack, cons, bakeItem(atlasSprite), stack, light, overlay);
    }

    @Override
    public @Nullable PerspectiveModelState getModelState() {
        return (PerspectiveModelState) this.parentState;
    }

    @Override
    public boolean isCosmic() {
        return true;
    }
}
