package com.avaritia.init.handler;



import com.avaritia.Const;
import com.avaritia.api.iface.item.ISwitchable;
import com.avaritia.api.utils.ItemUtils;
import com.avaritia.common.item.misc.InfinityUmbrellaItem;
import com.avaritia.common.item.resources.MatterClusterItem;
import com.avaritia.common.item.tools.infinity.InfinityCrossBowItem;
import com.avaritia.init.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * FIXME: MC 26.1.2 removed ItemProperties and ItemPropertyFunction APIs.
 * The item model property/override system was completely restructured;
 * ItemProperties.register() no longer exists.
 * Need to migrate to the new ItemModelResolver / ConditionalItemModelProperty system.
 * See https://docs.neoforged.net/ for migration guide.
 *
 * Original code registered model property overrides for:
 * - infinity_pickaxe, infinity_shovel, infinity_clock mode toggles
 * - infinity_bow, crystal_bow, blaze_bow pull/pulling/tracer states
 * - infinity_crossbow pull/charged states
 * - infinity_sword kill mode, infinity_shield blocking
 * - matter_cluster capacity indicator, infinity_umbrella mode
 */
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public class ItemOverrideHandler {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            setPropertyOverride(ModItems.infinity_pickaxe.get(), Const.rl("hammer"), (itemStack, world, livingEntity, d) -> {
                return ItemUtils.getOrCreateChildTag(itemStack, "mode").getBoolean("infinity_pickaxe_hammer") ? 1 : 0;
            });
            setPropertyOverride(ModItems.infinity_shovel.get(), Const.rl("destroyer"), (itemStack, world, livingEntity, d) -> {
                return ItemUtils.getOrCreateChildTag(itemStack, "mode").getBoolean("infinity_shovel_destroyer") ? 1 : 0;
            });
            setPropertyOverride(ModItems.infinity_clock.get(), Const.rl("up"), (itemStack, world, livingEntity, d) -> {
                return ItemUtils.getOrCreateChildTag(itemStack, "mode").getBoolean("infinity_clock_up") ? 1 : 0;
            });
            setPropertyOverride(ModItems.infinity_bow.get(), Const.rl("track"), (itemStack, world, livingEntity, d) -> {
                return ItemUtils.getOrCreateChildTag(itemStack, "mode").getBoolean("infinity_bow_tracer") ? 1 : 0;
            });
            setPropertyOverride(ModItems.infinity_sword.get(), Const.rl("kill"), (itemStack, world, livingEntity, d) -> {
                return ItemUtils.getOrCreateChildTag(itemStack, "mode").getBoolean("infinity_sword_kill") ? 1 : 0;
            });
            setPropertyOverride(ModItems.matter_cluster.get(), Const.rl("cap"), (itemStack, world, livingEntity, d) -> {
                return MatterClusterItem.getClusterSize(MatterClusterItem.getClusterItems(itemStack)) == MatterClusterItem.CAPACITY ? 1 : 0;
            });
            setPropertyOverride(ModItems.infinity_shield.get(), Const.rl("blocking"), (itemStack, world, livingEntity, d) -> {
                return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F;
            });
            ItemProperties.register(ModItems.infinity_umbrella.get(),
                    Const.rl("mode"),
                    (stack, world, entity, seed) -> {
                        return ISwitchable.getCurrentMode(stack, InfinityUmbrellaItem.MODES);
                    });
            setPropertyOverride(ModItems.infinity_bow.get(), Const.rl("pull"), (itemStack, world, livingEntity, d) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return CrossbowItem.isCharged(itemStack) ? 0.0F : (float) (itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(itemStack, livingEntity);
                }
            });
            setPropertyOverride(ModItems.infinity_bow.get(), Const.rl("pull"), (itemStack, world, livingEntity, d) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return CrossbowItem.isCharged(itemStack) ? 0.0F : (float) (itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(itemStack, livingEntity);
                }
            });
            setPropertyOverride(ModItems.crystal_bow.get(), Const.rl("pull"), (itemStack, world, livingEntity, d) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return CrossbowItem.isCharged(itemStack) ? 0.0F : (float) (itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(itemStack, livingEntity);
                }
            });
            setPropertyOverride(ModItems.blaze_bow.get(), Const.rl("pull"), (itemStack, world, livingEntity, d) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return CrossbowItem.isCharged(itemStack) ? 0.0F : (float) (itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(itemStack, livingEntity);
                }
            });
            setPropertyOverride(ModItems.infinity_bow.get(), Const.rl("pulling"), (itemStack, world, livingEntity, d) -> {
                return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && !CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F;
            });
            setPropertyOverride(ModItems.crystal_bow.get(), Const.rl("pulling"), (itemStack, world, livingEntity, d) -> {
                return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && !CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F;
            });
            setPropertyOverride(ModItems.blaze_bow.get(), Const.rl("pulling"), (itemStack, world, livingEntity, d) -> {
                return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && !CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F;
            });
            setPropertyOverride(ModItems.infinity_bow.get(), Const.rl("tracer"), (itemStack, world, livingEntity, d) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return CrossbowItem.isCharged(itemStack) && ItemUtils.getOrCreateChildTag(itemStack, "mode").getBoolean("infinity_bow_tracer") ? 0.0F : (float) (itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(itemStack, livingEntity);
                }
            });
            setPropertyOverride(ModItems.infinity_bow.get(), Const.rl("tracing"), (itemStack, world, livingEntity, d) -> {
                return livingEntity != null && livingEntity.isUsingItem()
                        && livingEntity.getUseItem() == itemStack && !CrossbowItem.isCharged(itemStack)
                        && ItemUtils.getOrCreateChildTag(itemStack, "mode").getBoolean("infinity_bow_tracer")
                        ? 1.0F : 0.0F;
            });


            setPropertyOverride(ModItems.infinity_crossbow.get(), Const.rl("pull"), (itemStack, world, livingEntity, d) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return InfinityCrossBowItem.isCharged(itemStack) ? 0.0F : (float) (itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) / 20;
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

    public static void setPropertyOverride(Item itemProvider, Identifier override, ItemPropertyFunction propertyGetter) {
        ItemProperties.register(itemProvider.asItem(), override, propertyGetter);
    }
}
