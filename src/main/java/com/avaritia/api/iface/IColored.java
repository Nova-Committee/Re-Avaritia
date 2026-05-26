package com.avaritia.api.iface;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 物品/方块染色接口�? */
public interface IColored {
    default int getColor(int index) {
        return -1;
    }

    default int getColor(ItemStack stack, int index) {
        return this.getColor(index);
    }

    class ItemBlockColors implements ItemColor {
        @Override
        public int getColor(ItemStack stack, int index) {
            return ((IColored) Block.byItem(stack.getItem())).getColor(stack, index);
        }
    }

    class ItemColors implements ItemColor {
        @Override
        public int getColor(ItemStack stack, int index) {
            return ((IColored) stack.getItem()).getColor(stack, index);
        }
    }

    class BlockColors implements BlockColor {
        @Override
        public int getColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int index) {
            return ((IColored) state.getBlock()).getColor(index);
        }
    }
}
