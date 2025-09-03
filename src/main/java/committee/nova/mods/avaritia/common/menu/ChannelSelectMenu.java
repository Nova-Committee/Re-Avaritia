package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import committee.nova.mods.avaritia.core.channel.IChannelTerminal;
import committee.nova.mods.avaritia.core.channel.ServerChannelManager;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 20:25
 * @Description:
 */
public class ChannelSelectMenu extends AbstractContainerMenu {

    private final Player player;
    public final IChannelTerminal terminal;

    public ChannelSelectMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(ModMenus.channel_select_menu.get(), containerId);
        this.player = playerInv.player;
        this.terminal = null;
    }

    public ChannelSelectMenu(int containerId, Player player, IChannelTerminal terminal) {
        super(ModMenus.channel_select_menu.get(), containerId);
        this.player = player;
        this.terminal = terminal;
        ServerChannelManager.getInstance().addChannelSelector((ServerPlayer) player, terminal.getTerminalOwner());
        this.terminal.addChannelSelector((ServerPlayer) player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pId) {
        switch (pId) {
            case 0 -> removeChannel();
            case 1 -> tryBack();
        }
        return true;
    }

    public void setChannel(byte type, int id) {
        switch (type) {
            case (byte) 0 -> terminal.setChannel(player.getUUID(), id);
            case (byte) 1 -> terminal.setChannel(terminal.getTerminalOwner(), id);
            case (byte) 2 -> terminal.setChannel(Const.AVARITIA_FAKE_PLAYER.getId(), id);
        }
    }

    public void removeChannel() {
        terminal.removeChannel((ServerPlayer) player);
    }

    public void renameChannel(String name) {
        terminal.renameChannel((ServerPlayer) player, name);
    }

    private void tryBack() {
        terminal.removeChannelSelector((ServerPlayer) player);
        if (terminal.getChannelInfo() == null) {
            player.closeContainer();
        } else {
            terminal.tryReOpenMenu((ServerPlayer) player);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return terminal.stillValid();
    }

    @Override
    public void removed(@NotNull Player pPlayer) {
        super.removed(pPlayer);
        if (pPlayer.isLocalPlayer()) ClientChannelManager.getInstance().onScreenClose();
        else {
            terminal.removeChannelSelector((ServerPlayer) pPlayer);
            ServerChannelManager.getInstance().removeChannelSelector((ServerPlayer) player);
        }
    }
}
