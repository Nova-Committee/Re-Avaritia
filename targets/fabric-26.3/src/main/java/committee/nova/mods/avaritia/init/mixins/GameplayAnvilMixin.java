package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityTridentItem;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class GameplayAnvilMixin {
    @Shadow @Final private DataSlot cost;
    @Inject(method = "createResult", at = @At("RETURN"))
    private void avaritia$rejectTridentBooks(CallbackInfo ci) {
        AnvilMenu menu = (AnvilMenu) (Object) this;
        if (menu.getSlot(0).getItem().getItem() instanceof InfinityTridentItem && menu.getSlot(1).getItem().is(Items.ENCHANTED_BOOK)) {
            menu.getSlot(2).set(ItemStack.EMPTY);
            cost.set(0);
            menu.broadcastChanges();
        }
    }
}
