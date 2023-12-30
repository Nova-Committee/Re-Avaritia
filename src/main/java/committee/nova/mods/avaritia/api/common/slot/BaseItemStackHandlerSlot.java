package committee.nova.mods.avaritia.api.common.slot;

import committee.nova.mods.avaritia.api.common.item.BaseItemStackHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 18:12
 * Version: 1.0
 */
public class BaseItemStackHandlerSlot extends SlotItemHandler {
    private final BaseItemStackHandler inventory;
    private final int index;

    public BaseItemStackHandlerSlot(BaseItemStackHandler inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.inventory = inventory;
        this.index = index;
    }

    @Override
    public boolean mayPickup(Player player) {
        return !this.inventory.extractItemSuper(this.index, 1, true).isEmpty();
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        return this.inventory.extractItemSuper(this.index, amount, false);
    }

}
