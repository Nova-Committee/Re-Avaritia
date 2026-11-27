package committee.nova.mods.avaritia.init.registry.enums;

import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/21 上午1:17
 * @Description:
 */
public enum CollectorTier {
    DEFAULT("neutron_collector", Ingredient.of(ModTags.NEUTRON_DUST), 3600),
    DENSE("dense_neutron_collector", Ingredient.of(ModTags.NEUTRON_NUGGET), 3600),
    DENSER("denser_neutron_collector", Ingredient.of(ModTags.NEUTRON_INGOT), 3600),
    DENSEST("densest_neutron_collector", Ingredient.of(ModTags.NEUTRON_BLOCK_ITEM), 200);

    public final int production_ticks;
    public final Ingredient production;
    public final String name;

    CollectorTier(String name, Ingredient production, int production_ticks) {
        this.production_ticks = production_ticks;
        this.production = production;
        this.name = name;
    }
}
