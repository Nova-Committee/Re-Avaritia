package committee.nova.mods.avaritia.init.handler;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.utils.PlayerUtils;
import committee.nova.mods.avaritia.common.item.tools.InfinityArmorItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static committee.nova.mods.avaritia.util.ToolUtils.isPlayerWearing;
import static net.minecraft.world.entity.EquipmentSlot.*;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/21 15:38
 * Version: 1.0
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class AbilityHandler {

    public static final Set<String> entitiesWithHelmets = new HashSet<>();
    public static final Set<String> entitiesWithLeggings = new HashSet<>();
    public static final Set<String> entitiesWithBoots = new HashSet<>();
    public static final Map<String, FlightInfo> entitiesWithFlight = new ConcurrentHashMap<>();


    @SubscribeEvent
    public static void updateAbilities(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            String key = player.getGameProfile().getName() + ":" + player.level().isClientSide;

            boolean hasHelmet = isPlayerWearing(player, HEAD, item -> item instanceof InfinityArmorItem);
            boolean hasChest = isPlayerWearing(player, CHEST, item -> item instanceof InfinityArmorItem);
            boolean hasLeggings = isPlayerWearing(player, LEGS, item -> item instanceof InfinityArmorItem);
            boolean hasBoots = isPlayerWearing(player, FEET, item -> item instanceof InfinityArmorItem);


            handleHelmetStateChange(player, key, hasHelmet);
            handleChestStateChange(player, key, hasChest);
            handleLeggingsStateChange(player, key, hasLeggings);
            handleBootsStateChange(player, key, hasBoots);

        }
    }

    private static void handleChestStateChange(Player player, String key, boolean hasChest) {
        boolean isFlyingGameMode = !PlayerUtils.isPlayingMode(player);
        FlightInfo flightInfo = entitiesWithFlight.computeIfAbsent(key, uuid -> new FlightInfo());
        if (isFlyingGameMode || hasChest) {
            if (!flightInfo.hadFlightItem) {
                if (!player.getAbilities().mayfly) {
                    updateClientServerFlight(player, true);
                }
                flightInfo.hadFlightItem = true;
            } else if (flightInfo.wasFlyingGameMode && !isFlyingGameMode) {
                updateClientServerFlight(player, true, flightInfo.wasFlying);
            } else if (flightInfo.wasFlyingAllowed && !player.getAbilities().mayfly) {
                updateClientServerFlight(player, true, flightInfo.wasFlying);
            }
            flightInfo.wasFlyingGameMode = isFlyingGameMode;
            flightInfo.wasFlying = player.getAbilities().flying;
            flightInfo.wasFlyingAllowed = player.getAbilities().mayfly;
            if (hasChest) {
                List<MobEffectInstance> effects = Lists.newArrayList(player.getActiveEffects());
                for (MobEffectInstance potion : Collections2.filter(effects, potion -> !potion.getEffect().value().isBeneficial())) {
                    player.removeEffect(potion.getEffect());
                }
            }
        } else {
            if (flightInfo.hadFlightItem) {
                if (player.getAbilities().mayfly) {
                    updateClientServerFlight(player, false);
                }
                flightInfo.hadFlightItem = false;
            }
            flightInfo.wasFlyingGameMode = false;
            flightInfo.wasFlying = player.getAbilities().flying;
            flightInfo.wasFlyingAllowed = player.getAbilities().mayfly;
        }
    }

    private static void handleHelmetStateChange(Player player, String key, boolean hasHelmet) {
        if (hasHelmet) {
            if (entitiesWithHelmets.contains(key)) {
                player.setAirSupply(300);
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(20f);
                MobEffectInstance nv = player.getEffect(MobEffects.NIGHT_VISION);
                if (nv == null) {
                    nv = new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false);
                    player.addEffect(nv);
                }
                nv.duration = 300;

            } else {
                entitiesWithHelmets.add(key);
            }
        } else {
            entitiesWithHelmets.remove(key);
        }
    }

    private static void handleLeggingsStateChange(Player player, String key, boolean hasLeggings) {
        if (hasLeggings) {
            if (entitiesWithLeggings.contains(key)) {
                if (player.isOnFire()) {
                    player.clearFire();
                    player.fireImmune();
                }
            } else {
                entitiesWithLeggings.add(key);
            }
        } else {
            entitiesWithLeggings.remove(key);
        }
    }

    private static void handleBootsStateChange(Player player, String key, boolean hasBoots) {
        if (hasBoots) {
            if (entitiesWithBoots.contains(key)) {
                Objects.requireNonNull(player.getAttribute(Attributes.STEP_HEIGHT)).addOrUpdateTransientModifier(new AttributeModifier(Const.rl("avaritia_boots"), 1.0625F - 0.6F, AttributeModifier.Operation.ADD_VALUE));
                boolean flying = player.getAbilities().flying;
                boolean swimming = player.isInWater();
                boolean sneaking = player.isCrouching();
                if (player.onGround() || flying || swimming) {

                    float speed = 0.1f * (flying ? 1.1f : 1.0f)
                            * (swimming ? 1.2f : 1.0f)
                            * (sneaking ? 0.1f : 1.0f);

                    if (player.zza > 0f) {
                        player.moveRelative(speed, new Vec3(0, 0, 1));
                    } else if (player.zza < 0f) {
                        player.moveRelative(-speed * 0.25f, new Vec3(0, 0, 1));
                    }

                    if (player.xxa != 0f) {
                        player.moveRelative(speed * 0.45f * Math.signum(player.xxa), new Vec3(1, 0, 0));
                    }
                }

                if (player.isSprinting()) {
                    float f = player.getYRot() * ((float) Math.PI / 180F);
                    player.setDeltaMovement(player.getDeltaMovement().add(-Mth.sin(f) * 0.2F, 0.0D, Mth.cos(f) * 0.2F));
                }

            } else {
                entitiesWithBoots.add(key);
            }
        } else {
            Objects.requireNonNull(player.getAttribute(Attributes.STEP_HEIGHT)).removeModifier(Const.rl("avaritia_boots"));
            entitiesWithBoots.remove(key);
        }
    }

    //跳跃增强
    @SubscribeEvent
    public static void jumpBoost(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player && entitiesWithBoots.contains(player.getGameProfile().getName() + ":" + player.level().isClientSide) && player.isSprinting())
            player.setDeltaMovement(player.getDeltaMovement().add(0F, 0.305F, 0F));
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        stripAbilities(event.getEntity());
        reapplyFly(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        stripAbilities(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        stripAbilities(event.getEntity());
        clearFly(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        stripAbilities(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player entity) {
            stripAbilities(entity);
        }
    }

    private static void stripAbilities(Player player) {
        String key = player.getGameProfile().getName() + ":" + player.level().isClientSide;
        entitiesWithHelmets.remove(key);
        entitiesWithFlight.remove(key);
        entitiesWithLeggings.remove(key);
        entitiesWithBoots.remove(key);
    }

    private static void clearFly(Player player) {
        entitiesWithFlight.remove(player.getGameProfile().getName() + ":" + player.level().isClientSide);
    }

    private static void reapplyFly(Player player) {
        //For when the dimension changes/we need to reapply the flight info values to the client
        FlightInfo flightInfo = entitiesWithFlight.get(player.getGameProfile().getName() + ":" + player.level().isClientSide);
        if (flightInfo != null) {
            if (flightInfo.wasFlyingAllowed || flightInfo.wasFlying) {
                updateClientServerFlight(player, flightInfo.wasFlyingAllowed, flightInfo.wasFlying);
            }
        }
    }

    private static void updateClientServerFlight(Player player, boolean allowFlying) {
        updateClientServerFlight(player, allowFlying, allowFlying && player.getAbilities().flying);
    }

    private static void updateClientServerFlight(Player player, boolean allowFlying, boolean isFlying) {
        player.getAbilities().mayfly = allowFlying;
        player.getAbilities().flying = isFlying;
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.onUpdateAbilities();
        }
    }

    public static class FlightInfo {
        public boolean hadFlightItem;
        public boolean wasFlyingGameMode;
        public boolean wasFlyingAllowed;
        public boolean wasFlying;
    }

}
