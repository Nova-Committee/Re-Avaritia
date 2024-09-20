package committee.nova.mods.avaritia.common.inventory.slot;

import committee.nova.mods.avaritia.util.ItemUtils;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/9/21 01:55
 * @Description:
 */
public class BlackListSlot extends Slot {
    private final ItemStack item;
    public BlackListSlot(Container pContainer, int pSlot, int pX, int pY, ItemStack item) {
        super(pContainer, pSlot, pX, pY);
        this.item = item;
    }

    @Override
    public boolean mayPickup(@NotNull Player pPlayer) {
        if (ItemUtils.areStacksSameType(item, this.getItem())) return false;
        else  return super.mayPickup(pPlayer);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if (ItemUtils.areStacksSameType(item, this.getItem())) return false;
        else  return super.mayPlace(stack);
    }
}
