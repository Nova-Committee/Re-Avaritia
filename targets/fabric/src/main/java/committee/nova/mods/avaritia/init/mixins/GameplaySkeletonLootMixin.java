package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.SkullDropTracker;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class GameplaySkeletonLootMixin implements SkullDropTracker {
    @Shadow protected int lastHurtByPlayerTime;
    @Unique private boolean avaritia$skullDropped;
    @Override public void avaritia$recordSkullDrop() { avaritia$skullDropped = true; }
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void avaritia$beginLoot(DamageSource source, CallbackInfo ci) { avaritia$skullDropped = false; }
    @Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
    private void avaritia$blazeSkull(DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof AbstractSkeleton skeleton && lastHurtByPlayerTime > 0
                && !avaritia$skullDropped && source.getEntity() instanceof Player player
                && (player.getMainHandItem().is(ModItems.blaze_sword.get()) || player.getOffhandItem().is(ModItems.blaze_sword.get()))) {
            skeleton.spawnAtLocation(Items.WITHER_SKELETON_SKULL);
        }
    }
}
