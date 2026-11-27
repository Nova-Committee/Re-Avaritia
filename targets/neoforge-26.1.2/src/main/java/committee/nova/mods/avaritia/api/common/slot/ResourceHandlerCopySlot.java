package committee.nova.mods.avaritia.api.common.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.world.inventory.StackCopySlot;

public class ResourceHandlerCopySlot extends StackCopySlot {
    private final ResourceHandler<ItemResource> handler;

    public ResourceHandlerCopySlot(ResourceHandler<ItemResource> handler, int handlerSlot, int xPosition, int yPosition) {
        super(handlerSlot, xPosition, yPosition);
        this.handler = handler;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && this.handler.isValid(this.getSlotIndex(), ItemResource.of(stack));
    }

    @Override
    protected ItemStack getStackCopy() {
        return ItemUtil.getStack(this.handler, this.getSlotIndex());
    }

    @Override
    protected void setStackCopy(ItemStack stack) {
        try (var tx = Transaction.openRoot()) {
            int index = this.getSlotIndex();
            ItemResource existing = this.handler.getResource(index);
            if (!existing.isEmpty()) {
                this.handler.extract(index, existing, this.handler.getAmountAsInt(index), tx);
            }

            if (stack.isEmpty()) {
                tx.commit();
                return;
            }

            int inserted = this.handler.insert(index, ItemResource.of(stack), stack.getCount(), tx);
            if (inserted == stack.getCount()) {
                tx.commit();
            }
        }
    }

    @Override
    public int getMaxStackSize() {
        return this.handler.getCapacityAsInt(this.getSlotIndex(), ItemResource.EMPTY);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return this.handler.getCapacityAsInt(this.getSlotIndex(), ItemResource.of(stack));
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemResource resource = this.handler.getResource(this.getSlotIndex());
        if (resource.isEmpty()) {
            return false;
        }
        try (var tx = Transaction.openRoot()) {
            return this.handler.extract(this.getSlotIndex(), resource, 1, tx) == 1;
        }
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return other instanceof ResourceHandlerCopySlot slot && slot.handler == this.handler;
    }
}
