package committee.nova.mods.avaritia.common.item.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.util.ItemUtils;
import committee.nova.mods.avaritia.common.capability.RingStorageProvider;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * One-time recovery of the old 81-slot Neutron Ring backpack.
 * Remainders stay in overworld SavedData until the player has inventory space.
 */
public final class NeutronRingLegacyRecovery extends SavedData {
    public static final String NAME = "avaritia_neutron_ring_legacy";
    public static final String RECOVERED_TAG = "NeutronRingLegacyRecovered";

    private static final Codec<PlayerEntry> PLAYER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(PlayerEntry::id),
            ItemStack.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(PlayerEntry::items),
            Codec.BOOL.optionalFieldOf("notified", false).forGetter(PlayerEntry::notified)
    ).apply(instance, PlayerEntry::new));

    public static final Codec<NeutronRingLegacyRecovery> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PLAYER_CODEC.listOf().optionalFieldOf("players", List.of()).forGetter(NeutronRingLegacyRecovery::entries)
    ).apply(instance, NeutronRingLegacyRecovery::fromEntries));

    // 26.3 Fabric 的 SavedDataType 为 4 参构造；DataFixTypes 不能为 null，用 SAVED_DATA_COMMAND_STORAGE 占位
    public static final SavedDataType<NeutronRingLegacyRecovery> TYPE = new SavedDataType<>(
            Const.rl(NAME), NeutronRingLegacyRecovery::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<UUID, List<ItemStack>> pending = new HashMap<>();
    private final Set<UUID> overflowNotified = new HashSet<>();

    public NeutronRingLegacyRecovery() {
    }

    private record PlayerEntry(UUID id, List<ItemStack> items, boolean notified) {
    }

    private static NeutronRingLegacyRecovery fromEntries(List<PlayerEntry> entries) {
        NeutronRingLegacyRecovery data = new NeutronRingLegacyRecovery();
        for (PlayerEntry entry : entries) {
            List<ItemStack> items = new ArrayList<>(entry.items());
            items.removeIf(ItemStack::isEmpty);
            if (!items.isEmpty()) {
                data.pending.put(entry.id(), items);
                if (entry.notified()) {
                    data.overflowNotified.add(entry.id());
                }
            }
        }
        return data;
    }

    private List<PlayerEntry> entries() {
        List<PlayerEntry> entries = new ArrayList<>(pending.size());
        pending.forEach((id, items) -> entries.add(new PlayerEntry(id, List.copyOf(items), overflowNotified.contains(id))));
        return entries;
    }

    public static NeutronRingLegacyRecovery get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static void recover(ServerPlayer player, ItemStack ring) {
        extractFromRing(player, ring);
        drain(player);
    }

    public static void recoverAll(ServerPlayer player) {
        Set<ItemStack> rings = new HashSet<>();
        ItemStack found = NeutronRingItem.find(player);
        if (!found.isEmpty()) {
            rings.add(found);
        }
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(ModItems.neutron_ring.get())) {
                rings.add(stack);
            }
        }
        if (player.getOffhandItem().is(ModItems.neutron_ring.get())) {
            rings.add(player.getOffhandItem());
        }
        for (ItemStack ring : rings) {
            extractFromRing(player, ring);
        }
        drain(player);
    }

    static void extractFromRing(ServerPlayer player, ItemStack ring) {
        if (ring.isEmpty() || !ring.is(ModItems.neutron_ring.get())) {
            return;
        }
        CompoundTag tag = ring.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        boolean alreadyRecovered = tag.getBooleanOr(RECOVERED_TAG, false);
        List<ItemStack> taken = RingStorageProvider.takeLegacyItems(ring);
        if (!alreadyRecovered && !taken.isEmpty()) {
            NeutronRingLegacyRecovery data = get(player.server);
            data.store(player.getUUID(), taken);
            data.setDirty();
        }
        CustomData.update(DataComponents.CUSTOM_DATA, ring, t -> t.putBoolean(RECOVERED_TAG, true));
    }


    static void drain(ServerPlayer player) {
        NeutronRingLegacyRecovery data = get(player.server);
        UUID id = player.getUUID();
        List<ItemStack> remainders = data.pending.get(id);
        if (remainders == null || remainders.isEmpty()) {
            data.overflowNotified.remove(id);
            return;
        }
        List<ItemStack> leftover = new ArrayList<>(remainders.size());
        NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();
        boolean changed = false;
        for (ItemStack next : remainders) {
            if (next.isEmpty()) {
                changed = true;
                continue;
            }
            int before = next.getCount();
            ItemStack remaining = next.copy();
            for (int pass = 0; pass < 2 && !remaining.isEmpty(); pass++) {
                for (int slot = 0; slot < items.size() && !remaining.isEmpty(); slot++) {
                    ItemStack existing = items.get(slot);
                    if (pass == 0 && !existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, remaining)) {
                        int moved = Math.min(remaining.getCount(), Math.max(0, existing.getMaxStackSize() - existing.getCount()));
                        existing.grow(moved);
                        remaining.shrink(moved);
                    } else if (pass == 1 && existing.isEmpty()) {
                        int moved = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                        items.set(slot, remaining.copyWithCount(moved));
                        remaining.shrink(moved);
                    }
                }
            }
            player.getInventory().setChanged();
            if (remaining.isEmpty()) {
                changed = true;
                continue;
            }
            if (remaining.getCount() != before) {
                changed = true;
            }
            leftover.add(remaining.copy());
        }
        if (leftover.isEmpty()) {
            data.pending.remove(id);
            data.overflowNotified.remove(id);
        } else {
            data.pending.put(id, leftover);
        }
        if (changed) {
            data.setDirty();
        }
        if (!leftover.isEmpty() && data.overflowNotified.add(id)) {
            data.setDirty();
            ItemUtils.sendActionbar(player, Component.translatable("message.avaritia.neutron_ring.legacy_pending",
                    leftover.size()));
        }
    }

    void store(UUID player, List<ItemStack> items) {
        List<ItemStack> existing = pending.computeIfAbsent(player, ignored -> new ArrayList<>());
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                existing.add(item.copy());
            }
        }
        overflowNotified.remove(player);
    }

    List<ItemStack> remaining(UUID player) {
        List<ItemStack> items = pending.get(player);
        return items == null ? List.of() : List.copyOf(items);
    }
}
