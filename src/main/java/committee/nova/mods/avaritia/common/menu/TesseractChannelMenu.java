package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.tile.TesseractTile;
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
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TesseractChannelMenu extends AbstractContainerMenu {
    private final Player player;
    @Nullable
    public final IChannelTerminal terminal;

    public TesseractChannelMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(ModMenus.tesseract_channel.get(), containerId);
        player = playerInv.player;
        terminal = null;
    }

    public TesseractChannelMenu(int containerId, Player player, IChannelTerminal terminal) {
        super(ModMenus.tesseract_channel.get(), containerId);
        this.player = player;
        this.terminal = terminal;
        if (player instanceof ServerPlayer serverPlayer) {
            ServerChannelManager manager = ServerChannelManager.getInstance();
            if (manager != null) {
                manager.addChannelSelector(serverPlayer, terminal.getTerminalOwner());
            }
            terminal.addChannelSelector(serverPlayer);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        if (!canPlayerModify(player)) {
            return false;
        }
        switch (id) {
            case 0 -> removeChannel();
            case 1 -> tryBack();
            default -> {
                return false;
            }
        }
        return true;
    }

    public void setChannel(byte type, int id) {
        if (!(player instanceof ServerPlayer serverPlayer) || terminal == null || !canPlayerModify(player)
                || id < 0 || id >= 10_000) {
            return;
        }
        UUID owner = switch (type) {
            case 0 -> player.getUUID();
            case 1 -> terminal.getTerminalOwner();
            case 2 -> Const.AVARITIA_FAKE_PLAYER.getId();
            default -> null;
        };
        ServerChannelManager manager = ServerChannelManager.getInstance();
        if (owner == null || manager == null || !manager.isSelecting(serverPlayer, terminal.getTerminalOwner())
                || manager.getChannel(owner, id).isRemoved()) {
            return;
        }
        terminal.setChannel(owner, id);
    }

    public void removeChannel() {
        if (player instanceof ServerPlayer serverPlayer && terminal != null && canPlayerModify(player)) {
            terminal.removeChannel(serverPlayer);
        }
    }

    public void renameChannel(String name) {
        if (player instanceof ServerPlayer serverPlayer && terminal != null && canPlayerModify(player)) {
            terminal.renameChannel(serverPlayer, name);
        }
    }

    public boolean canPlayerModify(Player candidate) {
        if (terminal == null || !terminal.stillValid()) {
            return false;
        }
        return !(terminal instanceof TesseractTile tile) || tile.stillValid(candidate);
    }

    public boolean isBoundTo(IChannelTerminal expected) {
        return terminal == expected;
    }

    public UUID getTerminalOwner() {
        return terminal == null ? player.getUUID() : terminal.getTerminalOwner();
    }

    private void tryBack() {
        if (!(player instanceof ServerPlayer serverPlayer) || terminal == null) {
            return;
        }
        terminal.removeChannelSelector(serverPlayer);
        if (terminal.getChannelInfo() == null) {
            player.closeContainer();
        } else {
            terminal.tryReOpenMenu(serverPlayer);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return canPlayerModify(player);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer && terminal != null) {
            terminal.removeChannelSelector(serverPlayer);
            ServerChannelManager manager = ServerChannelManager.getInstance();
            if (manager != null) {
                manager.removeChannelSelector(serverPlayer);
            }
        }
    }
}
