package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.channel.ChannelInfo;
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

import java.util.UUID;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/2/28 20:25
 * @Description:
 */
public class TesseractChannelMenu extends AbstractContainerMenu {

    private final Player player;
    public final IChannelTerminal terminal;

    public TesseractChannelMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(ModMenus.tesseract_channel.get(), containerId);
        this.player = playerInv.player;
        this.terminal = null;
    }

    public TesseractChannelMenu(int containerId, Player player, IChannelTerminal terminal) {
        super(ModMenus.tesseract_channel.get(), containerId);
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
            case (byte) 2 -> terminal.setChannel(Const.AVARITIA_FAKE_PLAYER.id(), id);
        }
    }

    public void removeChannel() {
        terminal.removeChannel((ServerPlayer) player);
    }

    /**
     * 按包内携带的频道身份（type + id）解析并删除目标频道。
     * 归属校验：只能删除本人或公共频道；删除成功后若目标正是终端当前绑定频道，则解除绑定。
     */
    public void removeChannel(ServerPlayer actor, byte type, int id) {
        UUID owner = switch (type) {
            case 0 -> actor.getUUID();
            case 1 -> terminal.getTerminalOwner();
            case 2 -> Const.AVARITIA_FAKE_PLAYER.id();
            default -> null;
        };
        if (owner == null) return;
        if (!owner.equals(actor.getUUID()) && !owner.equals(Const.AVARITIA_FAKE_PLAYER.id())) return;
        if (!ServerChannelManager.getInstance().tryRemoveChannel(owner, id)) return;
        ChannelInfo bound = terminal.getChannelInfo();
        if (bound != null && bound.owner().equals(owner) && bound.id() == id) terminal.clearChannel();
    }

    public void renameChannel(String name) {
        terminal.renameChannel((ServerPlayer) player, name);
    }

    private void tryBack() {
        terminal.removeChannelSelector((ServerPlayer) player);
        if (terminal.getChannelInfo() == null) {
            ((ServerPlayer) player).closeContainer();
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

        if (pPlayer.level().isClientSide()) {
            ClientChannelManager.getInstance().onScreenClose();
        } else {
            terminal.removeChannelSelector((ServerPlayer) player);
            ServerChannelManager.getInstance().removeChannelSelector((ServerPlayer) player);
        }
    }
}
