package committee.nova.mods.avaritia.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Name: Avaritia-forge / CosmicbakeModel
 * Author: cnlimiter
 * CreateTime: 2023/9/24 4:41
 * Description:
 */

public class CosmicBakeModel extends WrappedItemModel implements IItemRenderer {
    private final List<BakedQuad> maskQuads;
    public CosmicBakeModel(final BakedModel wrapped, final TextureAtlasSprite maskSprite) {
        super(wrapped);
        this.maskQuads = WrappedItemModel.bakeItem(maskSprite);
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
        float scale = 25f;
        if (!AvaritiaShaders.inventoryRender) {
            yaw = (float)(mc.player.getYRot() * 2.0f * 3.141592653589793 / 360.0);
            pitch = -(float)(mc.player.getXRot() * 2.0f * 3.141592653589793 / 360.0);
            scale = 2f;
        }
        if (stack.getItem() == ModItems.matter_cluster.get()) {
            AvaritiaShaders.cosmicOpacity.set(MatterClusterItem.getClusterSize(stack) / (float)MatterClusterItem.CAPACITY);
        } else {
            AvaritiaShaders.cosmicOpacity.set(0.2F);
        }
        AvaritiaShaders.cosmicYaw.set(yaw);
        AvaritiaShaders.cosmicPitch.set(pitch);
        AvaritiaShaders.cosmicExternalScale.set(scale);
        final VertexConsumer cons = source.getBuffer(AvaritiaShaders.COSMIC_RENDER_TYPE);
        mc.getItemRenderer().renderQuadList(pStack, cons, this.maskQuads, stack, light, overlay);
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
