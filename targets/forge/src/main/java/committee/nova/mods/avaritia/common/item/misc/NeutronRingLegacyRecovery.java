package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.Const;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
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
@Mod.EventBusSubscriber(modid = Const.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        ItemStack extra = Const.checkExtraSlots(player, stack -> stack.is(ModItems.neutron_ring.get()),
                ItemStack.EMPTY, stack -> stack);
        if (!extra.isEmpty()) {
            rings.add(extra);
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
        ring.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            List<ItemStack> taken = copySlots(handler);
            if (alreadyRecovered) {
                if (!taken.isEmpty()) {
                    clearSlots(handler);
                }
                return;
            }
            if (!taken.isEmpty()) {
                NeutronRingLegacyRecovery data = get(player.server);
                data.store(player.getUUID(), taken);
                data.setDirty();
            }
            ring.getOrCreateTag().putBoolean(RECOVERED_TAG, true);
            if (!taken.isEmpty()) {
                clearSlots(handler);
            }
        });
    }

    static List<ItemStack> copySlots(IItemHandler handler) {
        List<ItemStack> taken = new ArrayList<>();
        int slots = handler.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack stored = handler.getStackInSlot(i);
            if (!stored.isEmpty()) {
                taken.add(stored.copy());
            }
        }
        return taken;
    }

    static void clearSlots(IItemHandler handler) {
        int slots = handler.getSlots();
        for (int i = 0; i < slots; i++) {
            if (handler.getStackInSlot(i).isEmpty()) {
                continue;
            }
            if (handler instanceof IItemHandlerModifiable modifiable) {
                modifiable.setStackInSlot(i, ItemStack.EMPTY);
            } else {
                ItemStack stored = handler.getStackInSlot(i);
                handler.extractItem(i, stored.getCount(), false);
            }
        }
    }

    static void drain(ServerPlayer player) {
        NeutronRingLegacyRecovery data = get(player.server);
        UUID id = player.getUUID();
        List<ItemStack> remainders = data.pending.get(id);
        if (remainders == null || remainders.isEmpty()) {
            data.overflowNotified.remove(id);
            return;
        }
        IItemHandler inventory = new PlayerMainInvWrapper(player.getInventory());
        List<ItemStack> leftover = new ArrayList<>(remainders.size());
        boolean changed = false;
        for (ItemStack next : remainders) {
            if (next.isEmpty()) {
                changed = true;
                continue;
            }
            int before = next.getCount();
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(inventory, next, false);
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

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            recoverAll(serverPlayer);
        }
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
