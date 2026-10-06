package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityShieldItem;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/** Exercises vanilla lifecycle entry points with real, connected server players. */
public final class NativeGameplayGameTests {
    private static final String TEMPLATE = "avaritia:portable_ui_empty";
    private static final Vec3 INCOMING = new Vec3(0.0, 0.0, -2.0);

    private NativeGameplayGameTests() {}

    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void customFuelsSmeltThroughVanillaFurnaces(GameTestHelper helper) {
        Item[] fuels = {
                ModItems.star_fuel.get(), ModItems.refined_coal.get(),
                ModBlocks.star_fuel_block.get().asItem(), ModBlocks.refined_coal_block.get().asItem()
        };
        FurnaceBlockEntity[] furnaces = new FurnaceBlockEntity[fuels.length];
        for (int i = 0; i < fuels.length; i++) {
            BlockPos pos = new BlockPos(2 + i * 2, 2, 2);
            helper.setBlock(pos, Blocks.FURNACE);
            furnaces[i] = (FurnaceBlockEntity) helper.getBlockEntity(pos);
            furnaces[i].setItem(0, new ItemStack(Items.IRON_ORE));
            furnaces[i].setItem(1, new ItemStack(fuels[i]));
        }
        helper.runAfterDelay(210, () -> {
            for (int i = 0; i < furnaces.length; i++) {
                helper.assertTrue(furnaces[i].getItem(2).is(Items.IRON_INGOT)
                                && furnaces[i].getItem(2).getCount() == 1,
                        "native furnace must actually smelt one iron ore using " + fuels[i]);
                helper.assertTrue(furnaces[i].getItem(0).isEmpty() && furnaces[i].getItem(1).isEmpty(),
                        "smelting must consume the ore and exactly one fuel item");
            }
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void infinityArmorCancelsRealLivingDamage(GameTestHelper helper) {
        ServerPlayer player = player(helper, new BlockPos(4, 2, 4));
        try {
            LivingEntity living = player;
            helper.assertTrue(living.hurt(player.damageSources().magic(), 4.0F)
                            && player.getHealth() < player.getMaxHealth(),
                    "unarmored survival player must actually receive damage before testing immunity");
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.infinity_helmet.get()));
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.infinity_chestplate.get()));
            player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.infinity_pants.get()));
            player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.infinity_boots.get()));
            resetDamage(player, 13.0F);
            helper.assertTrue(!living.hurt(player.damageSources().generic(), 1000.0F),
                    "complete infinity armor must cancel lethal ordinary damage at LivingEntity.hurt");
            helper.assertTrue(!living.hurt(player.damageSources().magic(), 1000.0F),
                    "armor-bypassing magic must also be cancelled, not merely reduced by armor points");
            helper.assertTrue(!living.hurt(player.damageSources().fellOutOfWorld(), 1000.0F),
                    "complete infinity armor must preserve native void-damage immunity");
            helper.assertTrue(player.isAlive() && player.getHealth() == 13.0F
                            && player.hurtTime == 0 && player.deathTime == 0,
                    "immunity must preserve existing health without death or a totem-style heal");

            player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
            resetDamage(player, 13.0F);
            helper.assertTrue(living.hurt(player.damageSources().magic(), 4.0F)
                            && player.getHealth() < 13.0F,
                    "an incomplete set must not retain full-set damage immunity");
        } finally {
            remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void inventoryTotemSurvivesLethalDamageAndIsConsumed(GameTestHelper helper) {
        ServerPlayer player = player(helper, new BlockPos(4, 2, 4));
        try {
            ItemStack totem = new ItemStack(ModItems.infinity_totem.get());
            totem.setDamageValue(totem.getMaxDamage() - 2);
            player.getInventory().setItem(17, totem);
            player.getInventory().setItem(0, new ItemStack(Items.STICK));
            helper.assertTrue(player.getOffhandItem().isEmpty() && player.getMainHandItem().is(Items.STICK),
                    "totem must be in an ordinary non-hotbar inventory slot, not either hand");

            resetDamage(player, 2.0F);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 0));
            helper.assertTrue(player.hurt(player.damageSources().generic(), 1000.0F),
                    "lethal damage must enter the real damage/death-protection path");
            assertTotemSurvival(helper, player);
            helper.assertTrue(player.getInventory().getItem(17) == totem
                            && totem.getDamageValue() == totem.getMaxDamage() - 1 && totem.getCount() == 1,
                    "first resurrection must consume exactly one durability on the actual inventory stack");

            resetDamage(player, 2.0F);
            helper.assertTrue(player.hurt(player.damageSources().generic(), 1000.0F),
                    "second lethal hit must not be swallowed by damage cooldown or absorption");
            assertTotemSurvival(helper, player);
            helper.assertTrue(player.getInventory().getItem(17).isEmpty(),
                    "the last durability must break and consume the inventory totem");
            helper.assertTrue(player.getMainHandItem().is(Items.STICK),
                    "consuming an inventory totem must not consume the held item");

            resetDamage(player, 2.0F);
            helper.assertTrue(player.hurt(player.damageSources().generic(), 1000.0F)
                            && !player.isAlive() && player.getHealth() == 0.0F,
                    "a third lethal hit with no totem must really kill the player");
        } finally {
            remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void chestInfinityElytraStartsAndContinuesNativeFallFlight(GameTestHelper helper) {
        ServerPlayer player = player(helper, new BlockPos(4, 6, 4));
        try {
            player.setOnGround(false);
            player.setDeltaMovement(0.0, -0.2, 0.1);
            helper.assertTrue(!player.tryToStartFallFlying(),
                    "the same airborne player without an elytra must not start fall-flight");
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.infinity_elytra.get()));
            helper.assertTrue(player.tryToStartFallFlying() && player.isFallFlying(),
                    "chest infinity elytra must start through vanilla Player.tryToStartFallFlying");
            for (int tick = 0; tick < 12; tick++) {
                // ServerPlayer.tick is bookkeeping; doTick executes Player/LivingEntity ticking.
                player.doTick();
                helper.assertTrue(player.isFallFlying(),
                        "LivingEntity.updateFallFlying must retain infinity-elytra flight on tick " + tick);
                helper.assertTrue(!player.getAbilities().flying && !player.getAbilities().mayfly,
                        "fall-flight must not be replaced by creative/armor flight permissions");
            }
            helper.assertTrue(player.getFallFlyingTicks() >= 12,
                    "the native fall-flight counter must advance across real living ticks");
            player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            player.doTick();
            helper.assertTrue(!player.isFallFlying(),
                    "removing the chest elytra must stop fall-flight on the next native living tick");
        } finally {
            remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void infinityShieldReflectsEggWithoutOverrideDiscard(GameTestHelper helper) {
        ShieldFixture fixture = shieldFixture(helper);
        ThrownEgg egg = new ThrownEgg(helper.getLevel(), fixture.owner());
        try {
            launchAtShield(helper, fixture.player(), egg);
            egg.tick();
            assertReflected(helper, fixture, egg);
            Vec3 reflectedPosition = egg.position();
            egg.tick();
            helper.assertTrue(!egg.isRemoved() && egg.getDeltaMovement().z > 0.0
                            && egg.getZ() > reflectedPosition.z,
                    "reflected vanilla egg must remain alive and continue away on its next collision tick");
            helper.assertTrue(fixture.player().getHealth() == fixture.player().getMaxHealth()
                            && fixture.player().hurtTime == 0 && fixture.player().getActiveEffects().isEmpty(),
                    "the egg's onHitEntity and overriding onHit body must not damage or affect the defender");
        } finally {
            egg.discard();
            fixture.owner().discard();
            remove(fixture.player());
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void infinityShieldCancelsPotionSplashDamageAndEffects(GameTestHelper helper) {
        ShieldFixture fixture = shieldFixture(helper);
        ThrownPotion reflected = harmfulPotion(helper, fixture.owner());
        ThrownPotion control = harmfulPotion(helper, fixture.owner());
        try {
            launchAtShield(helper, fixture.player(), reflected);
            reflected.tick();
            assertReflected(helper, fixture, reflected);
            helper.assertTrue(fixture.player().getHealth() == fixture.player().getMaxHealth()
                            && fixture.player().getActiveEffects().isEmpty(),
                    "cancelling the entire potion override must prevent both splash harm and slowness");
            reflected.tick();
            helper.assertTrue(!reflected.isRemoved() && reflected.getDeltaMovement().z > 0.0,
                    "reflected splash potion must survive its overriding discard and the following tick");
            reflected.discard();

            // Same target, active blocking state, owner, trajectory and vanilla subclass; only mode changes.
            fixture.player().getUseItem().getOrCreateTagElement("mode").putBoolean(
                    InfinityShieldItem.MODES.get(InfinityShieldItem.MODE_DEFENDING), false);
            helper.assertTrue(fixture.player().isBlocking(),
                    "normal-mode control must retain the identical valid vanilla blocking state");
            launchAtShield(helper, fixture.player(), control);
            control.tick();
            helper.assertTrue(control.isRemoved(),
                    "normal-mode potion must collide and run its real overriding onHit/discard body");
            helper.assertTrue(fixture.player().getHealth() < fixture.player().getMaxHealth()
                            && fixture.player().hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                    "normal-mode control must actually apply splash damage and effects, proving the collision fixture");
        } finally {
            reflected.discard();
            control.discard();
            fixture.owner().discard();
            remove(fixture.player());
        }
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper, BlockPos relativePos) {
        UUID id = UUID.randomUUID();
        ServerPlayer player = NativeTestPlayers.get(helper.getLevel(),
                new GameProfile(id, "GT" + id.toString().replace("-", "").substring(0, 14)));
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.getAbilities().invulnerable = false;
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        BlockPos pos = helper.absolutePos(relativePos);
        player.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addNewPlayer(player);
        // Expire vanilla's 60-tick new-player damage immunity without bypassing the actual hurt entry point.
        for (int tick = 0; tick < 60; tick++) player.tick();
        return player;
    }

    private static void resetDamage(ServerPlayer player, float health) {
        player.removeAllEffects();
        player.setAbsorptionAmount(0.0F);
        player.invulnerableTime = 0;
        player.hurtTime = 0;
        player.setHealth(health);
    }

    private static void assertTotemSurvival(GameTestHelper helper, ServerPlayer player) {
        helper.assertTrue(player.isAlive() && !player.dead && player.getHealth() == player.getMaxHealth(),
                "inventory infinity totem must restore full health before vanilla death is entered");
        helper.assertTrue(!player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                        && player.hasEffect(MobEffects.REGENERATION)
                        && player.hasEffect(MobEffects.DAMAGE_RESISTANCE)
                        && player.hasEffect(MobEffects.ABSORPTION)
                        && player.hasEffect(MobEffects.FIRE_RESISTANCE),
                "real resurrection must clear prior effects and apply infinity-totem recovery effects");
    }

    private static ShieldFixture shieldFixture(GameTestHelper helper) {
        ServerPlayer player = player(helper, new BlockPos(4, 2, 4));
        helper.getLevel().setBlockAndUpdate(player.blockPosition().below(), Blocks.STONE.defaultBlockState());
        player.setOnGround(true);
        ItemStack shield = new ItemStack(ModItems.infinity_shield.get());
        shield.getOrCreateTagElement("mode").putBoolean(
                InfinityShieldItem.MODES.get(InfinityShieldItem.MODE_DEFENDING), true);
        player.setItemInHand(InteractionHand.MAIN_HAND, shield);
        helper.assertTrue(shield.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND)
                        .getResult().consumesAction(),
                "fixture must start shield use through the real vanilla ShieldItem.use path");
        for (int tick = 0; tick < 6; tick++) player.doTick();
        helper.assertTrue(player.isBlocking() && player.getUseItem() == shield
                        && player.getViewVector(1.0F).dot(INCOMING) < 0.0,
                "defender must really be blocking, past shield warmup, facing the incoming projectile");
        Skeleton owner = EntityType.SKELETON.create(helper.getLevel());
        helper.assertTrue(owner != null, "vanilla projectile owner must be created");
        owner.setNoAi(true);
        owner.setNoGravity(true);
        owner.moveTo(player.getX(), player.getY(), player.getZ() + 8.0, 180.0F, 0.0F);
        helper.assertTrue(helper.getLevel().addFreshEntity(owner), "vanilla projectile owner must enter the level");
        return new ShieldFixture(player, owner);
    }

    private static void launchAtShield(GameTestHelper helper, ServerPlayer player, Projectile projectile) {
        projectile.setPos(player.getX(), player.getY() + 0.9, player.getZ() + 1.6);
        projectile.setDeltaMovement(INCOMING);
        projectile.setNoGravity(true);
        helper.assertTrue(player.getBoundingBox().inflate(0.3)
                        .clip(projectile.position(), projectile.position().add(INCOMING)).isPresent(),
                "real projectile movement segment must intersect the actual defender hitbox");
        helper.assertTrue(player.isDamageSourceBlocked(
                        player.damageSources().thrown(projectile, projectile.getOwner())),
                "vanilla shield-block predicate must accept this actual projectile position and active use");
        helper.assertTrue(helper.getLevel().addFreshEntity(projectile), "projectile must enter the real server level");
    }

    private static void assertReflected(GameTestHelper helper, ShieldFixture fixture, Projectile projectile) {
        helper.assertTrue(!projectile.isRemoved(),
                "reflection must cancel the entire vanilla subclass impact, including its discard");
        helper.assertTrue(projectile.getOwner() == fixture.owner(),
                "reflection must preserve the original projectile owner, as in the source gameplay contract");
        helper.assertTrue(Math.abs(projectile.getDeltaMovement().z - 2.0 * 0.9 * 0.99F) < 0.00001
                        && Math.abs(projectile.getDeltaMovement().x) < 0.00001
                        && Math.abs(projectile.getDeltaMovement().y) < 0.00001,
                "native collision must reverse/damp incoming velocity once, followed by vanilla air drag");
    }

    private static ThrownPotion harmfulPotion(GameTestHelper helper, LivingEntity owner) {
        ThrownPotion potion = new ThrownPotion(helper.getLevel(), owner);
        potion.setItem(PotionUtils.setCustomEffects(new ItemStack(Items.SPLASH_POTION), List.of(
                new MobEffectInstance(MobEffects.HARM, 1, 0),
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 0))));
        return potion;
    }

    private static void remove(ServerPlayer player) {
        player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
    }

    private record ShieldFixture(ServerPlayer player, Skeleton owner) {}
}
