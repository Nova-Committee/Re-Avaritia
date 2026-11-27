package committee.nova.mods.avaritia.api.common.slot;

import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/**
 * @author cnlimiter
 */
public class ItemStackWrapperSlot extends ResourceHandlerSlot {
    public ItemStackWrapperSlot(ResourceHandler<ItemResource> inventory, IndexModifier<ItemResource> modifier, int index, int x, int y) {
        super(inventory, modifier, index, x, y);
    }
}
