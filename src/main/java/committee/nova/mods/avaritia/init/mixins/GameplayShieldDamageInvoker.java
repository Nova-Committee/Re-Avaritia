package committee.nova.mods.avaritia.init.mixins;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface GameplayShieldDamageInvoker {
    @Invoker("hurtCurrentlyUsedShield")
    void avaritia$hurtShield(float damage);
}
