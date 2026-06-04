package com.avaritia.compat.curios;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.avaritia.init.registry.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.List;

/** 为物品附加Curios能力，以及其他与Curios强相关的功能 */
public class AvaritiaCuriosPlugin {

    // 为物品附加饰品能力
    public static void registerCapabilities(final RegisterCapabilitiesEvent evt) {
        // 水晶铲子
        evt.registerItem(
                CuriosCapability.ITEM,
                (stack, context) -> new ICurio() {
                    @Override
                    public ItemStack getStack() {
                        return stack; // 必须返回传入的stack
                    }

                    @Override
                    public void curioTick(SlotContext slotContext) {
                        LivingEntity entity = slotContext.entity();
                        if (entity instanceof Player player && !player.level().isClientSide()) {
                            player.addEffect(new MobEffectInstance(MobEffects.HASTE, -1, 2, false, true));
                            player.addEffect(new MobEffectInstance(MobEffects.SPEED, -1, 2, false, true));

                            List<MobEffectInstance> effects = Lists.newArrayList(player.getActiveEffects());
                            for (MobEffectInstance potion : Collections2.filter(effects, potion ->
                                    (potion.getEffect().equals(MobEffects.SLOWNESS) ||
                                            potion.getEffect().equals(MobEffects.MINING_FATIGUE)))) {
                                player.removeEffect(potion.getEffect());
                            }
                        }
                    }
                },
                ModItems.crystal_shovel
        );
    }
}
