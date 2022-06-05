package nova.committee.avaritia.api.init.iface;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface IColored {
    default int getColor(int index) {
        return -1;
    }

    default int getColor(int index, ItemStack stack) {
        return this.getColor(index);
    }

    public static class ItemBlockColors implements ItemColor {
        public ItemBlockColors() {
        }

        public int getColor(ItemStack stack, int index) {
            return ((IColored) Block.byItem(stack.getItem())).getColor(index, stack);
        }
    }

    public static class ItemColors implements ItemColor {
        public ItemColors() {
        }

        public int getColor(ItemStack stack, int index) {
            return ((IColored) stack.getItem()).getColor(index, stack);
        }
    }

    public static class BlockColors implements BlockColor {
        public BlockColors() {
        }

        public int getColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int index) {
            return ((IColored) state.getBlock()).getColor(index);
        }
    }
}
