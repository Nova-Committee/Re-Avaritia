package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.world.item.Rarity;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/2 上午12:32
 * @Description:
 */
public class InfinityRingItem extends ResourceItem {


    public InfinityRingItem() {
        super(ModRarities.LEGEND, "infinity_ring", true, new Properties().stacksTo(1));
    }


}
