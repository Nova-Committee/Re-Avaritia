package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.tools.InfinityArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public abstract class GameplayPiglinAiMixin {
    // 26.3: isWearingGold was renamed to isWearingSafeArmor (PIGLIN_SAFE_ARMOR tag driven).
    @Inject(method = "isWearingSafeArmor", at = @At("HEAD"), cancellable = true)
    private static void avaritia$neutralArmor(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        // 26.3 删除 getArmorSlots，改用 EquipmentSlotGroup.ARMOR 遍历（与 vanilla isWearingSafeArmor 一致）
        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
            if (entity.getItemBySlot(slot).getItem() instanceof InfinityArmorItem) { cir.setReturnValue(true); return; }
        }
    }
}
