package committee.nova.mods.avaritia.common.menu.provider;

import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import committee.nova.mods.avaritia.core.channel.IChannelTerminal;
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
 * @CreateTime: 2025/3/2 01:23
 * @Description:
 */
public class ChannelSelectMenuProvider implements MenuProvider {

    private final IChannelTerminal terminal;
    public ChannelSelectMenuProvider(IChannelTerminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("");
    }

    @Override
    @ParametersAreNonnullByDefault
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new TesseractChannelMenu(pContainerId, pPlayer, terminal);
    }
}
