package committee.nova.mods.avaritia.core.channel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Component-sensitive, long-count backing store for one Tesseract channel.
 *
 * <p>The three public capability views share this journal. A transaction that
 * touches more than one resource kind therefore rolls the entire channel back
 * atomically.</p>
 */
public class Channel extends SnapshotJournal<Channel.Snapshot> {
    public static final int MAX_VARIANTS = 65_536;
    public static final String DEFAULT_NAME = "Channel";

    public static final Codec<ItemEntry> ITEM_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemResource.CODEC.fieldOf("resource").forGetter(ItemEntry::resource),
            ExtraCodecs.POSITIVE_LONG.fieldOf("amount").forGetter(ItemEntry::amount)
    ).apply(instance, ItemEntry::new));
    public static final Codec<FluidEntry> FLUID_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidResource.CODEC.fieldOf("resource").forGetter(FluidEntry::resource),
            ExtraCodecs.POSITIVE_LONG.fieldOf("amount").forGetter(FluidEntry::amount)
    ).apply(instance, FluidEntry::new));
    public static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", DEFAULT_NAME).forGetter(Data::name),
            ITEM_ENTRY_CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(Data::items),
            FLUID_ENTRY_CODEC.listOf().optionalFieldOf("fluids", List.of()).forGetter(Data::fluids),
            Codec.LONG.optionalFieldOf("energy", 0L).forGetter(Data::energy)
    ).apply(instance, Data::new));

    private final LinkedHashMap<ItemResource, Long> items = new LinkedHashMap<>();
    private final LinkedHashMap<FluidResource, Long> fluids = new LinkedHashMap<>();
    private final ItemStorage itemStorage = new ItemStorage();
    private final FluidStorage fluidStorage = new FluidStorage();
    private final EnergyStorage energyStorage = new EnergyStorage();
    private String name = DEFAULT_NAME;
    private long energy;

    public Channel() {
    }

    public Channel(Data data) {
        load(data);
    }

    public final ResourceHandler<ItemResource> items() {
        return itemStorage;
    }

    public final ResourceHandler<FluidResource> fluids() {
        return fluidStorage;
    }

    public final EnergyHandler energy() {
        return energyStorage;
    }

    public final String getName() {
        return name;
    }

    public void setName(String requestedName) {
        String next = normalizeName(requestedName);
        if (!next.equals(name)) {
            name = next;
            onMetadataChanged();
        }
    }

    public final int variantCount() {
        return items.size() + fluids.size() + (energy > 0 ? 1 : 0);
    }

    public final boolean isEmpty() {
        return items.isEmpty() && fluids.isEmpty() && energy == 0;
    }

    public boolean isRemoved() {
        return false;
    }

    public final List<ItemEntry> itemEntries() {
        return items.entrySet().stream().map(entry -> new ItemEntry(entry.getKey(), entry.getValue())).toList();
    }

    public final List<FluidEntry> fluidEntries() {
        return fluids.entrySet().stream().map(entry -> new FluidEntry(entry.getKey(), entry.getValue())).toList();
    }

    public final long energyAmount() {
        return energy;
    }

    public final long itemAmount(ItemResource resource) {
        return items.getOrDefault(resource, 0L);
    }

    public final long fluidAmount(FluidResource resource) {
        return fluids.getOrDefault(resource, 0L);
    }

    public final Data data() {
        return new Data(name, itemEntries(), fluidEntries(), energy);
    }

    public final int insert(ItemStack stack) {
        if (stack.isEmpty() || isRemoved()) {
            return 0;
        }
        int inserted;
        try (Transaction transaction = Transaction.openRoot()) {
            inserted = itemStorage.insert(ItemResource.of(stack), stack.getCount(), transaction);
            transaction.commit();
        }
        stack.shrink(inserted);
        return inserted;
    }

    public final ItemStack extract(ItemResource resource, int amount) {
        if (resource.isEmpty() || amount <= 0 || isRemoved()) {
            return ItemStack.EMPTY;
        }
        int extracted;
        try (Transaction transaction = Transaction.openRoot()) {
            extracted = itemStorage.extract(resource, amount, transaction);
            transaction.commit();
        }
        return extracted == 0 ? ItemStack.EMPTY : resource.toStack(extracted);
    }

    public final int insert(FluidStack stack) {
        if (stack.isEmpty() || isRemoved()) {
            return 0;
        }
        int inserted;
        try (Transaction transaction = Transaction.openRoot()) {
            inserted = fluidStorage.insert(FluidResource.of(stack), stack.getAmount(), transaction);
            transaction.commit();
        }
        stack.shrink(inserted);
        return inserted;
    }

    protected final void load(Data data) {
        name = normalizeName(data.name());
        items.clear();
        fluids.clear();
        energy = 0;
        for (ItemEntry entry : data.items()) {
            if (!entry.resource().isEmpty() && entry.amount() > 0
                    && (items.containsKey(entry.resource()) || variantCount() < MAX_VARIANTS)) {
                items.merge(entry.resource(), entry.amount(), Channel::saturatedAdd);
            }
        }
        for (FluidEntry entry : data.fluids()) {
            if (!entry.resource().isEmpty() && entry.amount() > 0
                    && (fluids.containsKey(entry.resource()) || variantCount() < MAX_VARIANTS)) {
                fluids.merge(entry.resource(), entry.amount(), Channel::saturatedAdd);
            }
        }
        if (data.energy() > 0 && variantCount() < MAX_VARIANTS) {
            energy = data.energy();
        }
    }

    public static String normalizeName(String requestedName) {
        String value = requestedName == null ? "" : requestedName.strip();
        if (value.isEmpty()) {
            return DEFAULT_NAME;
        }
        return value.substring(0, Math.min(64, value.length()));
    }

    private static long saturatedAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    private boolean canAddVariant(boolean alreadyPresent) {
        return alreadyPresent || variantCount() < MAX_VARIANTS;
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(new LinkedHashMap<>(items), new LinkedHashMap<>(fluids), energy);
    }

    @Override
    protected void revertToSnapshot(Snapshot snapshot) {
        items.clear();
        items.putAll(snapshot.items());
        fluids.clear();
        fluids.putAll(snapshot.fluids());
        energy = snapshot.energy();
    }

    @Override
    protected void onRootCommit(Snapshot originalState) {
        onStorageCommitted(originalState);
    }

    protected void onStorageCommitted(Snapshot originalState) {
    }

    protected void onMetadataChanged() {
    }

    public record ItemEntry(ItemResource resource, long amount) {
    }

    public record FluidEntry(FluidResource resource, long amount) {
    }

    public record Data(String name, List<ItemEntry> items, List<FluidEntry> fluids, long energy) {
        public Data {
            name = normalizeName(name);
            items = List.copyOf(items == null ? List.of() : items);
            fluids = List.copyOf(fluids == null ? List.of() : fluids);
            energy = Math.max(0, energy);
        }
    }

    protected record Snapshot(Map<ItemResource, Long> items, Map<FluidResource, Long> fluids, long energy) {
    }

    private final class ItemStorage implements ResourceHandler<ItemResource> {
        @Override
        public int size() {
            return items.size() + (variantCount() < MAX_VARIANTS ? 1 : 0);
        }

        @Override
        public ItemResource getResource(int index) {
            return resourceAt(items, index, ItemResource.EMPTY);
        }

        @Override
        public long getAmountAsLong(int index) {
            ItemResource resource = getResource(index);
            return resource.isEmpty() ? 0 : items.getOrDefault(resource, 0L);
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return validIndex(index, size()) && !resource.isEmpty() ? Long.MAX_VALUE : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return validIndex(index, size()) && !resource.isEmpty();
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (amount == 0 || isRemoved() || !validIndex(index, size())) {
                return 0;
            }
            ItemResource current = getResource(index);
            boolean present = items.containsKey(resource);
            if ((!current.isEmpty() && !current.equals(resource)) || !canAddVariant(present)) {
                return 0;
            }
            long stored = items.getOrDefault(resource, 0L);
            int inserted = (int) Math.min(amount, Long.MAX_VALUE - stored);
            if (inserted > 0) {
                updateSnapshots(transaction);
                items.put(resource, stored + inserted);
            }
            return inserted;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (amount == 0 || isRemoved() || !validIndex(index, size()) || !resource.equals(getResource(index))) {
                return 0;
            }
            long stored = items.getOrDefault(resource, 0L);
            int extracted = (int) Math.min(amount, stored);
            if (extracted > 0) {
                updateSnapshots(transaction);
                long remaining = stored - extracted;
                if (remaining == 0) items.remove(resource); else items.put(resource, remaining);
            }
            return extracted;
        }
    }

    private final class FluidStorage implements ResourceHandler<FluidResource> {
        @Override
        public int size() {
            return fluids.size() + (variantCount() < MAX_VARIANTS ? 1 : 0);
        }

        @Override
        public FluidResource getResource(int index) {
            return resourceAt(fluids, index, FluidResource.EMPTY);
        }

        @Override
        public long getAmountAsLong(int index) {
            FluidResource resource = getResource(index);
            return resource.isEmpty() ? 0 : fluids.getOrDefault(resource, 0L);
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return validIndex(index, size()) && !resource.isEmpty() ? Long.MAX_VALUE : 0;
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return validIndex(index, size()) && !resource.isEmpty();
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (amount == 0 || isRemoved() || !validIndex(index, size())) {
                return 0;
            }
            FluidResource current = getResource(index);
            boolean present = fluids.containsKey(resource);
            if ((!current.isEmpty() && !current.equals(resource)) || !canAddVariant(present)) {
                return 0;
            }
            long stored = fluids.getOrDefault(resource, 0L);
            int inserted = (int) Math.min(amount, Long.MAX_VALUE - stored);
            if (inserted > 0) {
                updateSnapshots(transaction);
                fluids.put(resource, stored + inserted);
            }
            return inserted;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (amount == 0 || isRemoved() || !validIndex(index, size()) || !resource.equals(getResource(index))) {
                return 0;
            }
            long stored = fluids.getOrDefault(resource, 0L);
            int extracted = (int) Math.min(amount, stored);
            if (extracted > 0) {
                updateSnapshots(transaction);
                long remaining = stored - extracted;
                if (remaining == 0) fluids.remove(resource); else fluids.put(resource, remaining);
            }
            return extracted;
        }
    }

    private final class EnergyStorage implements EnergyHandler {
        @Override
        public long getAmountAsLong() {
            return energy;
        }

        @Override
        public long getCapacityAsLong() {
            return Long.MAX_VALUE;
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            if (amount == 0 || isRemoved() || !canAddVariant(energy > 0)) {
                return 0;
            }
            int inserted = (int) Math.min(amount, Long.MAX_VALUE - energy);
            if (inserted > 0) {
                updateSnapshots(transaction);
                energy += inserted;
            }
            return inserted;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            if (amount == 0 || isRemoved()) {
                return 0;
            }
            int extracted = (int) Math.min(amount, energy);
            if (extracted > 0) {
                updateSnapshots(transaction);
                energy -= extracted;
            }
            return extracted;
        }
    }

    private static boolean validIndex(int index, int size) {
        return index >= 0 && index < size;
    }

    private static <T> T resourceAt(LinkedHashMap<T, Long> values, int index, T empty) {
        if (index < 0 || index >= values.size()) {
            return empty;
        }
        int current = 0;
        for (T resource : values.keySet()) {
            if (current++ == index) {
                return resource;
            }
        }
        return empty;
    }
}
