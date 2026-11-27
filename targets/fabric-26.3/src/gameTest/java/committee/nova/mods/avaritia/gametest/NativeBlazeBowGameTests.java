package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.common.entity.arrow.BurningArrowEntity;
import committee.nova.mods.avaritia.common.entity.ball.BurningBallEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/** Real bow use/release, registered server entities and vanilla projectile collision ticks. */
public final class NativeBlazeBowGameTests {
    private static final String TEMPLATE = "avaritia:portable_ui_empty";
    private static final String BURNING_MODE = "blaze_bow_burning";

    private NativeBlazeBowGameTests() {}

    @GameTest(template = TEMPLATE)
    public static void ordinaryReleaseSpawnsOwnedArrowAndFliesThroughAir(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        try {
            ItemStack bow = player.getMainHandItem();
            release(helper, player, 20);
            BurningArrowEntity arrow = arrow(helper, player);
            assertLaunch(helper, player, arrow, player.getEyeY() - (double) 0.1F, 3.6);
            helper.assertTrue(arrow.isCritArrow() && arrow.getBaseDamage() == 4.0
                            && arrow.pickup == AbstractArrow.Pickup.CREATIVE_ONLY,
                    "full charge must retain critical damage multiplier and creative-only pickup");
            assertAirTicks(helper, arrow);
            assertBowAndNoAmmo(helper, player, bow);
        } finally {
            remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void partialReleaseRetainsPowerPunchFlameAndFlight(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        try {
            ItemStack bow = player.getMainHandItem();
            bow.enchant(Enchantments.POWER_ARROWS, 3);
            bow.enchant(Enchantments.PUNCH_ARROWS, 2);
            bow.enchant(Enchantments.FLAMING_ARROWS, 1);
            release(helper, player, 10);
            BurningArrowEntity arrow = arrow(helper, player);
            assertLaunch(helper, player, arrow, player.getEyeY() - (double) 0.1F,
                    BowItem.getPowerForTime(10) * 1.2F * 3.0F);
            helper.assertTrue(!arrow.isCritArrow() && arrow.getBaseDamage() == 6.0
                            && arrow.getKnockback() == 2 && arrow.getRemainingFireTicks() == 2000
                            && arrow.pickup == AbstractArrow.Pickup.CREATIVE_ONLY,
                    "partial charge must preserve power/punch/flame without awarding full-charge criticals");
            assertAirTicks(helper, arrow);
            assertBowAndNoAmmo(helper, player, bow);
        } finally {
            remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void burningReleaseSpawnsOwnedBallAndRetainsCooldown(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        try {
            ItemStack bow = player.getMainHandItem();
            bow.getOrCreateTagElement("mode").putBoolean(BURNING_MODE, true);
            release(helper, player, 20);
            List<Projectile> shots = projectiles(player);
            helper.assertTrue(shots.size() == 1 && shots.get(0) instanceof BurningBallEntity,
                    "burning-mode release must add the actual burning-ball entity, never an arrow");
            Projectile ball = shots.get(0);
            helper.assertTrue(ball.getType() == ModEntities.BURNING_BALL.get(),
                    "burning ball must use the native registered entity type");
            assertLaunch(helper, player, ball, player.getEyeY() + 0.1, 3.6);
            helper.assertTrue(player.getCooldowns().isOnCooldown(bow.getItem()),
                    "burning-mode release must preserve its cooldown");
            assertAirTicks(helper, ball);
            assertBowAndNoAmmo(helper, player, bow);
        } finally {
            remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void shortChargeDoesNotLaunchEitherMode(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        try {
            ItemStack bow = player.getMainHandItem();
            for (boolean burning : new boolean[]{false, true}) {
                bow.getOrCreateTagElement("mode").putBoolean(BURNING_MODE, burning);
                release(helper, player, 2);
                helper.assertTrue(projectiles(player).isEmpty()
                                && !player.getCooldowns().isOnCooldown(bow.getItem()),
                        "two-tick charge must not launch a projectile or start a burning-mode cooldown");
                assertBowAndNoAmmo(helper, player, bow);
            }
        } finally {
            remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void ordinaryArrowDiscardsOnlyAfterRealEntityImpact(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Skeleton target = EntityType.SKELETON.create(helper.getLevel());
        helper.assertTrue(target != null, "native living target must be created");
        try {
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(player.getX() + 6.0, player.getY(), player.getZ());
            helper.assertTrue(helper.getLevel().addFreshEntity(target), "target must enter the server level");
            release(helper, player, 20);
            BurningArrowEntity arrow = arrow(helper, player);
            arrow.tick();
            helper.assertTrue(!arrow.isRemoved() && !target.hasEffect(ModMobEffects.BURNING.get()),
                    "first empty-air tick must not discard or apply an impact effect");
            arrow.tick();
            helper.assertTrue(arrow.isRemoved() && target.hasEffect(ModMobEffects.BURNING.get())
                            && target.getEffect(ModMobEffects.BURNING.get()).getDuration() == 1200,
                    "real second-tick collision must still burn the target and discard the arrow");
        } finally {
            target.discard();
            remove(player);
        }
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        UUID id = UUID.randomUUID();
        ServerPlayer player = NativeTestPlayers.get(helper.getLevel(),
                new GameProfile(id, "GT" + id.toString().replace("-", "").substring(0, 14)));
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.blaze_bow.get()));
        BlockPos pos = helper.absolutePos(new BlockPos(4, 4, 4));
        player.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, -90.0F, 0.0F);
        player.setNoGravity(true);
        player.setOnGround(true);
        player.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addNewPlayer(player);
        return player;
    }

    private static void release(GameTestHelper helper, ServerPlayer player, int drawTicks) {
        ItemStack bow = player.getMainHandItem();
        helper.assertTrue(bow.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND)
                        .getResult().consumesAction() && player.getUseItem() == bow,
                "bow must enter native item use without ammunition");
        for (int tick = 0; tick < drawTicks; tick++) player.doTick();
        helper.assertTrue(player.getTicksUsingItem() == drawTicks,
                "native living ticks must accumulate the requested draw duration");
        player.releaseUsingItem();
    }

    private static BurningArrowEntity arrow(GameTestHelper helper, ServerPlayer player) {
        List<Projectile> shots = projectiles(player);
        helper.assertTrue(shots.size() == 1 && shots.get(0) instanceof BurningArrowEntity,
                "ordinary releaseUsing must add exactly one real burning arrow near the shooter");
        BurningArrowEntity arrow = (BurningArrowEntity) shots.get(0);
        helper.assertTrue(arrow.getType() == ModEntities.BURNING_ARROW.get(),
                "ordinary shot must preserve the custom registered entity type, not vanilla arrow");
        return arrow;
    }

    private static List<Projectile> projectiles(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(Projectile.class,
                player.getBoundingBox().inflate(20.0), projectile -> projectile.getOwner() == player);
    }

    private static void assertLaunch(GameTestHelper helper, ServerPlayer player, Projectile shot,
                                     double expectedY, double expectedSpeed) {
        helper.assertTrue(shot.level() == helper.getLevel() && shot.getOwner() == player
                        && helper.getLevel().getEntity(shot.getId()) == shot,
                "shot must be owned by the shooter and actually registered in the same server level");
        helper.assertTrue(shot.position().distanceTo(new Vec3(player.getX(), expectedY, player.getZ())) < 0.000001,
                "shot must spawn at the mode's shooter eye position, not the world origin");
        Vec3 velocity = shot.getDeltaMovement();
        helper.assertTrue(velocity.normalize().dot(player.getViewVector(1.0F)) > 0.99
                        && Math.abs(velocity.length() - expectedSpeed) < 0.1,
                "shot must travel in the real aim direction at the charge-scaled velocity");
    }

    private static void assertAirTicks(GameTestHelper helper, Projectile shot) {
        for (int tick = 0; tick < 2; tick++) {
            Vec3 start = shot.position();
            Vec3 velocity = shot.getDeltaMovement();
            shot.tick();
            helper.assertTrue(!shot.isRemoved() && shot.position().distanceTo(start.add(velocity)) < 0.000001,
                    "native empty-air tick " + tick + " must keep the shot alive and advance by its real velocity");
        }
    }

    private static void assertBowAndNoAmmo(GameTestHelper helper, ServerPlayer player, ItemStack bow) {
        helper.assertTrue(player.getMainHandItem() == bow && bow.getCount() == 1
                        && player.getProjectile(bow).isEmpty(),
                "shot must preserve the held bow and work without consuming or creating ammunition");
    }

    private static void remove(ServerPlayer player) {
        projectiles(player).forEach(Entity::discard);
        player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
    }
}
