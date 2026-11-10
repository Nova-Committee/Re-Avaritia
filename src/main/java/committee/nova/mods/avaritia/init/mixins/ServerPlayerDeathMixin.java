package committee.nova.mods.avaritia.init.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import committee.nova.mods.avaritia.api.iface.IInfinitySwordDeath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDeathMixin implements IInfinitySwordDeath {
    @Unique
    private DamageSource avaritia$infinitySwordDamage;

    @Override
    public void avaritia$dieFromInfinitySword(DamageSource source) {
        this.avaritia$infinitySwordDamage = source;
        try {
            ((ServerPlayer) (Object) this).die(source);
        } finally {
            this.avaritia$infinitySwordDamage = null;
        }
    }

    @WrapOperation(method = "die", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraftforge/common/ForgeHooks;onLivingDeath(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private boolean avaritia$completeInfinitySwordDeath(LivingEntity victim, DamageSource source, Operation<Boolean> original) {
        boolean canceled = original.call(victim, source);
        if (source == this.avaritia$infinitySwordDamage) {
            if (victim.isRemoved() || victim.dead) {
                return true;
            }
            // Notify death observers once, then retain the sword's terminal damage contract.
            victim.setHealth(0.0F);
            return false;
        }
        return canceled;
    }
}
