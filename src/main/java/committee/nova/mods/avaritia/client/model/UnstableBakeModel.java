package committee.nova.mods.avaritia.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.model.PerspectiveModelState;
import committee.nova.mods.avaritia.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.avaritia.api.client.util.TransformUtils;
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


public class UnstableBakeModel extends WrappedItemModel {
    public static final float[] UNSTABLE_UVS = new float[40];
    private final List<ResourceLocation> maskSprite;

    public UnstableBakeModel(final BakedModel wrapped, final List<ResourceLocation> maskSprite) {
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
        if (AvaritiaShaders.inventoryRender || transformType == ItemDisplayContext.GUI) {
            scale = 100.0F;
        } else {
            yaw = (float) (mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -(float) (mc.player.getXRot() * 2.0f * Math.PI / 360.0);
        }


        AvaritiaShaders.unstableTime
                .set((System.currentTimeMillis() - AvaritiaShaders.renderTime) / 2000.0F);
        AvaritiaShaders.unstableYaw.set(yaw);
        AvaritiaShaders.unstablePitch.set(pitch);
        AvaritiaShaders.unstableExternalScale.set(scale);

        if (stack.getItem() == ModItems.matter_cluster.get()) {
            AvaritiaShaders.unstableOpacity.set(MatterClusterItem.getClusterSize(stack) / (float) MatterClusterItem.CAPACITY);
        } else {
            AvaritiaShaders.unstableOpacity.set(1.5F);
        }


        for (int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(Const.rl("misc/cosmic_" + i));
            UNSTABLE_UVS[i * 4] = sprite.getU0();
            UNSTABLE_UVS[i * 4 + 1] = sprite.getV0();
            UNSTABLE_UVS[i * 4 + 2] = sprite.getU1();
            UNSTABLE_UVS[i * 4 + 3] = sprite.getV1();
        }

        if (AvaritiaShaders.unstableUVs != null) {
            AvaritiaShaders.unstableUVs.set(UNSTABLE_UVS);
        }

        final VertexConsumer cons = source.getBuffer(AvaritiaRenderTypes.UNSTABLE);
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
