package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.common.item.tools.InfinityBowItem;
import committee.nova.mods.avaritia.common.item.tools.InfinityPickaxeItem;
import committee.nova.mods.avaritia.common.item.tools.InfinityShovelItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.CrossbowItem;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 19:50
 * Version: 1.0
 */
public class ItemOverrideHandler {

    public static void init() {
        ItemProperties.register(ModItems.matter_cluster.get(), Static.rl("cap"), (itemStack, world, livingEntity, d) -> MatterClusterItem.getClusterSize(itemStack) == MatterClusterItem.CAPACITY ? 1 : 0);

        ItemProperties.register(ModItems.infinity_pickaxe.get(), Static.rl("hammer"), (itemStack, world, livingEntity, d) -> {
            if (itemStack.getItem() instanceof InfinityPickaxeItem)
                return itemStack.getOrCreateTag().getBoolean("hammer") ? 1 : 0;
            return 0;
        });
        ItemProperties.register(ModItems.infinity_bow.get(), Static.rl("tracer"), (itemStack, world, livingEntity, d) -> {
            if (itemStack.getItem() instanceof InfinityBowItem)
                return itemStack.getOrCreateTag().getBoolean("tracer") ? 1 : 0;
            return 0;
        });
        ItemProperties.register(ModItems.infinity_bow.get(), Static.rl("pull"), (itemStack, world, livingEntity, d) -> {
            if (livingEntity == null) {
                return 0.0F;
            } else {
                return CrossbowItem.isCharged(itemStack) ? 0.0F : (float) (itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(itemStack);
            }
        });
        ItemProperties.register(ModItems.infinity_bow.get(), Static.rl("pulling"), (itemStack, world, livingEntity, d) -> {
            if (itemStack.getItem() instanceof InfinityBowItem)
                return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && !CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F;
            return 0;
        });

        ItemProperties.register(ModItems.infinity_shovel.get(), Static.rl("destroyer"), (itemStack, world, livingEntity, d) -> {
            if (itemStack.getItem() instanceof InfinityShovelItem)
                return itemStack.getOrCreateTag().getBoolean("destroyer") ? 1 : 0;
            return 0;
        });
    }
}
