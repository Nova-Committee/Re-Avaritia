package committee.nova.mods.avaritia.common.tile.collector;

import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.item.Item;

/**
 * Project: Avaritia
 * Author: cnlimiter
 * CreateTime: 2024/7/21 上午1:17
 * Description:
 */
public enum CollectorTier {
    DEFAULT("neutron_collector", ModItems.neutron_pile.get(), 3600),
    DENSE("dense_neutron_collector", ModItems.neutron_nugget.get(), 3600),
    DENSER("denser_neutron_collector", ModItems.neutron_ingot.get(), 3600),
    DENSEST("densest_neutron_collector", ModItems.neutron_ingot.get(), 200);

    public final int production_ticks;
    public final Item production;
    public final String name;

    CollectorTier(String name, Item production, int production_ticks) {
        this.production_ticks = production_ticks;
        this.production = production;
        this.name = name;
    }
}
