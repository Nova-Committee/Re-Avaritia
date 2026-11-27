package committee.nova.mods.avaritia.api.common.menu;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/1/6 13:07
 * @Description:
 */
@Getter
public abstract class BaseTileMenu<T extends BlockEntity> extends BaseMenu {

    private final BlockPos blockPos;

    protected BaseTileMenu(MenuType<?> menu, int id, Inventory playerInventory, @NotNull BlockPos blockPos) {
        super(menu, id, playerInventory);
        this.blockPos = blockPos;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockPos == null || player.distanceToSqr(this.blockPos.getX() + 0.5, this.blockPos.getY() + 0.5, this.blockPos.getZ() + 0.5) <= 64;
    }

    @SuppressWarnings("unchecked")
    public T getTileEntity() {
        return (T) level.getBlockEntity(blockPos);
    }

}
