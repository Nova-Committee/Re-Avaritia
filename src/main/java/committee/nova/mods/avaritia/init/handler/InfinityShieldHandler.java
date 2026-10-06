package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityShieldItem;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class InfinityShieldHandler {
    private InfinityShieldHandler() {}
    public static boolean preventDamage(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof Player player) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.is(ModDamageTypes.INFINITY)) return false;
        ItemStack shield = heldInfinityShield(player);
        if (!shield.isEmpty() && InfinityShieldItem.isDefiniteDefendingMode(shield)) {
            player.hurtTime = 0;
            player.deathTime = 0;
            return true;
        }
        return false;
    }
    public static void onShieldBlock(LivingEntity entity, DamageSource source, float blocked) {
        if (!(entity instanceof Player player) || !player.getUseItem().is(ModItems.infinity_shield.get())
                || !InfinityShieldItem.isDefendingMode(player.getUseItem()) || !(player.level() instanceof ServerLevel level)) return;
        Entity attacker = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
        LivingEntity target = attacker instanceof LivingEntity living ? living : attacker instanceof EnderDragonPart part ? part.parentMob : null;
        if (target == null || target == player) return;
        target.invulnerableTime = 0;
        level.getServer().execute(() -> {
            if (target.isAlive()) target.hurt(ModDamageTypes.causeRandomDamage(level, player), blocked);
        });
    }
    public static boolean reflectProjectile(Projectile projectile, HitResult result) {
        if (projectile.level().isClientSide || !(result instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof Player player)) return false;
        ItemStack shield = heldInfinityShield(player);
        if (shield.isEmpty() || !InfinityShieldItem.isDefendingMode(shield)) return false;
        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-0.9D));
        return true;
    }
    public static boolean preventFall(Player player) {
        ItemStack shield = heldInfinityShield(player);
        return !shield.isEmpty() && InfinityShieldItem.isFloatMode(shield);
    }
    public static void floatTick(Player player) {
        if (!preventFall(player) || player.getAbilities().flying || player.isSwimming()) return;
        Vec3 motion = player.getDeltaMovement();
        if ((player.isInWater() || player.isInLava()) && motion.y <= 0D) {
            player.setDeltaMovement(motion.x, 0D, motion.z);
            player.setOnGround(true);
            player.fallDistance = 0F;
        } else if (!player.onGround()) {
            if (player.isCrouching()) {
                player.setDeltaMovement(motion.x, 0D, motion.z);
                player.fallDistance = 0F;
            } else if (motion.y < 0D) player.setDeltaMovement(motion.x, -0.15D, motion.z);
        }
    }
    private static ItemStack heldInfinityShield(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.is(ModItems.infinity_shield.get())) return main;
        ItemStack off = player.getOffhandItem();
        return off.is(ModItems.infinity_shield.get()) ? off : ItemStack.EMPTY;
    }
}
