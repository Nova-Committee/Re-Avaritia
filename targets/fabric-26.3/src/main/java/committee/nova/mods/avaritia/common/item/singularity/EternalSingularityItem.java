package committee.nova.mods.avaritia.common.item.singularity;

import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * EternalSingularityItem
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/13 下午7:52
 */
public class EternalSingularityItem extends Item {
    public EternalSingularityItem() {
        super(ModItems.properties().stacksTo(16).rarity(ModRarities.RARE));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }
}
