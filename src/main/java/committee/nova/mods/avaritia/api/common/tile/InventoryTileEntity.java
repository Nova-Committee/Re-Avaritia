package committee.nova.mods.avaritia.api.common.tile;

import committee.nova.mods.avaritia.api.common.item.BaseItemStackHandler;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:59
 * Version: 1.0
 */
public abstract class InventoryTileEntity extends BaseTileEntity implements MenuProvider {

    public InventoryTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract @NotNull BaseItemStackHandler getInventory();

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.getInventory().deserializeNBT(tag);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.merge(this.getInventory().serializeNBT());
    }

    public boolean isUsableByPlayer(Player player) {
        BlockPos pos = this.getBlockPos();
        return player.distanceToSqr((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }

}
