package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/18 17:30
 * Version: 1.0
 */
public class InfinityTotemItem extends ResourceItem {


    public InfinityTotemItem() {
        super(ModRarities.EPIC, "infinity_totem", true, new Item.Properties().stacksTo(1).durability(99));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

}
