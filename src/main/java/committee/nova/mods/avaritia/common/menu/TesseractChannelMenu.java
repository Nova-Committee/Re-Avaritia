package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.core.channel.ChannelInfo;
import committee.nova.mods.avaritia.core.channel.IChannelTerminal;
import committee.nova.mods.avaritia.core.channel.ServerChannelManager;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Server-authoritative channel create/select/rename/delete menu. */
public class TesseractChannelMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final UUID terminalOwner;
    public final @Nullable IChannelTerminal terminal;

    public TesseractChannelMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), buffer.readUUID());
    }

    public TesseractChannelMenu(int id, Inventory inventory, IChannelTerminal terminal, BlockPos pos) {
        this(id, inventory, pos, terminal.getTerminalOwner());
    }

    private TesseractChannelMenu(int id, Inventory inventory, BlockPos pos, UUID terminalOwner) {
        super(ModMenus.tesseract_channel.get(), id);
        this.pos = pos;
        this.terminalOwner = terminalOwner;
        this.terminal = inventory.player.level().getBlockEntity(pos) instanceof IChannelTerminal found ? found : null;
        if (inventory.player instanceof ServerPlayer player) {
            ServerChannelManager manager = ServerChannelManager.getInstance();
            if (manager != null) manager.openSelector(player, terminalOwner);
        }
    }

    public UUID getTerminalOwner() {
        return terminalOwner;
    }

    public boolean canPlayerModify(Player player) {
        return terminal != null && terminal.canPlayerModify(player);
    }

    public void setChannel(ServerPlayer player, byte type, int id) {
        ServerChannelManager manager = ServerChannelManager.getInstance();
        if (manager == null || terminal == null) return;
        ChannelInfo reference = manager.resolveSelection(player, terminalOwner, type, id);
        if (reference != null) {
            terminal.setChannel(reference);
            manager.sendSelected(player, terminalOwner, reference);
            terminal.openMainMenu(player);
        }
    }

    public void addChannel(ServerPlayer player, String name, boolean shared) {
        ServerChannelManager manager = ServerChannelManager.getInstance();
        if (manager != null) manager.create(player, name, shared);
    }

    public void renameChannel(ServerPlayer player, byte type, int id, String name) {
        ServerChannelManager manager = ServerChannelManager.getInstance();
        if (manager == null) return;
        ChannelInfo reference = manager.resolveSelection(player, terminalOwner, type, id);
        if (reference != null) manager.rename(player, reference, name);
    }

    public void removeChannel(ServerPlayer player, byte type, int id) {
        ServerChannelManager manager = ServerChannelManager.getInstance();
        if (manager == null) return;
        ChannelInfo reference = manager.resolveSelection(player, terminalOwner, type, id);
        if (reference != null && manager.remove(player, reference) && terminal != null
                && reference.equals(terminal.getChannelInfo())) {
            terminal.setChannel(null);
            manager.sendSelected(player, terminalOwner, null);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return terminal != null && terminal.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer) {
            ServerChannelManager manager = ServerChannelManager.getInstance();
            if (manager != null) manager.closeSelector(serverPlayer);
        }
    }
}
