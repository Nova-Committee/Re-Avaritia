package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.fabricmc.fabric.api.registry.FuelRegistry;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/25 19:29
 * @Description:
 */
public class RefinedCoalItem extends ResourceItem {
    public RefinedCoalItem(String registryName) {
        super(ModRarities.UNCOMMON, registryName, true, new Properties().stacksTo(32));
        FuelRegistry.INSTANCE.add(this, BURN_TIME);
    }

    public static final int BURN_TIME = 16000 * 10;
}
