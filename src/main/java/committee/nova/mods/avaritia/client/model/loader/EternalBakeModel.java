package committee.nova.mods.avaritia.client.model.loader;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.api.client.model.PerspectiveModelState;
import committee.nova.mods.avaritia.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.avaritia.api.client.util.TransformUtils;
import committee.nova.mods.avaritia.client.AvaritiaForgeClient;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static committee.nova.mods.avaritia.client.shader.AvaritiaShaders.COSMIC_UVS;


public class EternalBakeModel extends WrappedItemModel {
    private final List<ResourceLocation> maskSprite;

    public EternalBakeModel(final BakedModel wrapped, final List<ResourceLocation> maskSprite) {
        super(wrapped);
        this.maskSprite = maskSprite;
    }


    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source, int light, int overlay) {
        if (stack.getItem() == ModItems.infinity_sword.get()) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        } else if (stack.getItem() == ModItems.infinity_bow.get() || stack.getItem() == ModItems.infinity_crossbow.get()) {
            this.parentState = TransformUtils.DEFAULT_BOW;
        } else {
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
        if (AvaritiaForgeClient.inventoryRender || transformType == ItemDisplayContext.GUI) {
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
            AvaritiaShaders.eternalOpacity.set(MatterClusterItem.getClusterSize(stack) / (float) MatterClusterItem.CAPACITY);
        } else {
            AvaritiaShaders.eternalOpacity.set(1.5F);
        }

        if (AvaritiaShaders.eternalUVs != null) {
            AvaritiaShaders.eternalUVs.set(COSMIC_UVS);
        }

        final VertexConsumer cons = source.getBuffer(AvaritiaRenderTypes.ETERNAL);
        List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
        for (ResourceLocation res : maskSprite) {
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
