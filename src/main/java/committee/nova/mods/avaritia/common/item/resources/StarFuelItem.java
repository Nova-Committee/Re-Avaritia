package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.init.registry.ModItems;

import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/18 17:30
 * Version: 1.0
 */
public class StarFuelItem extends ResourceItem {


    public static final int BURN_TIME = Integer.MAX_VALUE;

    public StarFuelItem() {
        // 26.3 起燃料由物品组件 COOKING_FUEL 驱动（原 NeoForge getBurnTime 扩展已移除）
        super(ModRarities.RARE, true, ModItems.properties().stacksTo(16)
                .component(DataComponents.COOKING_FUEL,
                        new CookingFuel(new ResolvableInt.Constant(BURN_TIME),
                                ResolvableFloat.fromKey(ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER))));
    }

}
