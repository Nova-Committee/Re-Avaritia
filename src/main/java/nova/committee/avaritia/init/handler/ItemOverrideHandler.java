package nova.committee.avaritia.init.handler;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.common.item.MatterClusterItem;
import nova.committee.avaritia.common.item.tools.BowInfinityItem;
import nova.committee.avaritia.common.item.tools.PickaxeInfinityItem;
import nova.committee.avaritia.common.item.tools.ShovelInfinityItem;
import nova.committee.avaritia.init.registry.ModItems;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 19:50
 * Version: 1.0
 */
@Mod.EventBusSubscriber(modid = Static.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemOverrideHandler {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> setPropertyOverride(ModItems.pick_axe, Static.rl("hammer"), (itemStack, world, livingEntity, d) -> {
            if (itemStack.getItem() instanceof PickaxeInfinityItem)
                return itemStack.getOrCreateTag().getBoolean("hammer") ? 1 : 0;
            return 0;
        }));
        event.enqueueWork(() -> setPropertyOverride(ModItems.matter_cluster, Static.rl("cap"), (itemStack, world, livingEntity, d) -> {
            return MatterClusterItem.getClusterSize(itemStack) == MatterClusterItem.CAPACITY ? 1 : 0;
        }));

        event.enqueueWork(() -> setPropertyOverride(ModItems.infinity_bow, Static.rl("pull"), (itemStack, world, livingEntity, d) -> {
            if (livingEntity == null) {
                return 0.0F;
            } else {
                return CrossbowItem.isCharged(itemStack) ? 0.0F : (float) (itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(itemStack);
            }
        }));
        event.enqueueWork(() -> setPropertyOverride(ModItems.infinity_bow, Static.rl("pulling"), (itemStack, world, livingEntity, d) -> {
            if (itemStack.getItem() instanceof BowInfinityItem)
                return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && !CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F;
            return 0;
        }));

        event.enqueueWork(() -> setPropertyOverride(ModItems.infinity_shovel, Static.rl("destroyer"), (itemStack, world, livingEntity, d) -> {
            if (itemStack.getItem() instanceof ShovelInfinityItem)
                return itemStack.getOrCreateTag().getBoolean("destroyer") ? 1 : 0;
            return 0;
        }));
    }

    public static void setPropertyOverride(Item itemProvider, ResourceLocation override, ItemPropertyFunction propertyGetter) {
        ItemProperties.register(itemProvider.asItem(), override, propertyGetter);
    }
}
