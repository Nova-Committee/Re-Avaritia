package committee.nova.mods.avaritia.core.chest;

import com.mojang.serialization.Dynamic;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.chest.ChannelState;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePack;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerChestHandler extends ChestHandler {
    private static final int MINECRAFT_1_20_1_DATA_VERSION = 3465;
    private final Map<ItemSuper, Long> changedItems = new HashMap<>();
    private final Set<ServerPlayer> players = new HashSet<>();
    @Nullable
    private MinecraftServer server;
    private boolean removed;
    private boolean loadComplete = true;

    public ServerChestHandler() {
    }

    public ServerChestHandler(MinecraftServer server) {
        this.server = server;
    }

    public ServerChestHandler(MinecraftServer server, CompoundTag data) {
        this.server = server;
        initialize(data);
    }

    @Override
    public void onItemChanged(ItemSuper item, boolean listChanged) {
        super.onItemChanged(item, listChanged);
        changedItems.put(item, storageItems.getOrDefault(item, 0L));
    }

    public void initialize(CompoundTag data) {
        storageItems.clear();
        loadComplete = true;
        if (data.contains("items", Tag.TAG_LIST)) {
            loadCurrentItems(data.getList("items", Tag.TAG_COMPOUND));
        } else if (data.contains("items", Tag.TAG_COMPOUND)) {
            loadLegacyItems(data.getCompound("items"), data.getCompound("nbtData"));
        }
        updateItemKeys();
    }

    private void loadCurrentItems(ListTag items) {
        if (server == null) {
            loadComplete = items.isEmpty();
            return;
        }
        for (int index = 0; index < items.size(); index++) {
            ItemSuper item = ItemSuper.fromTag(server.registryAccess(), items.getCompound(index));
            if (item != null && item.getRealCount() > 0) {
                storageItems.put(item, item.getRealCount());
            } else if (item == null) {
                loadComplete = false;
            }
        }
    }

    private void loadLegacyItems(CompoundTag items, CompoundTag nbtData) {
        for (String legacyKey : items.getAllKeys()) {
            long count = items.getLong(legacyKey);
            if (count <= 0) {
                continue;
            }
            ResourceLocation itemId = ResourceLocation.tryParse(getBaseItemId(legacyKey));
            Item item = itemId == null ? Items.AIR : BuiltInRegistries.ITEM.get(itemId);
            if (item == Items.AIR) {
                Const.LOGGER.warn("Skipping unknown legacy infinity chest item {}", legacyKey);
                loadComplete = false;
                continue;
            }
            CompoundTag legacyData = nbtData.contains(legacyKey, Tag.TAG_COMPOUND)
                    ? nbtData.getCompound(legacyKey).copy() : new CompoundTag();
            ItemStack stack = loadLegacyStack(itemId, item, legacyData);
            storageItems.put(new ItemSuper(stack, count), count);
        }
    }

    private ItemStack loadLegacyStack(ResourceLocation itemId, Item fallbackItem, CompoundTag legacyData) {
        if (server != null) {
            try {
                ItemStack upgraded = ItemStack.parseOptional(server.registryAccess(),
                        upgradeLegacyStackTag(itemId, legacyData));
                if (!upgraded.isEmpty()) {
                    return upgraded;
                }
                loadComplete = false;
                Const.LOGGER.error("Data fixer produced an empty legacy infinity chest item {}", itemId);
            } catch (RuntimeException exception) {
                loadComplete = false;
                Const.LOGGER.error("Failed to upgrade legacy infinity chest item {}", itemId, exception);
            }
        }

        ItemStack fallback = new ItemStack(fallbackItem);
        if (!legacyData.isEmpty()) {
            fallback.set(DataComponents.CUSTOM_DATA, CustomData.of(legacyData));
        }
        return fallback;
    }

    static CompoundTag upgradeLegacyStackTag(ResourceLocation itemId, CompoundTag legacyData) {
        CompoundTag legacyStack = new CompoundTag();
        legacyStack.putString("id", itemId.toString());
        legacyStack.putByte("Count", (byte) 1);
        if (!legacyData.isEmpty()) {
            legacyStack.put("tag", legacyData.copy());
        }
        Dynamic<Tag> upgraded = DataFixers.getDataFixer().update(
                References.ITEM_STACK,
                new Dynamic<>(NbtOps.INSTANCE, legacyStack),
                MINECRAFT_1_20_1_DATA_VERSION,
                SharedConstants.getCurrentVersion().getDataVersion().getVersion()
        );
        if (upgraded.getValue() instanceof CompoundTag compoundTag) {
            return compoundTag;
        }
        throw new IllegalStateException("Legacy item stack data fixer returned a non-compound tag");
    }

    private static String getBaseItemId(String legacyKey) {
        int separator = legacyKey.indexOf('#');
        return separator < 0 ? legacyKey : legacyKey.substring(0, separator);
    }

    public void addListener(ServerPlayer player) {
        players.add(player);
        PacketDistributor.sendToPlayer(player, new S2CInfinityChestStatePack(ChannelState.FULL, snapshot(storageItems)));
    }

    public void removeListener(ServerPlayer player) {
        players.remove(player);
    }

    public void sendUpdate() {
        if (changedItems.isEmpty()) {
            return;
        }
        if (!players.isEmpty()) {
            Collection<ItemSuper> changed = snapshot(changedItems);
            players.forEach(player -> PacketDistributor.sendToPlayer(player,
                    new S2CInfinityChestStatePack(ChannelState.COMMON, changed)));
        }
        changedItems.clear();
    }

    public void sendFullUpdate() {
        if (changedItems.isEmpty()) {
            return;
        }
        if (!players.isEmpty()) {
            Collection<ItemSuper> snapshot = snapshot(storageItems);
            players.forEach(player -> PacketDistributor.sendToPlayer(player,
                    new S2CInfinityChestStatePack(ChannelState.FULL, snapshot)));
        }
        changedItems.clear();
    }

    private static Collection<ItemSuper> snapshot(Map<ItemSuper, Long> items) {
        ArrayList<ItemSuper> snapshot = new ArrayList<>(items.size());
        items.forEach((item, count) -> snapshot.add(item.copyWithCount(count)));
        return snapshot;
    }

    public CompoundTag buildData() {
        if (server == null) {
            throw new IllegalStateException("Cannot serialize an infinity chest handler without a server");
        }
        CompoundTag data = new CompoundTag();
        ListTag items = new ListTag();
        storageItems.forEach((item, count) -> items.add(item.copyWithCount(count).toTag(server.registryAccess())));
        data.put("items", items);
        return data;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved() {
        players.clear();
        removed = true;
    }

    public boolean isLoadComplete() {
        return loadComplete;
    }
}
