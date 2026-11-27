package committee.nova.mods.avaritia.init.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps the neutron horse armor readout within the vanilla display range. */
@Mixin(LivingEntity.class)
public abstract class ClientHorseArmorValueMixin {
    @Inject(method = "getArmorValue", at = @At("RETURN"), cancellable = true)
    private void avaritia$limitHorseArmorDisplay(CallbackInfoReturnable<Integer> cir) {
        // A physical client can also contain a logical server.
        if (cir.getReturnValueI() <= 30 || !((Object) this instanceof Horse horse) || !horse.level().isClientSide) {
            return;
        }

        // The armor attribute can arrive before the equipment stack on the client.
        AttributeInstance armor = horse.getAttribute(Attributes.ARMOR);
        AttributeModifier modifier = armor == null ? null : armor.getModifier(ClientHorseArmorAccessor.avaritia$armorModifierId());
        if (modifier != null && modifier.getAmount() == Integer.MAX_VALUE
                && modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
            cir.setReturnValue(30);
        }
    }
}
