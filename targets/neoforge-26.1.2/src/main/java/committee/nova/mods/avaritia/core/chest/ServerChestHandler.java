package committee.nova.mods.avaritia.core.chest;

import com.mojang.serialization.Dynamic;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePacket;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** 服务端持久化无限箱通道。 */
public class ServerChestHandler extends ChestHandler {
    private static final int MINECRAFT_1_20_1_DATA_VERSION = 3465;

    private Runnable dirtyCallback = () -> { };
    private final Set<ServerPlayer> listeners = new HashSet<>();
    private boolean removed;

    public ServerChestHandler() {
    }

    public ServerChestHandler(Collection<StoredItem> storedItems) {
        super(storedItems);
    }

    void attachDirtyCallback(Runnable callback) {
        dirtyCallback = callback == null ? () -> { } : callback;
    }

    public void setRemoved() {
        removed = true;
        listeners.clear();
    }

    public void addListener(ServerPlayer player) {
        if (!removed) {
            listeners.add(player);
            sendFull(player);
        }
    }

    public void removeListener(ServerPlayer player) {
        listeners.remove(player);
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    @Override
    protected void onStorageCommitted(Snapshot originalState) {
        if (!removed) {
            dirtyCallback.run();
            syncChanges(originalState.items());
        }
    }

    private void syncChanges(Map<ItemResource, Long> before) {
        if (listeners.isEmpty()) {
            return;
        }
        listeners.removeIf(ServerPlayer::hasDisconnected);
        if (listeners.isEmpty()) {
            return;
        }
        Map<ItemResource, Long> current = entries().stream().collect(Collectors.toMap(
                StoredItem::resource, StoredItem::amount));
        List<StoredItem> changed = current.entrySet().stream()
                .filter(entry -> !entry.getValue().equals(before.get(entry.getKey())))
                .map(entry -> new StoredItem(entry.getKey(), entry.getValue()))
                .toList();
        List<ItemResource> removedResources = before.keySet().stream()
                .filter(resource -> !current.containsKey(resource))
                .toList();
        if (changed.size() + removedResources.size() > S2CInfinityChestStatePacket.PAGE_SIZE) {
            listeners.forEach(this::sendFull);
            return;
        }
        S2CInfinityChestStatePacket packet = S2CInfinityChestStatePacket.incremental(changed, removedResources);
        listeners.forEach(player -> PacketDistributor.sendToPlayer(player, packet));
    }

    private void sendFull(ServerPlayer player) {
        List<StoredItem> all = entries();
        List<List<StoredItem>> pages = ChestSyncPages.partition(all, S2CInfinityChestStatePacket.PAGE_SIZE);
        for (int index = 0; index < pages.size(); index++) {
            PacketDistributor.sendToPlayer(player, S2CInfinityChestStatePacket.fullPage(
                    pages.get(index), index == 0, index == pages.size() - 1));
        }
    }

    /**
     * 读取 1.21 独立通道文件。任一物品无法无损解析时返回空，调用方不得写入部分结果。
     */
    public static Optional<List<StoredItem>> readLegacyData(MinecraftServer server, CompoundTag data) {
        Tag items = data.get("items");
        if (items instanceof ListTag itemList) {
            return readLegacyItemList(server, itemList);
        }
        if (items instanceof CompoundTag itemMap) {
            return readLegacyItemMap(server, itemMap, data.getCompoundOrEmpty("nbtData"));
        }
        return Optional.of(List.of());
    }

    private static Optional<List<StoredItem>> readLegacyItemList(MinecraftServer server, ListTag items) {
        List<StoredItem> result = new ArrayList<>(items.size());
        var ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        for (int index = 0; index < items.size(); index++) {
            CompoundTag itemTag = items.getCompoundOrEmpty(index);
            long amount = itemTag.contains("realCount")
                    ? itemTag.getLongOr("realCount", 0L)
                    : itemTag.getLongOr("amount", 0L);
            Optional<ItemResource> resource = ItemResource.CODEC.parse(ops, itemTag).result();
            if (amount <= 0L || resource.isEmpty() || resource.get().isEmpty()) {
                return Optional.empty();
            }
            result.add(new StoredItem(resource.get(), amount));
        }
        return validateLegacyEntries(result);
    }

    private static Optional<List<StoredItem>> readLegacyItemMap(
            MinecraftServer server, CompoundTag items, CompoundTag nbtData) {
        List<StoredItem> result = new ArrayList<>();
        for (String legacyKey : items.keySet()) {
            long amount = items.getLongOr(legacyKey, 0L);
            if (amount <= 0L) {
                continue;
            }
            String rawId = legacyKey.contains("#") ? legacyKey.substring(0, legacyKey.indexOf('#')) : legacyKey;
            Identifier itemId = Identifier.tryParse(rawId);
            Item item = itemId == null ? Items.AIR : BuiltInRegistries.ITEM.getValue(itemId);
            if (item == null || item == Items.AIR) {
                Const.LOGGER.warn("Cannot import unknown legacy infinity chest item {}", legacyKey);
                return Optional.empty();
            }

            CompoundTag legacyData = nbtData.getCompound(legacyKey)
                    .map(CompoundTag::copy)
                    .orElseGet(CompoundTag::new);
            ItemStack stack = loadLegacyStack(server, itemId, item, legacyData);
            if (stack.isEmpty()) {
                return Optional.empty();
            }
            result.add(new StoredItem(ItemResource.of(stack), amount));
        }
        return validateLegacyEntries(result);
    }

    private static Optional<List<StoredItem>> validateLegacyEntries(List<StoredItem> entries) {
        Map<ItemResource, Long> merged = new LinkedHashMap<>();
        for (StoredItem entry : entries) {
            if (!merged.containsKey(entry.resource()) && merged.size() >= ChestHandler.MAX_VARIANTS) {
                return Optional.empty();
            }
            merged.merge(entry.resource(), entry.amount(), ServerChestHandler::saturatedAdd);
        }
        return Optional.of(merged.entrySet().stream()
                .map(entry -> new StoredItem(entry.getKey(), entry.getValue()))
                .toList());
    }

    private static long saturatedAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    private static ItemStack loadLegacyStack(
            MinecraftServer server, Identifier itemId, Item fallbackItem, CompoundTag legacyData) {
        try {
            ItemStack upgraded = ItemStack.OPTIONAL_CODEC.parse(
                    server.registryAccess().createSerializationContext(NbtOps.INSTANCE),
                    upgradeLegacyStackTag(itemId, legacyData)).result().orElse(ItemStack.EMPTY);
            if (!upgraded.isEmpty()) {
                return upgraded;
            }
        } catch (RuntimeException exception) {
            Const.LOGGER.error("Failed to upgrade legacy infinity chest item {}", itemId, exception);
        }

        ItemStack fallback = new ItemStack(fallbackItem);
        if (!legacyData.isEmpty()) {
            fallback.set(DataComponents.CUSTOM_DATA, CustomData.of(legacyData));
        }
        return fallback;
    }

    static CompoundTag upgradeLegacyStackTag(Identifier itemId, CompoundTag legacyData) {
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
                SharedConstants.getCurrentVersion().dataVersion().version()
        );
        if (upgraded.getValue() instanceof CompoundTag compoundTag) {
            return compoundTag;
        }
        throw new IllegalStateException("Legacy item stack data fixer returned a non-compound tag");
    }
}
