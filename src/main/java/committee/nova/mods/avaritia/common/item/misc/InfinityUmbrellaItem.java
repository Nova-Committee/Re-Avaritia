package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.world.item.Item;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/2 上午12:32
 * @Description:
 */
public class InfinityUmbrellaItem extends ResourceItem {


    public InfinityUmbrellaItem() {
        super(ModRarities.COSMIC, "infinity_umbrella", true, new Item.Properties().stacksTo(1));
    }


}
