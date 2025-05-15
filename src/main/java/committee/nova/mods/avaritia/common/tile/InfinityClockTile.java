package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import committee.nova.mods.avaritia.common.menu.InfinityClockBlockMenu;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/4/4 01:05
 * @Description:
 */
public class InfinityClockTile extends BaseInventoryTileEntity {
    int range = 12;
    int speed = 100;
    public final Iterable<BlockPos> targetBlocks;
    public final ItemStackWrapper inventory;
    public Mode mode = Mode.COMMON;

    public InfinityClockTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.infinity_clock_tile.get(), pos, state);
        this.inventory = createInventoryHandler();
        this.targetBlocks = BlockPos.betweenClosed(pos.getX() - this.range, pos.getY() - this.range, pos.getZ() - this.range,
                pos.getX() + this.range, pos.getY() + this.range, pos.getZ() + this.range);
    }

    public static ItemStackWrapper createInventoryHandler() {
        return ItemStackWrapper.create(1);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InfinityClockTile tile) {
        var randomTicks = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        tile.targetBlocks.forEach(blockPos -> {
            if(level instanceof ServerLevel serverLevel && !serverLevel.getBlockState(blockPos).is(ModBlocks.infinity_clock.get()))
                ToolUtils.speedBlockTick(blockPos, serverLevel, tile.speed, randomTicks);
        });
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("container.infinity_clock").build();
    }

    @Override
    public @NotNull ItemStackWrapper getInventory() {
        return this.inventory;
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return new InfinityClockBlockMenu(pContainerId, pInventory, this.getBlockPos());
    }


    public enum Mode implements StringRepresentable {
        COMMON("common"),
        CARD("card");

        private final String name;

        private Mode(String pName) {
            this.name = pName;
        }

        public String toString() {
            return this.getSerializedName();
        }

        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}
