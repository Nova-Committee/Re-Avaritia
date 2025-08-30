package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.iface.IFourModeSwitchable;
import committee.nova.mods.avaritia.common.item.resources.MatterClusterItem;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityCrossBowItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static committee.nova.mods.avaritia.common.item.misc.InfinityUmbrellaItem.MODE_KEY;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 19:50
 * Version: 1.0
 */
@Mod.EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemOverrideHandler {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            setPropertyOverride(ModItems.infinity_pickaxe.get(), Const.rl("hammer"), (itemStack, world, livingEntity, d) -> {
                return itemStack.getOrCreateTagElement("mode").getBoolean("infinity_pickaxe_hammer") ? 1 : 0;
            });
            setPropertyOverride(ModItems.infinity_shovel.get(), Const.rl("destroyer"), (itemStack, world, livingEntity, d) -> {
                return itemStack.getOrCreateTagElement("mode").getBoolean("infinity_shovel_destroyer") ? 1 : 0;
            });
            setPropertyOverride(ModItems.infinity_clock.get(), Const.rl("up"), (itemStack, world, livingEntity, d) -> {
                return itemStack.getOrCreateTagElement("mode").getBoolean("infinity_clock_up") ? 1 : 0;
            });
            setPropertyOverride(ModItems.infinity_bow.get(), Const.rl("track"), (itemStack, world, livingEntity, d) -> {
                return itemStack.getOrCreateTagElement("mode").getBoolean("infinity_bow_tracer") ? 1 : 0;
            });
//            setPropertyOverride(ModItems.matter_cluster.get(), Const.rl("cap"), (itemStack, world, livingEntity, d) -> {
//                return MatterClusterItem.getClusterSize(itemStack) == MatterClusterItem.CAPACITY ? 1 : 0;
//            });
            ItemProperties.register(ModItems.infinity_umbrella.get(),
                    new ResourceLocation("mode"),
                    (stack, world, entity, seed) -> {
                        return IFourModeSwitchable.getMode(stack, MODE_KEY);
                    });
            setPropertyOverride(ModItems.infinity_bow.get(), Const.rl("pull"), (itemStack, world, livingEntity, d) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return CrossbowItem.isCharged(itemStack) ? 0.0F : (float) (itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(itemStack);
                }
            });
            setPropertyOverride(ModItems.infinity_bow.get(), Const.rl("pulling"), (itemStack, world, livingEntity, d) -> {
                return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && !CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F;
            });

            setPropertyOverride(ModItems.infinity_bow.get(), Const.rl("tracer"), (itemStack, world, livingEntity, d) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return CrossbowItem.isCharged(itemStack) && itemStack.getOrCreateTagElement("mode").getBoolean("infinity_bow_tracer") ? 0.0F : (float) (itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(itemStack);
                }
            });
            setPropertyOverride(ModItems.infinity_bow.get(), Const.rl("tracing"), (itemStack, world, livingEntity, d) -> {
                return livingEntity != null && livingEntity.isUsingItem()
                        && livingEntity.getUseItem() == itemStack && !CrossbowItem.isCharged(itemStack)
                        && itemStack.getOrCreateTagElement("mode").getBoolean("infinity_bow_tracer")
                        ? 1.0F : 0.0F;
            });


            setPropertyOverride(ModItems.infinity_crossbow.get(), Const.rl("pull"), (itemStack, world, livingEntity, d) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return InfinityCrossBowItem.isCharged(itemStack) ? 0.0F : (float) (itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / 20;
                }
            });
            setPropertyOverride(Items.CROSSBOW, Const.rl("pulling"), (itemStack, level, livingEntity, i) -> {
                return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && !InfinityCrossBowItem.isCharged(itemStack) ? 1.0F : 0.0F;
            });
            setPropertyOverride(ModItems.infinity_crossbow.get(), Const.rl("charged"), (itemStack, world, livingEntity, d) -> {
                return InfinityCrossBowItem.isCharged(itemStack) ? 1.0F : 0.0F;
            });
        });


    }

    public static void setPropertyOverride(Item itemProvider, ResourceLocation override, ItemPropertyFunction propertyGetter) {
        ItemProperties.register(itemProvider.asItem(), override, propertyGetter);
    }
}
