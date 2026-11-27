package committee.nova.mods.avaritia.mixin;

import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinitySpearItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.ApplyExhaustion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ApplyExhaustion.class)
public abstract class ApplyExhaustionMixin {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void skipInfinityLungeExhaustion(ServerLevel serverLevel, int enchantmentLevel,
                                             EnchantedItemInUse item, Entity entity, Vec3 position,
                                             CallbackInfo ci) {
        if (entity instanceof Player
                && item.itemStack().getItem() instanceof InfinitySpearItem
                && ISwitchable.isMode(item.itemStack(), "infinity_spear_lunge")) {
            ci.cancel();
        }
    }
}