package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InfinityElytraItem extends ElytraItem implements FabricElytraItem {
    public InfinityElytraItem() {
        super(new Item.Properties()
                .rarity(ModRarities.COSMIC)
                .fireResistant()
                .stacksTo(1));
    }

    @Override
    public void doVanillaElytraTick(LivingEntity entity, ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().getBoolean("Unbreakable")) {
            stack.getOrCreateTag().putBoolean("Unbreakable", true);
        }
    }
}
