package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.world.item.Item;

public class InfinityClockItem extends ResourceItem {
    public InfinityClockItem() {
        super(ModRarities.COSMIC.getValue(), "infinity_clock", false, new Item.Properties().stacksTo(1));
    }
}
