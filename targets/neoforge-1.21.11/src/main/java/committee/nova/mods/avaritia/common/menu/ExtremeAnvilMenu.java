package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.NotNull;

/**
 * 复用 26.1 铁砧算法和 NeoForge 钩子，同时移除经验、过高费用和铁砧损坏限制。
 */
public class ExtremeAnvilMenu extends AnvilMenu {
    public ExtremeAnvilMenu(int containerId, Inventory playerInventory, FriendlyByteBuf ignored) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public ExtremeAnvilMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(containerId, playerInventory, access);
    }

    @Override
    public @NotNull MenuType<?> getType() {
        return ModMenus.extreme_anvil.get();
    }

    @Override
    protected boolean mayPickup(Player player, boolean hasStack) {
        return hasStack;
    }

    /**
     * NeoForge 26.1 将公开的 createResult() 固定为 final，并提供此钩子供子类定制。
     * 临时使用原版的无限材料分支可保留完整算法，同时解除 40 级上限和附魔兼容限制。
     */
    @Override
    protected void createResultInternal() {
        boolean previousInstabuild = this.player.getAbilities().instabuild;
        this.player.getAbilities().instabuild = true;
        try {
            super.createResultInternal();
        } finally {
            this.player.getAbilities().instabuild = previousInstabuild;
        }
    }

    @Override
    protected void onTake(@NotNull Player player, @NotNull ItemStack output) {
        ItemStack left = this.inputSlots.getItem(0).copy();
        ItemStack right = this.inputSlots.getItem(1).copy();
        if (CommonHooks.fireAnvilCraftPre(this, player, output, left, right).isCanceled()) {
            return;
        }

        this.inputSlots.setItem(0, ItemStack.EMPTY);
        if (this.repairItemCountCost > 0) {
            ItemStack material = this.inputSlots.getItem(1);
            if (!material.isEmpty() && material.getCount() > this.repairItemCountCost) {
                material.shrink(this.repairItemCountCost);
                this.inputSlots.setItem(1, material);
            } else {
                this.inputSlots.setItem(1, ItemStack.EMPTY);
            }
        } else {
            this.inputSlots.setItem(1, ItemStack.EMPTY);
        }

        this.repairItemCountCost = 0;
        this.setCost(0);
        this.access.execute((level, blockPos) ->
                level.playSound(null, blockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F));
        CommonHooks.fireAnvilCraftPost(this, player, output, left, right);
    }
}
