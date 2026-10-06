package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class GameplayItemEntityMixin {
    @Shadow private java.util.UUID target;
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void avaritia$immortality(CallbackInfo ci) {
        ItemEntity entity = (ItemEntity) (Object) this;
        if (!InfinityHandler.isImmortal(entity.getItem())) return;
        entity.setInvulnerable(true);
        entity.setUnlimitedLifetime();
        if (!entity.level().isClientSide && !(entity instanceof ImmortalItemEntity)) {
            ImmortalItemEntity replacement = ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), entity.level(), entity, entity.getItem());
            if (replacement != null) {
                entity.discard();
                entity.level().addFreshEntity(replacement);
                ci.cancel();
            }
        }
    }
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void avaritia$noItemDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (InfinityHandler.isImmortal(((ItemEntity) (Object) this).getItem())) cir.setReturnValue(false);
    }
    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void avaritia$clusterPickup(Player player, CallbackInfo ci) {
        ItemEntity entity = (ItemEntity) (Object) this;
        if (!entity.level().isClientSide && !entity.hasPickUpDelay()
                && (target == null || target.equals(player.getUUID()))) InfinityHandler.clusterCluster(player, entity);
    }
}
