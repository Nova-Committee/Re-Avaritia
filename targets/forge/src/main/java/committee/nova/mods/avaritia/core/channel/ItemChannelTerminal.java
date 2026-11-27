package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.channel.ChannelAction;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelActionPack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/3/3 15:36
 * @Description:
 */
public class ItemChannelTerminal implements IChannelTerminal {

    private final UUID terminalOwner;
    private final ItemStack terminal;
    private final int slotID;
    private final Inventory inventory;

    public ItemChannelTerminal(Inventory playerInventory, ItemStack itemStack, int slotID) {
        this.inventory = playerInventory;
        this.slotID = slotID;
        this.terminal = itemStack;
        CompoundTag nbt = itemStack.getOrCreateTag();
        this.terminalOwner = nbt.getUUID("owner");
    }

    @Override
    public UUID getTerminalOwner() {
        return terminalOwner;
    }

    @Override
    @Nullable
    public ChannelInfo getChannelInfo() {
        CompoundTag nbt = terminal.getOrCreateTag();
        if (!nbt.contains("channel")) return null;
        CompoundTag channel = nbt.getCompound("channel");
        int channelId = channel.getInt("channelID");
        UUID channelOwner = channel.getUUID("channelOwner");
        return new ChannelInfo(channelOwner, channelId);
    }

    @Override
    public void setChannel(UUID channelOwner, int channelId) {
        CompoundTag nbt = terminal.getOrCreateTag();
        CompoundTag channel;
        if (nbt.contains("channel")) channel = nbt.getCompound("channel");
        else {
            channel = new CompoundTag();
            nbt.put("channel", channel);
        }
        channel.putUUID("channelOwner", channelOwner);
        channel.putInt("channelID", channelId);
        ServerChannelManager.sendChannelSet((ServerPlayer) inventory.player, terminalOwner, channelOwner, channelId);
    }

    @Override
    public void addChannelSelector(ServerPlayer player) {
        ChannelInfo info = getChannelInfo();
        if (info == null) return;
        ServerChannelManager.sendChannelSet((ServerPlayer) inventory.player, terminalOwner, info.owner(), info.id());
    }

    @Override
    public void removeChannel(ServerPlayer actor) {
        ChannelInfo info = getChannelInfo();
        if (info == null) return;
        if (info.owner().equals(actor.getUUID()) || info.owner().equals(Const.AVARITIA_FAKE_PLAYER.getId())) {
            if (!ServerChannelManager.getInstance().tryRemoveChannel(info.owner(), info.id())) return;
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) inventory.player), new S2CChannelActionPack(ChannelAction.SET, (byte) -1, "", -1));
            terminal.getTag().remove("channel");
        }
    }

    @Override
    public void renameChannel(ServerPlayer actor, String name) {
        ChannelInfo info = getChannelInfo();
        if (info == null) return;
        if (info.owner().equals(actor.getUUID()) || info.owner().equals(Const.AVARITIA_FAKE_PLAYER.getId()))
            ServerChannelManager.getInstance().renameChannel(info, name);
    }

    @Override
    public void removeChannelSelector(ServerPlayer player) {
    }

    @Override
    public boolean stillValid() {
        return terminal == inventory.getItem(slotID);
    }

    @Override
    public void tryReOpenMenu(ServerPlayer player) {
        if (getChannelInfo() != null) terminal.use(player.level(), player, InteractionHand.MAIN_HAND);
    }
}
