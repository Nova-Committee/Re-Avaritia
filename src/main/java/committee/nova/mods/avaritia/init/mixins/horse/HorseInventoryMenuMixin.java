package committee.nova.mods.avaritia.init.mixins.horse;

import committee.nova.mods.avaritia.api.iface.IChestHorse;
import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HorseInventoryMenuMixin
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/5 22:42
 */
@Mixin(HorseInventoryMenu.class)
public abstract class HorseInventoryMenuMixin extends AbstractContainerMenu {

    @Shadow @Final private AbstractHorse horse;

    @Shadow @Final private Container horseContainer;

    protected HorseInventoryMenuMixin(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }




//    @Inject(method = "<init>", at = @At(value = "TAIL"))
//    public void init(int pContainerId, Inventory pPlayerInventory, Container pContainer, AbstractHorse pHorse, CallbackInfo ci){
//        if (this.avaritia_forge$hasChest(pHorse)) {
//            for(int k = 0; k < 3; ++k) {
//                for(int l = 0; l < ((IChestHorse)pHorse).avaritia_forge$getInventoryColumns(); ++l) {
//                    this.addSlot(new Slot(pContainer, 2 + l + k * ((IChestHorse)pHorse).avaritia_forge$getInventoryColumns(), 80 + l * 18, 18 + k * 18));
//                }
//            }
//        }
//    }

    @Unique
    public boolean avaritia_forge$hasChest(AbstractHorse pHorse){
        return pHorse instanceof IChestHorse chestHorse && chestHorse.avaritia_forge$hasChest();
    }

    @Override
    public void broadcastChanges() {
        if (avaritia_forge$hasChest(this.horse)){
            for(int k = 0; k < 3; ++k) {
                for(int l = 0; l < ((IChestHorse)this.horse).avaritia_forge$getInventoryColumns(); ++l) {
                    this.addSlot(new Slot(this.horseContainer, 2 + l + k * ((IChestHorse)this.horse).avaritia_forge$getInventoryColumns(), 80 + l * 18, 18 + k * 18));
                }
            }
        }
        else {
            for(int k = 0; k < 3; ++k) {
                for(int l = 0; l < ((IChestHorse)this.horse).avaritia_forge$getInventoryColumns(); ++l) {
                    this.avaritia_forge$removeSlot(new Slot(this.horseContainer, 2 + l + k * ((IChestHorse)this.horse).avaritia_forge$getInventoryColumns(), 80 + l * 18, 18 + k * 18));
                }
            }
        }
        super.broadcastChanges();

    }

    @Unique
    protected Slot avaritia_forge$removeSlot(Slot pSlot) {
        this.slots.remove(pSlot);
        return pSlot;
    }
}
