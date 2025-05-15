package committee.nova.mods.avaritia.api.common.slot;

import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 18:12
 * Version: 1.0
 */
public class ItemStackWrapperSlot extends SlotItemHandler {
    private final ItemStackWrapper inventory;
    private final int index;

    public ItemStackWrapperSlot(ItemStackWrapper inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.inventory = inventory;
        this.index = index;
    }

    @Override
    public boolean mayPickup(Player player) {
        return !this.inventory.extractItem(this.index, 1, true).isEmpty();
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        return this.inventory.extractItem(this.index, amount, false);
    }

}
