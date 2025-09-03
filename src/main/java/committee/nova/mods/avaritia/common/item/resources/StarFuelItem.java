package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.fabricmc.fabric.api.registry.FuelRegistry;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/18 17:30
 * Version: 1.0
 */
public class StarFuelItem extends ResourceItem {


    public static final int BURN_TIME = Integer.MAX_VALUE;

    public StarFuelItem() {
        super(ModRarities.RARE, "star_fuel", true, new Properties().stacksTo(16));
        FuelRegistry.INSTANCE.add(this, BURN_TIME);
    }

}
