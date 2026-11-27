package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.init.registry.ModItems;

import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/25 19:29
 * @Description:
 */
public class RefinedCoalItem extends ResourceItem{
    public RefinedCoalItem() {
        // 26.3 起燃料由物品组件 COOKING_FUEL 驱动（原 NeoForge getBurnTime 扩展已移除）
        super(ModRarities.UNCOMMON, true, ModItems.properties().stacksTo(32)
                .component(DataComponents.COOKING_FUEL,
                        new CookingFuel(new ResolvableInt.Constant(BURN_TIME),
                                ResolvableFloat.fromKey(ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER))));
    }

    public static final int BURN_TIME = 16000 * 10;

}
