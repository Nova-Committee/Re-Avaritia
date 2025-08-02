package committee.nova.mods.avaritia.init.registry.enums;

import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @author: cnlimiter
 */
public enum CompressorTier {
    DEFAULT("neutron_compressor", Ingredient.of(ModTags.NEUTRON_DUST), 3600),
    DENSE("dense_neutron_compressor", Ingredient.of(ModTags.NEUTRON_NUGGET), 3600),
    DENSER("denser_neutron_compressor", Ingredient.of(ModTags.NEUTRON_INGOT), 3600),
    DENSEST("densest_neutron_compressor", Ingredient.of(ModTags.NEUTRON_INGOT), 200);

    public final int production_ticks;
    public final Ingredient production;
    public final String name;

    CompressorTier(String name, Ingredient production, int production_ticks) {
        this.production_ticks = production_ticks;
        this.production = production;
        this.name = name;
    }
}
