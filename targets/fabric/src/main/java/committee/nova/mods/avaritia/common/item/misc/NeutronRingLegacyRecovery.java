package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.capability.RingStorageProvider;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

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
    private final Map<UUID, List<ItemStack>> pending = new HashMap<>();
    private final Set<UUID> overflowNotified = new HashSet<>();

    public static NeutronRingLegacyRecovery get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                NeutronRingLegacyRecovery::load, NeutronRingLegacyRecovery::new, NAME);
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
        for (ItemStack stack : player.getInventory().items) {
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
        boolean alreadyRecovered = ring.hasTag() && ring.getTag().getBoolean(RECOVERED_TAG);
        List<ItemStack> taken = RingStorageProvider.takeLegacyItems(ring);
        if (!alreadyRecovered && !taken.isEmpty()) {
            NeutronRingLegacyRecovery data = get(player.server);
            data.store(player.getUUID(), taken);
            data.setDirty();
        }
        ring.getOrCreateTag().putBoolean(RECOVERED_TAG, true);
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
        boolean changed = false;
        for (ItemStack next : remainders) {
            if (next.isEmpty()) {
                changed = true;
                continue;
            }
            int before = next.getCount();
            ItemStack remaining = next.copy();
            for (int pass = 0; pass < 2 && !remaining.isEmpty(); pass++) {
                for (int slot = 0; slot < player.getInventory().items.size() && !remaining.isEmpty(); slot++) {
                    ItemStack existing = player.getInventory().items.get(slot);
                    if (pass == 0 && !existing.isEmpty() && ItemStack.isSameItemSameTags(existing, remaining)) {
                        int moved = Math.min(remaining.getCount(), Math.max(0, existing.getMaxStackSize() - existing.getCount()));
                        existing.grow(moved);
                        remaining.shrink(moved);
                    } else if (pass == 1 && existing.isEmpty()) {
                        int moved = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                        player.getInventory().items.set(slot, remaining.copyWithCount(moved));
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
            player.displayClientMessage(Component.translatable("message.avaritia.neutron_ring.legacy_pending",
                    leftover.size()), true);
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


    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag players = new ListTag();
        pending.forEach((id, items) -> {
            CompoundTag entry = new CompoundTag();
            entry.put("Id", NbtUtils.createUUID(id));
            ListTag stacks = new ListTag();
            for (ItemStack item : items) {
                if (!item.isEmpty()) {
                    stacks.add(item.save(new CompoundTag()));
                }
            }
            entry.put("Items", stacks);
            entry.putBoolean("Notified", overflowNotified.contains(id));
            players.add(entry);
        });
        tag.put("Players", players);
        return tag;
    }

    static NeutronRingLegacyRecovery load(CompoundTag tag) {
        NeutronRingLegacyRecovery data = new NeutronRingLegacyRecovery();
        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag entry = players.getCompound(i);
            UUID id = NbtUtils.loadUUID(entry.get("Id"));
            ListTag stacks = entry.getList("Items", Tag.TAG_COMPOUND);
            List<ItemStack> items = new ArrayList<>();
            for (int j = 0; j < stacks.size(); j++) {
                ItemStack stack = ItemStack.of(stacks.getCompound(j));
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
            if (!items.isEmpty()) {
                data.pending.put(id, items);
                if (entry.getBoolean("Notified")) {
                    data.overflowNotified.add(id);
                }
            }
        }
        return data;
    }
}
