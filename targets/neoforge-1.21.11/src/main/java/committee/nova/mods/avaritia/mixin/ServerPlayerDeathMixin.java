package committee.nova.mods.avaritia.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import committee.nova.mods.avaritia.util.InfinityDamageUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDeathMixin {
    @WrapOperation(method = "die", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/neoforged/neoforge/common/CommonHooks;onLivingDeath(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private boolean avaritia$completeInfinityDeath(LivingEntity victim, DamageSource source, Operation<Boolean> original) {
        boolean canceled = original.call(victim, source);
        if (InfinityDamageUtils.completePlayerDeath(victim, source)) {
            if (victim.isRemoved() || victim.dead) return true;
            return false;
        }
        return canceled;
    }
}
