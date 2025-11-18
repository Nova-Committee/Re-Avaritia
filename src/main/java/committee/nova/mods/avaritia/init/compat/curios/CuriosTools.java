package committee.nova.mods.avaritia.init.compat.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Predicate;

public class CuriosTools {

    public static ItemStack getFirstItemFromCuriosInv(Player player, Predicate<ItemStack> is) {
        return CuriosApi.getCuriosInventory(player)
                .map(curiosInventory -> curiosInventory.findCurios(is).get(0).stack())
                .orElse(ItemStack.EMPTY);
    }

//    public static ICapabilityProvider getIDKCuriosProvider(ItemStack stack) {
//
//        return CuriosApi.createCurioProvider(new ICurio() {
//
//            @Override
//            public ItemStack getStack() {
//                return stack;
//            }
//
//            @Override
//            public void curioTick(SlotContext slotContext) {
//                LivingEntity entity = slotContext.entity();
//                if (entity instanceof Player player && !player.level().isClientSide) {
//                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, -1, 2, false, true));
//                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 2, false, true));
//
//                    List<MobEffectInstance> effects = Lists.newArrayList(player.getActiveEffects());
//                    for (MobEffectInstance potion : Collections2.filter(effects, potion ->
//                            (potion.getEffect().equals(MobEffects.MOVEMENT_SLOWDOWN) ||
//                                    potion.getEffect().equals(MobEffects.DIG_SLOWDOWN)))) {
//                        player.removeEffect(potion.getEffect());
//                    }
//                }
//            }
//
//            @Override
//            public boolean canEquip(SlotContext slotContext) {
//                return true;
//            }
//
//            @Override
//            public boolean canUnequip(SlotContext slotContext) {
//                return true;
//            }
//        });
//
//    }
}
