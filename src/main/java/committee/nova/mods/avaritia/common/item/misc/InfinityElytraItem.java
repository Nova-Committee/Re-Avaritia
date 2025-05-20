package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InfinityElytraItem extends ElytraItem {
    public InfinityElytraItem() {
        super(new Item.Properties()
                .rarity(ModRarities.COSMIC.getValue())
                .fireResistant()
                .stacksTo(1));
    }

    @Override
    public boolean canElytraFly(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return true;
    }
}
