package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.common.net.channel.ChannelState;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.util.StorageUtils;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;

/**
 * @author cnlimiter
 */
public class ServerChestHandler extends ChestHandler {
    private final HashSet<ItemStack> changedItems = new HashSet<>();
    private final HashSet<ServerPlayer> players = new HashSet<>();
    @Getter
    private boolean removed = false;

    public ServerChestHandler() {}

    public ServerChestHandler(CompoundTag dat) {
        initialize(dat);
    }

    @Override
    public void onItemChanged(ItemStack itemId, boolean listChanged) {
        super.onItemChanged(itemId, listChanged);
        changedItems.add(itemId);
    }

    public void initialize(CompoundTag dat) {
        storageItems.clear();
        if (dat.contains("items")) {
            CompoundTag items = dat.getCompound("items");
            items.getAllKeys().forEach(itemId -> {
                CompoundTag itemTag = items.getCompound(itemId);
                long count = itemTag.getLong("realCount");
                ItemStack item = ItemStack.of(itemTag.getCompound("item"));
                if (count > 0 && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemId))) {
                    storageItems.put(item, count);
                }
            });
            updateItemKeys();
        }
    }

    public void addListener(ServerPlayer player) {
        players.add(player);
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CInfinityChestStatePack(ChannelState.FULL, buildData()));
    }

    public void removeListener(ServerPlayer player) {
        players.remove(player);
    }

    public void sendUpdate() {
        if (!hasChanged()) return;
        if (!players.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            CompoundTag items = new CompoundTag();
            changedItems.forEach(itemStack -> {
                CompoundTag itemTag = new CompoundTag();
                itemTag.put("item", itemStack.serializeNBT());
                itemTag.putLong("realCount", storageItems.getOrDefault(itemStack, 0L));
                items.put(StorageUtils.getItemId(itemStack), itemTag);
            });
            tag.put("items", items);
            players.forEach(player -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CInfinityChestStatePack(ChannelState.COMMON, tag)));
        }
        resetChanged();
    }

    private boolean hasChanged() {
        return !changedItems.isEmpty();
    }

    private void resetChanged() {
        changedItems.clear();
    }

    public void sendFullUpdate() {
        if (!hasChanged()) return;
        if (!players.isEmpty()) {
            players.forEach(player -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CInfinityChestStatePack(ChannelState.FULL, buildData())));
        }
        changedItems.clear();
    }

    public CompoundTag buildData() {
        CompoundTag data = new CompoundTag();
        CompoundTag items = new CompoundTag();
        storageItems.forEach((itemStack, aLong) -> {
            CompoundTag itemTag = new CompoundTag();
            itemTag.put("item", itemStack.serializeNBT());
            itemTag.putLong("realCount", aLong);
            items.put(StorageUtils.getItemId(itemStack), itemTag);
        });
        data.put("items", items);
        return data;
    }

    public void setRemoved() {
        players.clear();
        this.removed = true;
    }
}
