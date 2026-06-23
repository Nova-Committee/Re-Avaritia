package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.init.compat.curios.CuriosTools;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.Const;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/**
 * Seperate logics of infinity elytra checking from single chest to both curios and equipment
 * @author  HowXu <dev@howxu.cn>
 */
public class InfinityElytraUtils {
    public static boolean hasInfinityElytraEquipped(Player player) {
        return !getInfinityElytraStack(player).isEmpty();
    }

    public static ItemStack getInfinityElytraStack(Player player) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (isInfinityElytra(chestStack)) {
            return chestStack;
        }
        return findInfinityElytraInCurios(player);
    }

    private static ItemStack findInfinityElytraInCurios(Player player) {
        if (!Const.curios) {
            return ItemStack.EMPTY;
        }
        return CuriosTools.getFirstItemFromCuriosInv(player, InfinityElytraUtils::isInfinityElytra);
    }

    private static boolean isInfinityElytra(ItemStack stack) {
        return stack.is(ModItems.infinity_elytra.get());
    }
}
