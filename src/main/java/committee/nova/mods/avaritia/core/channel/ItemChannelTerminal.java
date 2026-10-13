package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.channel.ChannelAction;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelActionPack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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
        CompoundTag nbt = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        UUID owner;
        try {
            owner = UUID.fromString(nbt.getStringOr("owner", Const.AVARITIA_FAKE_PLAYER.id().toString()));
        } catch (IllegalArgumentException e) {
            owner = Const.AVARITIA_FAKE_PLAYER.id();
        }
        this.terminalOwner = owner;
    }

    @Override
    public UUID getTerminalOwner() {
        return terminalOwner;
    }

    @Override
    @Nullable
    public ChannelInfo getChannelInfo() {
        CompoundTag nbt = terminal.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!nbt.contains("channel")) return null;
        CompoundTag channel = nbt.getCompoundOrEmpty("channel");
        int channelId = channel.getIntOr("channelID", 0);
        String ownerRaw = channel.getStringOr("channelOwner", "");
        if (ownerRaw.isEmpty()) return null;
        UUID channelOwner;
        try {
            channelOwner = UUID.fromString(ownerRaw);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return new ChannelInfo(channelOwner, channelId);
    }

    @Override
    public void setChannel(UUID channelOwner, int channelId) {
        CompoundTag root = terminal.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag channel = root.contains("channel") ? root.getCompoundOrEmpty("channel") : new CompoundTag();
        channel.putString("channelOwner", channelOwner.toString());
        channel.putInt("channelID", channelId);
        root.put("channel", channel);
        terminal.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
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
        if (info.owner().equals(actor.getUUID()) || info.owner().equals(Const.AVARITIA_FAKE_PLAYER.id())) {
            if (!ServerChannelManager.getInstance().tryRemoveChannel(info.owner(), info.id())) return;
            NetworkHandler.CHANNEL.sendTo((ServerPlayer) inventory.player, new S2CChannelActionPack(ChannelAction.SET, (byte) -1, "", -1));
            CompoundTag root = terminal.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            root.remove("channel");
            terminal.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        }
    }

    @Override
    public void renameChannel(ServerPlayer actor, String name) {
        ChannelInfo info = getChannelInfo();
        if (info == null) return;
        if (info.owner().equals(actor.getUUID()) || info.owner().equals(Const.AVARITIA_FAKE_PLAYER.id()))
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
