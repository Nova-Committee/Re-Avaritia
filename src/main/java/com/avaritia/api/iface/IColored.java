package com.avaritia.api.iface;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 物品/方块染色接口
 */
public interface IColored {
    default int getColor(int index) {
        return -1;
    }

    default int getColor(ItemStack stack, int index) {
        return this.getColor(index);
    }

    class ItemBlockColors implements ItemTintSource {
        public static final MapCodec<ItemBlockColors> CODEC = MapCodec.unit(ItemBlockColors::new);

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            return ((IColored) Block.byItem(stack.getItem())).getColor(stack, 0);
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return CODEC;
        }
    }

    class ItemColors implements ItemTintSource {
        public static final MapCodec<ItemColors> CODEC = MapCodec.unit(ItemColors::new);

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            return ((IColored) stack.getItem()).getColor(stack, 0);
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return CODEC;
        }
    }

    class BlockColors implements BlockTintSource {
        @Override
        public int color(BlockState state) {
            return ((IColored) state.getBlock()).getColor(0);
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return ((IColored) state.getBlock()).getColor(0);
        }
    }
}
