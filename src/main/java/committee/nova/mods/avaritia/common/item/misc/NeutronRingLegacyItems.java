package committee.nova.mods.avaritia.common.item.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.utils.ContainerUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * One-time escrow for pre-library Neutron Ring 81-slot inventories.
 * Items are copied here before the inventory component is stripped and never dropped.
 */
public final class NeutronRingLegacyItems extends SavedData {
    public static final String NAME = "avaritia_neutron_ring_legacy";

    private static final Codec<Escrow> ESCROW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(Escrow::owner),
            ItemStack.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(Escrow::items)
    ).apply(instance, Escrow::new));

    public static final Codec<NeutronRingLegacyItems> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ESCROW_CODEC.listOf().optionalFieldOf("escrows", List.of()).forGetter(NeutronRingLegacyItems::entries)
    ).apply(instance, NeutronRingLegacyItems::fromEntries));

    public static final SavedDataType<NeutronRingLegacyItems> TYPE = new SavedDataType<>(
            Const.rl(NAME), NeutronRingLegacyItems::new, CODEC);

    private final Map<UUID, List<ItemStack>> items = new HashMap<>();
    private final Set<UUID> leftoverNotified = new HashSet<>();

    public NeutronRingLegacyItems() {
    }

    private static NeutronRingLegacyItems fromEntries(List<Escrow> entries) {
        NeutronRingLegacyItems data = new NeutronRingLegacyItems();
        for (Escrow escrow : entries) {
            data.items.put(escrow.owner(), copyAll(escrow.items()));
        }
        return data;
    }

    private List<Escrow> entries() {
        List<Escrow> entries = new ArrayList<>(items.size());
        items.forEach((owner, stacks) -> entries.add(new Escrow(owner, copyAll(stacks))));
        return entries;
    }

    public static NeutronRingLegacyItems get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /**
     * Copies every non-empty slot into escrow, marks dirty, then strips the inventory component.
     * Drain leftover stacks into the player inventory without dropping; remainders stay in escrow.
     */
    public static void recover(ServerPlayer player, ItemStack ring) {
        NeutronRingLegacyItems escrow = get(player.level().getServer());
        escrow.importFrom(player.getUUID(), ring);
        escrow.drain(player);
    }

    public void importFrom(UUID owner, ItemStack ring) {
        ItemContainerContents contents = ring.get(committee.nova.mods.avaritia.init.registry.ModDataComponents.NEUTRON_RING_INVENTORY.get());
        if (contents == null) {
            return;
        }
        List<ItemStack> imported = contents.nonEmptyItemCopyStream()
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
        if (imported.isEmpty()) {
            ring.remove(committee.nova.mods.avaritia.init.registry.ModDataComponents.NEUTRON_RING_INVENTORY.get());
            return;
        }
        List<ItemStack> stored = items.computeIfAbsent(owner, ignored -> new ArrayList<>());
        for (ItemStack stack : imported) {
            stored.add(stack.copy());
        }
        setDirty();
        ring.remove(committee.nova.mods.avaritia.init.registry.ModDataComponents.NEUTRON_RING_INVENTORY.get());
    }

    public void drain(ServerPlayer player) {
        UUID owner = player.getUUID();
        List<ItemStack> stored = items.get(owner);
        if (stored == null || stored.isEmpty()) {
            leftoverNotified.remove(owner);
            return;
        }
        var main = PlayerInventoryWrapper.of(player).getMainSlots();
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : stored) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack leftover = ContainerUtils.insertItem(main, stack.copy(), false);
            if (!leftover.isEmpty()) {
                remaining.add(leftover);
            }
        }
        if (remaining.isEmpty()) {
            items.remove(owner);
            leftoverNotified.remove(owner);
        } else {
            items.put(owner, remaining);
            if (leftoverNotified.add(owner)) {
                player.sendSystemMessage(Component.translatable(
                        "message.avaritia.neutron_ring.legacy_pending", remaining.size()));
            }
        }
        setDirty();
    }

    public List<ItemStack> peek(UUID owner) {
        List<ItemStack> stored = items.get(owner);
        return stored == null ? List.of() : copyAll(stored);
    }

    private static List<ItemStack> copyAll(List<ItemStack> source) {
        List<ItemStack> copy = new ArrayList<>(source.size());
        for (ItemStack stack : source) {
            if (!stack.isEmpty()) {
                copy.add(stack.copy());
            }
        }
        return copy;
    }

    private record Escrow(UUID owner, List<ItemStack> items) {
    }
}
