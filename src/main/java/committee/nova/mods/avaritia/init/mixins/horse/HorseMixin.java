package committee.nova.mods.avaritia.init.mixins.horse;

import committee.nova.mods.avaritia.api.iface.IChestHorse;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AbstractHorseMixin
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/5 22:13
 */
@Mixin(Horse.class)
public abstract class HorseMixin extends AbstractHorse implements IChestHorse {

    protected HorseMixin(EntityType<? extends AbstractHorse> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow
    protected abstract void updateContainerEquipment();

    @Shadow
    public abstract boolean isArmor(@NotNull ItemStack pStack);

    @Unique
    @Override
    public boolean avaritia_forge$hasChest() {
        return this.inventory != null && this.inventory.getItem(1).is(ModItems.infinity_horse_armor.get());
    }

    @Override
    protected int getInventorySize() {
        return 17;
    }

    @Override
    public void containerChanged(@NotNull Container pInvBasic) {
        super.containerChanged(pInvBasic);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addSave(CompoundTag pCompound, CallbackInfo ci) {
        ListTag listtag = new ListTag();

        for (int i = 2; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty()) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Slot", (byte) i);
                itemstack.save(compoundtag);
                listtag.add(compoundtag);
            }
        }

        pCompound.put("ChestedHorseItems", listtag);

    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readSave(CompoundTag pCompound, CallbackInfo ci) {
        this.createInventory();
        ListTag listtag = pCompound.getList("ChestedHorseItems", 10);

        for (int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            if (j >= 2 && j < this.inventory.getContainerSize()) {
                this.inventory.setItem(j, ItemStack.of(compoundtag));
            }
        }

        this.updateContainerEquipment();
    }

    @Unique
    @Override
    public int avaritia_forge$getInventoryColumns() {
        return 5;
    }

}
