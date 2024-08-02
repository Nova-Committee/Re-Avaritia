package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/2 上午12:32
 * @Description:
 */
public class InfinityUmbrellaItem extends ResourceItem {


    public InfinityUmbrellaItem() {
        super(ModItems.COSMIC_RARITY, "infinity_umbrella", true, new Item.Properties().stacksTo(1));
    }


}
