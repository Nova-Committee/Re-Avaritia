package committee.nova.mods.avaritia.core.chest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 以完整物品组件为身份、以 {@code long} 为数量的无限箱存储。
 *
 * <p>最后一个索引是接受新物品种类的虚拟索引。所有修改都先登记事务快照，
 * 只有根事务提交后才触发持久化和同步钩子。</p>
 */
public class ChestHandler extends SnapshotJournal<ChestHandler.Snapshot>
        implements ResourceHandler<ItemResource> {
    public static final int MAX_VARIANTS = 65_536;

    public static final Codec<StoredItem> STORED_ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemResource.CODEC.fieldOf("resource").forGetter(StoredItem::resource),
            ExtraCodecs.POSITIVE_LONG.fieldOf("amount").forGetter(StoredItem::amount)
    ).apply(instance, StoredItem::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StoredItem> STORED_ITEM_STREAM_CODEC = StreamCodec.of(
            (buffer, storedItem) -> {
                ItemResource.STREAM_CODEC.encode(buffer, storedItem.resource());
                buffer.writeVarLong(storedItem.amount());
            },
            buffer -> {
                ItemResource resource = ItemResource.STREAM_CODEC.decode(buffer);
                long amount = buffer.readVarLong();
                if (resource.isEmpty() || amount <= 0L) {
                    throw new DecoderException("Infinity chest entries require a non-empty resource and positive amount");
                }
                return new StoredItem(resource, amount);
            }
    );

    private final LinkedHashMap<ItemResource, Long> items = new LinkedHashMap<>();

    public ChestHandler() {
    }

    public ChestHandler(Collection<StoredItem> storedItems) {
        load(storedItems);
    }

    public boolean isRemoved() {
        return false;
    }

    public final boolean isEmpty() {
        return items.isEmpty();
    }

    public final int variantCount() {
        return items.size();
    }

    public final long amount(ItemResource resource) {
        return resource == null || resource.isEmpty() ? 0L : items.getOrDefault(resource, 0L);
    }

    public final List<StoredItem> entries() {
        return items.entrySet().stream()
                .map(entry -> new StoredItem(entry.getKey(), entry.getValue()))
                .toList();
    }

    public final Set<ItemResource> resources() {
        return Set.copyOf(items.keySet());
    }

    /** 在同一事务中处理超过 {@code int} 的导入数量。 */
    public final long insertLong(ItemResource resource, long amount, TransactionContext transaction) {
        if (resource == null || resource.isEmpty()) {
            throw new IllegalArgumentException("Resource may not be empty");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount may not be negative");
        }
        if (amount == 0L || isRemoved()) {
            return 0L;
        }
        boolean present = items.containsKey(resource);
        if (!present && items.size() >= MAX_VARIANTS) {
            return 0L;
        }
        long stored = items.getOrDefault(resource, 0L);
        long inserted = ChestAmounts.insertable(stored, amount);
        if (inserted > 0L) {
            updateSnapshots(transaction);
            items.put(resource, stored + inserted);
        }
        return inserted;
    }

    /** 在同一事务中处理超过 {@code int} 的提取数量。 */
    public final long extractLong(ItemResource resource, long amount, TransactionContext transaction) {
        if (resource == null || resource.isEmpty()) {
            throw new IllegalArgumentException("Resource may not be empty");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount may not be negative");
        }
        if (amount == 0L || isRemoved()) {
            return 0L;
        }
        long stored = items.getOrDefault(resource, 0L);
        long extracted = ChestAmounts.extractable(stored, amount);
        if (extracted > 0L) {
            updateSnapshots(transaction);
            long remaining = stored - extracted;
            if (remaining == 0L) {
                items.remove(resource);
            } else {
                items.put(resource, remaining);
            }
        }
        return extracted;
    }

    @Override
    public int size() {
        return items.size() + (items.size() < MAX_VARIANTS ? 1 : 0);
    }

    @Override
    public ItemResource getResource(int index) {
        if (index < 0 || index >= items.size()) {
            return ItemResource.EMPTY;
        }
        int current = 0;
        for (ItemResource resource : items.keySet()) {
            if (current++ == index) {
                return resource;
            }
        }
        return ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        ItemResource resource = getResource(index);
        return resource.isEmpty() ? 0L : items.getOrDefault(resource, 0L);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        if (!isValid(index, resource)) {
            return 0L;
        }
        ItemResource current = getResource(index);
        return current.isEmpty() || current.equals(resource) ? Long.MAX_VALUE : 0L;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return index >= 0 && index < size() && resource != null && !resource.isEmpty();
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int index = indexOf(resource);
        if (index < 0) {
            index = items.size();
        }
        return insert(index, resource, amount, transaction);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || isRemoved() || index < 0 || index >= size()) {
            return 0;
        }

        ItemResource current = getResource(index);
        boolean present = items.containsKey(resource);
        if ((!current.isEmpty() && !current.equals(resource)) || (!present && items.size() >= MAX_VARIANTS)) {
            return 0;
        }

        long stored = items.getOrDefault(resource, 0L);
        int inserted = (int) ChestAmounts.insertable(stored, amount);
        if (inserted > 0) {
            updateSnapshots(transaction);
            items.put(resource, stored + inserted);
        }
        return inserted;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int index = indexOf(resource);
        return index < 0 ? 0 : extract(index, resource, amount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || isRemoved() || index < 0 || index >= items.size()
                || !resource.equals(getResource(index))) {
            return 0;
        }

        long stored = items.getOrDefault(resource, 0L);
        int extracted = (int) ChestAmounts.extractable(stored, amount);
        if (extracted > 0) {
            updateSnapshots(transaction);
            long remaining = stored - extracted;
            if (remaining == 0L) {
                items.remove(resource);
            } else {
                items.put(resource, remaining);
            }
        }
        return extracted;
    }

    protected final void load(Collection<StoredItem> storedItems) {
        items.clear();
        if (storedItems == null) {
            return;
        }
        for (StoredItem entry : storedItems) {
            if (entry == null || entry.resource().isEmpty() || entry.amount() <= 0L) {
                continue;
            }
            if (!items.containsKey(entry.resource()) && items.size() >= MAX_VARIANTS) {
                break;
            }
            items.merge(entry.resource(), entry.amount(), ChestHandler::saturatedAdd);
        }
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(new LinkedHashMap<>(items));
    }

    @Override
    protected void revertToSnapshot(Snapshot snapshot) {
        items.clear();
        items.putAll(snapshot.items());
    }

    @Override
    protected void onRootCommit(Snapshot originalState) {
        onStorageCommitted(originalState);
    }

    protected void onStorageCommitted(Snapshot originalState) {
    }

    private int indexOf(ItemResource resource) {
        int index = 0;
        for (ItemResource stored : items.keySet()) {
            if (stored.equals(resource)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private static long saturatedAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    public record StoredItem(ItemResource resource, long amount) {
        public StoredItem {
            if (resource == null || resource.isEmpty()) {
                throw new IllegalArgumentException("Infinity chest resource may not be empty");
            }
            if (amount <= 0L) {
                throw new IllegalArgumentException("Infinity chest amount must be positive");
            }
        }
    }

    protected record Snapshot(Map<ItemResource, Long> items) {
    }
}
