package committee.nova.mods.avaritia.init.handler;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.common.item.ArmorInfinityItem;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.AbilityUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public static final Set<String> entitiesWithChest = new HashSet<>();
    public static final Set<String> entitiesWithLeggings = new HashSet<>();
    public static final Set<String> entitiesWithBoots = new HashSet<>();
    public static final Set<String> entitiesWithFlight = new HashSet<>();




    @SubscribeEvent
    public static void updateAbilities(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player entity) {
            String key = entity.getGameProfile().getName() + ":" + entity.level().isClientSide;

            boolean hasHelmet = isPlayerWearing(event.getEntity(), HEAD, item -> item instanceof ArmorInfinityItem);
            boolean hasChest = isPlayerWearing(event.getEntity(), CHEST, item -> item instanceof ArmorInfinityItem);
            boolean hasLeggings = isPlayerWearing(event.getEntity(), LEGS, item -> item instanceof ArmorInfinityItem);
            boolean hasBoots = isPlayerWearing(event.getEntity(), FEET, item -> item instanceof ArmorInfinityItem);

            if (entitiesWithHelmets.contains(key)) {
                if (hasHelmet) {
                    handleHelmetStateChange(entity);
                } else {
                    entitiesWithHelmets.remove(key);
                }
            } else {
                entitiesWithHelmets.add(key);
            }


            if (entitiesWithChest.contains(key)) {
                if (hasChest) {
                    handleChestStateChange(entity);
                } else  {
                    if (!entity.isCreative() && !entity.isSpectator()){
                        entity.getAbilities().mayfly = false;
                        entity.getAbilities().flying = false;
                    }
                    entitiesWithChest.remove(key);
                }
            } else {
                entitiesWithChest.add(key);
            }


            if (entitiesWithLeggings.contains(key)) {
                if (hasLeggings) {
                    handleLeggingsStateChange(entity);
                } else  {
                    entitiesWithLeggings.remove(key);
                }
            } else {
                entitiesWithLeggings.add(key);
            }


            if (entitiesWithBoots.contains(key)) {
                if (hasBoots) {
                    handleBootsStateChange(entity);
                } else  {
                    entity.setMaxUpStep(0.5F);
                    entitiesWithBoots.remove(key);
                }
            } else {
                entitiesWithBoots.add(key);
            }

        }
    }

    private static void stripAbilities(Player entity) {
        String key = entity.getGameProfile().getName() + ":" + entity.level().isClientSide;

        if (entitiesWithHelmets.remove(key)) {
        }

        if (entitiesWithChest.remove(key)) {
            if (!entity.isCreative() && !entity.isSpectator()){
                entity.getAbilities().mayfly = false;
                entity.getAbilities().flying = false;
            }
        }

        if (entitiesWithLeggings.remove(key)) {
        }

        if (entitiesWithBoots.remove(key)) {
            entity.setMaxUpStep(0.5F);
        }
    }

    private static void handleHelmetStateChange(LivingEntity entity) {
        if (entity instanceof Player player) {
                player.setAirSupply(300);
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(20f);
                MobEffectInstance nv = player.getEffect(MobEffects.NIGHT_VISION);
                if (nv == null) {
                    nv = new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false);
                    player.addEffect(nv);
                }
                nv.duration = 300;
        }
    }

    private static void handleChestStateChange(LivingEntity entity) {
        if (entity instanceof Player player) {
                player.getAbilities().mayfly = true;
                List<MobEffectInstance> effects = Lists.newArrayList(player.getActiveEffects());
                for (MobEffectInstance potion : Collections2.filter(effects, potion -> !potion.getEffect().isBeneficial())) {
                    player.removeEffect(potion.getEffect());
                }


        }
    }

    private static void handleLeggingsStateChange(LivingEntity entity) {
        if (entity.isOnFire()) {
            entity.clearFire();
            entity.fireImmune();
        }
    }

    private static void handleBootsStateChange(LivingEntity entity) {
        entity.setMaxUpStep(1.25F);//Step 17 pixels, Allows for stepping directly from a path to the top of a block next to the path.
        boolean flying = entity instanceof Player player && player.getAbilities().flying;
        boolean swimming = entity.isInWater();
        if (entity.onGround() || flying || swimming) {
            boolean sneaking = entity.isCrouching();

            float speed = 0.1f * (flying ? 1.1f : 1.0f)
                    * (swimming ? 1.2f : 1.0f)
                    * (sneaking ? 0.1f : 1.0f);

            if (entity.zza > 0f) {
                entity.moveRelative(speed, new Vec3(0, 0, 1));
            } else if (entity.zza < 0f) {
                entity.moveRelative(-speed * 0.3f, new Vec3(0, 0, 1));
            }

            if (entity.xxa != 0f) {
                entity.moveRelative(speed * 0.5f * Math.signum(entity.xxa), new Vec3(1, 0, 0));
            }
        }
    }


    //特殊效果（附魔）
    @SubscribeEvent
    public static void opTool(PlayerEvent.ItemCraftedEvent event) {
        ItemStack stack = event.getCrafting();
        if (stack.getItem().equals(ModItems.infinity_sword.get())) {
            if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.MOB_LOOTING, stack) < 10) {
                stack.enchant(Enchantments.MOB_LOOTING, 10);
            }
        }
        if (stack.getItem().equals(ModItems.infinity_pickaxe.get())) {
            if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack) < 10) {
                stack.enchant(Enchantments.BLOCK_FORTUNE, 10);
            }
        }
        if (stack.getItem().equals(ModItems.infinity_bow.get())) {
            if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) < 10) {
                stack.enchant(Enchantments.INFINITY_ARROWS, 10);
            }
        }
    }


    //取消身穿无尽套时的伤害
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (AbilityUtil.isInfinite(player) && !(event.getSource() instanceof ModDamageTypes.DamageSourceRandomMessages)) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth());
            }
        }
    }


    //跳跃增强
    @SubscribeEvent
    public static void jumpBoost(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player && entitiesWithBoots.contains(player.getGameProfile().getName() + ":" + entity.level().isClientSide))
            player.setDeltaMovement(0, 0.65f, 0);
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        stripAbilities(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        stripAbilities(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        stripAbilities(event.getEntity());
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
}
