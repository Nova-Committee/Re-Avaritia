package committee.nova.mods.avaritia.init.handler;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.common.item.ArmorInfinityItem;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.AbilityUtil;
import committee.nova.mods.avaritia.util.PlayerUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

import static committee.nova.mods.avaritia.util.AbilityUtil.isPlayerWearing;
import static net.minecraft.world.entity.EquipmentSlot.*;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/21 15:38
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AbilityHandler {

    public static final Set<String> entitiesWithHelmets = new HashSet<>();
    public static final Set<String> entitiesWithLeggings = new HashSet<>();
    public static final Set<String> entitiesWithBoots = new HashSet<>();
    public static final Map<String, FlightInfo> entitiesWithFlight = new HashMap<>();


    @SubscribeEvent
    public static void updateAbilities(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            String key = player.getGameProfile().getName() + ":" + player.level().isClientSide;

            boolean hasHelmet = isPlayerWearing(event.getEntity(), HEAD, item -> item instanceof ArmorInfinityItem);
            boolean hasChest = isPlayerWearing(event.getEntity(), CHEST, item -> item instanceof ArmorInfinityItem);
            boolean hasLeggings = isPlayerWearing(event.getEntity(), LEGS, item -> item instanceof ArmorInfinityItem);
            boolean hasBoots = isPlayerWearing(event.getEntity(), FEET, item -> item instanceof ArmorInfinityItem);


            handleHelmetStateChange(player, key, hasHelmet);
            handleChestStateChange(player, key, hasChest);
            handleLeggingsStateChange(player, key, hasLeggings);
            handleBootsStateChange(player, key, hasBoots);

        }
    }

    private static void handleChestStateChange(Player player, String key, boolean hasChest) {
        boolean isFlyingGameMode = !PlayerUtil.isPlayingMode(player);
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
            if (player.getAbilities().flying && hasChest){
                List<MobEffectInstance> effects = Lists.newArrayList(player.getActiveEffects());
                for (MobEffectInstance potion : Collections2.filter(effects, potion -> !potion.getEffect().isBeneficial())) {
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
                player.setMaxUpStep(1.0625F);//Step 17 pixels, Allows for stepping directly from a path to the top of a block next to the path.
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
                    float f = player.getYRot() * ((float)Math.PI / 180F);
                    player.setDeltaMovement(player.getDeltaMovement().add(-Mth.sin(f) * 0.2F, 0.0D, Mth.cos(f) * 0.2F));
                }

            } else {
                entitiesWithBoots.add(key);
            }
        } else  {
            player.setMaxUpStep(0.6F);
            entitiesWithBoots.remove(key);
        }
    }

    //跳跃增强
    @SubscribeEvent
    public static void jumpBoost(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player && entitiesWithBoots.contains(player.getGameProfile().getName() + ":" + player.level().isClientSide))
            player.setDeltaMovement(0, 0.45f, 0);
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


    public static class FlightInfo {
        public boolean hadFlightItem;
        public boolean wasFlyingGameMode;
        public boolean wasFlyingAllowed;
        public boolean wasFlying;
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

}
