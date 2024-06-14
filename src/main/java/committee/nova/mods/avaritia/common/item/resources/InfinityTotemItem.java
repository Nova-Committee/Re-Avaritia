package committee.nova.mods.avaritia.common.item.resources;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/18 17:30
 * Version: 1.0
 */
public class InfinityTotemItem extends ResourceItem {


    public InfinityTotemItem() {
        super(Rarity.UNCOMMON, "infinity_totem", true, new Item.Properties().stacksTo(1).durability(999));
    }

}
