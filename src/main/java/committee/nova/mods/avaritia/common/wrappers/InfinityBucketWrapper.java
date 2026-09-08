package committee.nova.mods.avaritia.common.wrappers;

import committee.nova.mods.avaritia.common.component.InfinityBucketBudget;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreature;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreatures;
import committee.nova.mods.avaritia.common.component.InfinityBucketFluids;
import committee.nova.mods.avaritia.common.item.misc.InfinityBucketItem;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Infinity Bucket fluid handler. Uses {@link ItemAccess} so component writes land on the real stack/slot.
 * Distinct component patches never merge. Stacked buckets and empty resources are rejected independently.
 */
public class InfinityBucketWrapper implements ResourceHandler<FluidResource> {
    private final ItemAccess itemAccess;
    private final Item validItem;

    public InfinityBucketWrapper(@NotNull ItemAccess itemAccess) {
        this.itemAccess = itemAccess;
        this.validItem = itemAccess.getResource().getItem();
    }

    private boolean usable() {
        return itemAccess.getAmount() == 1 && itemAccess.getResource().is(validItem) && validItem == ModItems.infinity_bucket.get();
    }

    private InfinityBucketFluids fluidsComponent(ItemResource accessResource) {
        return accessResource.getOrDefault(ModDataComponents.INFINITY_BUCKET_FLUIDS.get(), InfinityBucketFluids.EMPTY);
    }

    private List<FluidStack> fluids(ItemResource accessResource) {
        return fluidsComponent(accessResource).copyFluids();
    }

    private List<InfinityBucketCreature> creatures(ItemResource accessResource) {
        return accessResource.getOrDefault(ModDataComponents.INFINITY_BUCKET_CREATURES.get(), InfinityBucketCreatures.EMPTY).copyCreatures();
    }

    @Override
    public int size() {
        if (!usable()) {
            return 0;
        }
        return fluids(itemAccess.getResource()).size() + 1;
    }

    @Override
    public FluidResource getResource(int index) {
        if (!usable()) {
            return FluidResource.EMPTY;
        }
        List<FluidStack> fluids = fluids(itemAccess.getResource());
        if (index < 0 || index >= fluids.size()) {
            return FluidResource.EMPTY;
        }
        return FluidResource.of(fluids.get(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        if (!usable()) {
            return 0;
        }
        List<FluidStack> fluids = fluids(itemAccess.getResource());
        if (index < 0 || index >= fluids.size()) {
            return 0;
        }
        return fluids.get(index).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        if (!usable()) {
            return 0;
        }
        if (!resource.isEmpty() && !isValid(index, resource)) {
            return 0;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return usable() && !resource.isEmpty();
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (itemAccess.getAmount() != 1) {
            return 0;
        }
        if (resource.isEmpty() || amount <= 0) {
            return 0;
        }
        if (!usable()) {
            return 0;
        }
        Objects.checkIndex(index, Math.max(1, size()));
        ItemResource accessResource = itemAccess.getResource();
        List<FluidStack> fluids = fluids(accessResource);
        int matching = indexOf(fluids, resource);
        if (matching >= 0 && index != matching) {
            return insert(matching, resource, amount, transaction);
        }
        if (index > fluids.size()) {
            return 0;
        }
        int current = index < fluids.size() ? fluids.get(index).getAmount() : 0;
        if (current > 0 && !resource.matches(fluids.get(index))) {
            return 0;
        }
        int inserted = (int) Math.min(amount, (long) Integer.MAX_VALUE - current);
        if (inserted <= 0) {
            return 0;
        }
        ItemResource updated = update(accessResource, index, resource, current + inserted);
        if (updated == null || updated.isEmpty()) {
            return 0;
        }
        return inserted * itemAccess.exchange(updated, 1, transaction);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        if (itemAccess.getAmount() != 1) {
            return 0;
        }
        if (resource.isEmpty() || amount <= 0) {
            return 0;
        }
        TransferPreconditions.checkNonNegative(amount);
        int matching = indexOf(fluids(itemAccess.getResource()), resource);
        if (matching >= 0) {
            return insert(matching, resource, amount, transaction);
        }
        return insert(fluids(itemAccess.getResource()).size(), resource, amount, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (itemAccess.getAmount() != 1) {
            return 0;
        }
        if (resource.isEmpty() || amount <= 0) {
            return 0;
        }
        if (!usable()) {
            return 0;
        }
        ItemResource accessResource = itemAccess.getResource();
        List<FluidStack> fluids = fluids(accessResource);
        if (index < 0 || index >= fluids.size() || !resource.matches(fluids.get(index))) {
            return 0;
        }
        int current = fluids.get(index).getAmount();
        int extracted = Math.min(amount, current);
        if (extracted <= 0) {
            return 0;
        }
        ItemResource updated = update(accessResource, index, resource, current - extracted);
        if (updated == null || updated.isEmpty()) {
            return 0;
        }
        return extracted * itemAccess.exchange(updated, 1, transaction);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        if (itemAccess.getAmount() != 1) {
            return 0;
        }
        if (resource.isEmpty() || amount <= 0) {
            return 0;
        }
        ItemResource accessResource = itemAccess.getResource();
        FluidStack selected = InfinityBucketItem.getSelectedFluid(accessResource.toStack());
        if (!selected.isEmpty() && resource.matches(selected)) {
            int matching = indexOf(fluids(accessResource), resource);
            if (matching >= 0) {
                return extract(matching, resource, amount, transaction);
            }
        }
        int matching = indexOf(fluids(accessResource), resource);
        if (matching < 0) {
            return 0;
        }
        return extract(matching, resource, amount, transaction);
    }

    @Nullable
    private ItemResource update(ItemResource accessResource, int index, FluidResource resource, int newAmount) {
        List<FluidStack> fluids = new ArrayList<>(fluids(accessResource));
        if (newAmount <= 0) {
            if (index < 0 || index >= fluids.size()) {
                return accessResource;
            }
            fluids.remove(index);
        } else if (index < fluids.size()) {
            fluids.set(index, resource.toStack(newAmount));
        } else if (index == fluids.size()) {
            if (fluids.size() >= InfinityBucketFluids.MAX_ENTRIES) {
                return null;
            }
            fluids.add(resource.toStack(newAmount));
        } else {
            return null;
        }
        if (!InfinityBucketBudget.canStore(fluids, creatures(accessResource))) {
            return null;
        }
        InfinityBucketFluids stored = new InfinityBucketFluids(fluids);
        if (stored.isEmpty()) {
            return accessResource.without(ModDataComponents.INFINITY_BUCKET_FLUIDS.get());
        }
        return accessResource.with(ModDataComponents.INFINITY_BUCKET_FLUIDS.get(), stored);
    }

    private static int indexOf(List<FluidStack> fluids, FluidResource resource) {
        for (int i = 0; i < fluids.size(); i++) {
            if (resource.matches(fluids.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
