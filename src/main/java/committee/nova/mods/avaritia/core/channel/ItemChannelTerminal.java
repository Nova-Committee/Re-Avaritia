package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.channel.ChannelAction;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelActionPack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Compatibility terminal for legacy portable channel-panel items. */
public final class ItemChannelTerminal implements IChannelTerminal {
    private final UUID terminalOwner;
    private final ItemStack terminal;
    private final int slotID;
    private final Inventory inventory;

    public ItemChannelTerminal(Inventory inventory, ItemStack terminal, int slotID) {
        this.inventory = inventory;
        this.slotID = slotID;
        this.terminal = terminal;
        CompoundTag data = readData();
        terminalOwner = data.contains("owner") ? data.getUUID("owner") : inventory.player.getUUID();
    }

    private CompoundTag readData() {
        CustomData data = terminal.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private void writeData(CompoundTag data) {
        terminal.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    @Override public UUID getTerminalOwner() { return terminalOwner; }

    @Override
    public @Nullable ChannelInfo getChannelInfo() {
        CompoundTag data = readData();
        if (!data.contains("channel")) return null;
        CompoundTag channel = data.getCompound("channel");
        if (!channel.contains("channelOwner") || !channel.contains("channelID")) return null;
        return new ChannelInfo(channel.getUUID("channelOwner"), channel.getInt("channelID"));
    }

    @Override
    public void setChannel(UUID owner, int id) {
        CompoundTag data = readData();
        CompoundTag channel = new CompoundTag();
        channel.putUUID("channelOwner", owner);
        channel.putInt("channelID", id);
        data.put("channel", channel);
        writeData(data);
        if (inventory.player instanceof ServerPlayer player) {
            ServerChannelManager.sendChannelSet(player, terminalOwner, owner, id);
        }
    }

    @Override
    public void removeChannel(ServerPlayer actor) {
        ChannelInfo info = getChannelInfo();
        boolean allowed = info != null && (info.owner().equals(actor.getUUID())
                || info.owner().equals(Const.AVARITIA_FAKE_PLAYER.getId()) && actor.hasPermissions(2));
        ServerChannelManager manager = ServerChannelManager.getInstance();
        if (!allowed || manager == null || !manager.tryRemoveChannel(info.owner(), info.id())) return;
        CompoundTag data = readData();
        data.remove("channel");
        writeData(data);
        PacketDistributor.sendToPlayer(actor, new S2CChannelActionPack(ChannelAction.SET, (byte) -1, "", -1));
    }

    @Override
    public void renameChannel(ServerPlayer actor, String name) {
        ChannelInfo info = getChannelInfo();
        boolean allowed = info != null && (info.owner().equals(actor.getUUID())
                || info.owner().equals(Const.AVARITIA_FAKE_PLAYER.getId()) && actor.hasPermissions(2));
        ServerChannelManager manager = ServerChannelManager.getInstance();
        if (allowed && manager != null) manager.renameChannel(info, name);
    }

    @Override
    public void addChannelSelector(ServerPlayer player) {
        ChannelInfo info = getChannelInfo();
        if (info != null) ServerChannelManager.sendChannelSet(player, terminalOwner, info.owner(), info.id());
    }

    @Override public void removeChannelSelector(ServerPlayer player) { }
    @Override public boolean stillValid() { return slotID >= 0 && slotID < inventory.getContainerSize() && terminal == inventory.getItem(slotID); }
    @Override public void tryReOpenMenu(ServerPlayer player) { player.closeContainer(); }
}
