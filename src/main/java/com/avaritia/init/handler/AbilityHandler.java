package com.avaritia.init.handler;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.avaritia.Const;
import com.avaritia.api.utils.PlayerUtils;
import com.avaritia.common.item.misc.NeutronHorseArmorItem;
import com.avaritia.common.item.tools.InfinityArmorItem;
import com.avaritia.init.config.ModConfig;
import com.avaritia.util.ToolUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.avaritia.util.ToolUtils.isPlayerWearing;
import static net.minecraft.world.entity.EquipmentSlot.*;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/21 15:38
 * Version: 1.0
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class AbilityHandler {

    public static final Set<String> entitiesWithHelmets = new HashSet<>();
    public static final Set<String> entitiesWithLeggings = new HashSet<>();
    public static final Set<String> entitiesWithBoots = new HashSet<>();
    public static final Set<String> entitiesWithHands = new HashSet<>();
    public static final Map<String, FlightInfo> entitiesWithFlight = new ConcurrentHashMap<>();
    private static final Set<Integer> ARMORED_HORSES = new HashSet<>();

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
                if (ModConfig.InfinityArmorNightVision.get()) {
                    MobEffectInstance nv = player.getEffect(MobEffects.NIGHT_VISION);
                    if (nv == null) {
                        nv = new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false);
                        player.addEffect(nv);
                    }
                    nv.duration = 300;
                }
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

                    float speed = (float) (ModConfig.bootSpeedBase.get() * (flying ? ModConfig.bootSpeedFlyingMultiplier.get() : 1.0f)
                            * (swimming ? ModConfig.bootSpeedSwimmingMultiplier.get() : 1.0f)
                            * (sneaking ? ModConfig.bootSpeedSneakingMultiplier.get() : 1.0f));

                    if (player.zza > 0f) {
                        player.moveRelative(speed, new Vec3(0, 0, 1));
                    } else if (player.zza < 0f) {
                        player.moveRelative((float) (-speed * ModConfig.bootSpeedBackwardMultiplier.get()), new Vec3(0, 0, 1));
                    }

                    if (player.xxa != 0f) {
                        player.moveRelative((float) (speed * ModConfig.bootSpeedStrafingMultiplier.get() * Math.signum(player.xxa)), new Vec3(1, 0, 0));
                    }
                }

                if (player.isSprinting()) {
                    float f = player.getYRot() * ((float) Math.PI / 180F);
                    player.setDeltaMovement(player.getDeltaMovement().add(-Mth.sin(f) * ModConfig.bootSpeedSprintingMultiplier.get(), 0.0D, Mth.cos(f) * ModConfig.bootSpeedSprintingMultiplier.get()));
                }

            } else {
                entitiesWithBoots.add(key);
            }
        } else {
            Objects.requireNonNull(player.getAttribute(Attributes.STEP_HEIGHT)).removeModifier(Const.rl("avaritia_boots"));
            entitiesWithBoots.remove(key);
        }
    }

    private static void handleHandItemChange(Player player, String key, ItemStack itemStack) {
        if (player.getMainHandItem().is(itemStack.getItem()) || player.getOffhandItem().is(itemStack.getItem())) {
            if (entitiesWithHands.contains(key)) {

            }

        }
    }


        //璺宠穬澧炲己
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

    //鏃犲敖濂楀厤鐤瑙夋晥鏋?
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderOverlay(RenderBlockScreenEffectEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && ToolUtils.isInfinite(player) && event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE) {
            event.setCanceled(true);
        }else if (player != null && ToolUtils.isInfinite(player) && event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.BLOCK) {
            event.setCanceled(true);
        }else if (player != null && ToolUtils.isInfinite(player) && event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.WATER) {
            event.setCanceled(true);
        }
    }
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        Camera camera = event.getCamera();
        Entity entity = camera.getEntity();

        if (!(entity instanceof Player player)) return;
        if (!ToolUtils.isWearingInfinityHelmet(player)) return;

        FogType fogType = camera.getFluidInCamera();

        if (fogType == FogType.LAVA || fogType == FogType.POWDER_SNOW) {

            float farPlane = event.getRenderer().getRenderDistance();

            event.setNearPlaneDistance(-8.0f);
            event.setFarPlaneDistance(Math.min(96.0f, farPlane));
            event.setCanceled(true);
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
        player.onUpdateAbilities(); 
    }

    public static class FlightInfo {
        public boolean hadFlightItem;
        public boolean wasFlyingGameMode;
        public boolean wasFlyingAllowed;
        public boolean wasFlying;
    }

}
