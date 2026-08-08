package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityShieldItem;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.InfinityDamageUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 无尽盾三种模式：
 * 防御：举盾时反弹近战伤害并自动弹开弹射物。
 * 终极防御：无需举盾，免除一切外部伤害。
 * 漂浮：水/岩浆面行走，潜行悬停，缓慢下落。
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public final class InfinityShieldHandler {
    private InfinityShieldHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDefiniteDefendingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        DamageSource source = event.getSource();
        // 保留虚空、/kill 等绕过无敌的伤害，也保留模组自己的无尽伤害，避免反射递归。
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.is(ModDamageTypes.INFINITY)) {
            return;
        }

        ItemStack shield = heldInfinityShield(player);
        if (!shield.isEmpty() && InfinityShieldItem.isDefiniteDefendingMode(shield)) {
            event.setCanceled(true);
            player.hurtTime = 0;
            player.deathTime = 0;
        }
    }

    @SubscribeEvent
    public static void onDefendingShieldBlock(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof Player player) || !event.getBlocked()) {
            return;
        }

        ItemStack shield = player.getItemBlockingWith();
        if (!shield.is(ModItems.infinity_shield.get()) || !InfinityShieldItem.isDefendingMode(shield)) {
            return;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity attacker = event.getDamageSource().getEntity();
        if (attacker == null) {
            attacker = event.getDamageSource().getDirectEntity();
        }

        LivingEntity target = InfinityDamageUtils.resolveLivingTarget(attacker);
        if (target == null || target == player) {
            return;
        }

        target.invulnerableTime = 0;
        target.hurtServer(serverLevel, ModDamageTypes.source(player), event.getBlockedDamage());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDefendingProjectile(ProjectileImpactEvent event) {
        if (event.getProjectile().level().isClientSide()) {
            return;
        }

        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) {
            return;
        }

        if (!(hit.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack shield = heldInfinityShield(player);
        if (shield.isEmpty() || !InfinityShieldItem.isDefendingMode(shield)) {
            return;
        }

        Projectile projectile = event.getProjectile();
        projectile.setDeltaMovement(projectile.getDeltaMovement().reverse().scale(0.9));
        event.setCanceled(true);
    }

    private static ItemStack heldInfinityShield(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(ModItems.infinity_shield.get())) {
            return mainHand;
        }

        ItemStack offhand = player.getOffhandItem();
        return offhand.is(ModItems.infinity_shield.get()) ? offhand : ItemStack.EMPTY;
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFloatFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack shield = heldInfinityShield(player);
        if (shield.isEmpty() || !InfinityShieldItem.isFloatMode(shield)) {
            return;
        }

        event.setCanceled(true);
        event.setDistance(0.0F);
        event.setDamageMultiplier(0.0F);
        player.fallDistance = 0.0F;
    }

    @SubscribeEvent
    public static void onFloatTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        ItemStack shield = heldInfinityShield(player);
        if (shield.isEmpty() || !InfinityShieldItem.isFloatMode(shield)) {
            return;
        }

        // 创造飞行、游泳时不干扰。
        if (player.getAbilities().flying || player.isSwimming()) {
            return;
        }

        Vec3 motion = player.getDeltaMovement();

        // 水面 / 岩浆表面行走：停止下沉，并视为站在地面上。
        if ((player.isInWater() || player.isInLava()) && motion.y() <= 0.0D) {
            player.setDeltaMovement(motion.x(), 0.0D, motion.z());
            player.setOnGround(true);
            player.fallDistance = 0.0F;
            return;
        }

        // 空中：潜行悬停，否则缓慢下落。
        if (!player.onGround()) {
            if (player.isCrouching()) {
                player.setDeltaMovement(motion.x(), 0.0D, motion.z());
                player.fallDistance = 0.0F;
            } else if (motion.y() < 0.0D) {
                player.setDeltaMovement(motion.x(), -0.15D, motion.z());
            }
        }
    }
}