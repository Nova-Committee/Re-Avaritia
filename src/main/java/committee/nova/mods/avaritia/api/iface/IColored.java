package committee.nova.mods.avaritia.api.iface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.BlockAndTintGetter;
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

    record ItemBlockColors(int index) implements ItemTintSource {
        public static final MapCodec<ItemBlockColors> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.optionalFieldOf("index", 0).forGetter(ItemBlockColors::index)
        ).apply(instance, ItemBlockColors::new));

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            Block block = Block.byItem(stack.getItem());
            return block instanceof IColored colored ? colored.getColor(stack, this.index) : -1;
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return CODEC;
        }
    }

    record ItemColors(int index) implements ItemTintSource {
        public static final MapCodec<ItemColors> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.optionalFieldOf("index", 0).forGetter(ItemColors::index)
        ).apply(instance, ItemColors::new));

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            return stack.getItem() instanceof IColored colored ? colored.getColor(stack, this.index) : -1;
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return CODEC;
        }
    }

    /**
     * 1.21.11 exposes block tinting through {@code BlockColor#getColor(state, level, pos, tintIndex)}.
     * The 26.1 {@code BlockTintSource} split this into {@code color(state)} and
     * {@code colorInWorld(state, level, pos)}; both only ever used index 0, so the single
     * 1.21.11 method keeps that behaviour.
     */
    class BlockColors implements BlockColor {
        @Override
        public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
            return ((IColored) state.getBlock()).getColor(0);
        }
    }
}
