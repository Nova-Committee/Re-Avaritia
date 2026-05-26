package com.avaritia.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.avaritia.common.tile.InfinityChestTile;
import com.avaritia.init.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
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
                Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(
                        new InfinityChestTile(BlockPos.ZERO, ModBlocks.infinity_chest.get().defaultBlockState()),
                        poseStack, buffer, packedLight, packedOverlay);
            }
        }
    }
}
