package committee.nova.mods.avaritia.api.common.slot;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.minecraft.world.item.ItemStack;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 15:16
 * Version: 1.0
 */
public class OutputSlot extends SlotItemHandler {


    public OutputSlot(SlottedStackStorage storage, int index, int xPosition, int yPosition) {
        super(storage, index, xPosition, yPosition);
    }

    public OutputSlot(SlottedStorage<ItemVariant> storage, int index, int xPosition, int yPosition) {
        super(storage, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

}
