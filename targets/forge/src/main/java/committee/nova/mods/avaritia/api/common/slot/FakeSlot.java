package committee.nova.mods.avaritia.api.common.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/11/22 01:42
 * @Description:
 */
public class FakeSlot extends Slot {
    public FakeSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    @Override
    public void set(@NotNull ItemStack pStack) {
    }

    @Override
    public void onTake(@NotNull Player pPlayer, @NotNull ItemStack pStack) {
    }

    @Override
    public @NotNull ItemStack remove(int pAmount) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull Optional<ItemStack> tryRemove(int pCount, int pDecrement, @NotNull Player pPlayer) {
        return Optional.of(ItemStack.EMPTY);
    }

    @Override
    public @NotNull ItemStack safeInsert(@NotNull ItemStack pStack, int pIncrement) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack safeTake(int pCount, int pDecrement, @NotNull Player pPlayer) {
        return ItemStack.EMPTY;
    }

    @Override
    public void onQuickCraft(@NotNull ItemStack pOldStack, @NotNull ItemStack pNewStack) {
    }

    @Override
    public void setChanged() {
    }
}