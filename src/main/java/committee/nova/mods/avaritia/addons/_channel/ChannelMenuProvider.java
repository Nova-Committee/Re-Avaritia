package committee.nova.mods.avaritia.addons._channel;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 20:10
 * @Description:
 */
public class ChannelMenuProvider implements MenuProvider {

    private final BlackHoleTile blockEntity;
    private final int slotIndex;

    public ChannelMenuProvider(BlackHoleTile blockEntity) {
        this.blockEntity = blockEntity;
        this.slotIndex = -2;
    }

    public ChannelMenuProvider(int slotIndex) {
        this.blockEntity = null;
        this.slotIndex = slotIndex;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("");
    }

    @Override
    @ParametersAreNonnullByDefault
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ChannelMenu(pContainerId, pPlayer, blockEntity, slotIndex);
    }
}