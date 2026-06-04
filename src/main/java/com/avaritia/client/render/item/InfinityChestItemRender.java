package com.avaritia.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.avaritia.Const;
import com.avaritia.api.client.util.TextureUtils;
import com.avaritia.client.render.tile.InfinityChestBlockRender;
import com.avaritia.init.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * @author cnlimiter
 */
public class InfinityChestItemRender {
    public static void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext,
                                     @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                                     int packedLight, int packedOverlay) {
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            BlockState blockstate = block.defaultBlockState();
            if (blockstate.is(ModBlocks.infinity_chest.get())) {
                ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(InfinityChestBlockRender.INFINITY_CHEST);
                TextureAtlasSprite sprite = TextureUtils.getTexture(Const.rl("block/chest/infinity_chest"));
                VertexConsumer consumer = sprite.wrap(buffer.getBuffer(RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS)));
                root.getChild("lid").render(poseStack, consumer, packedLight, packedOverlay);
                root.getChild("lock").render(poseStack, consumer, packedLight, packedOverlay);
                root.getChild("bottom").render(poseStack, consumer, packedLight, packedOverlay);
            }
        }
    }
}
