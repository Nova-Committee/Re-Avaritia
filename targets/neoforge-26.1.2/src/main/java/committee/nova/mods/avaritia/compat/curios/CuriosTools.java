package committee.nova.mods.avaritia.compat.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.function.Predicate;

public class CuriosTools {

    public static ItemStack getFirstItemFromCuriosInv(Player player, Predicate<ItemStack> filter) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(filter)
                        .map(SlotResult::stack)
                        .orElse(ItemStack.EMPTY))
                .orElse(ItemStack.EMPTY);
    }

    public static ItemStack getFirstItemFromCuriosSlot(Player player, String slotId, Predicate<ItemStack> filter) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findCurios(slotId).stream()
                        .map(SlotResult::stack)
                        .filter(filter)
                        .findFirst()
                        .orElse(ItemStack.EMPTY))
                .orElse(ItemStack.EMPTY);
    }
}
