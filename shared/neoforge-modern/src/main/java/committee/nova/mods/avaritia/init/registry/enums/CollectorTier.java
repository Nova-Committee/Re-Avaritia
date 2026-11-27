package committee.nova.mods.avaritia.init.registry.enums;

import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

/**
 * 中子收集器等级参数。
 */
public enum CollectorTier {
    DEFAULT("neutron_collector", ModItems.neutron_pile, 3600),
    DENSE("dense_neutron_collector", ModItems.neutron_nugget, 3600),
    DENSER("denser_neutron_collector", ModItems.neutron_ingot, 3600),
    DENSEST("densest_neutron_collector", ModBlocks.neutron, 200);

    public final int production_ticks;
    public final Supplier<? extends ItemLike> production;
    public final String name;

    CollectorTier(String name, Supplier<? extends ItemLike> production, int production_ticks) {
        this.production_ticks = production_ticks;
        this.production = production;
        this.name = name;
    }

    public ItemStack createProductionStack() {
        return new ItemStack(this.production.get());
    }
}
