package committee.nova.mods.avaritia.init.handler;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.common.item.ArmorInfinityItem;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import java.util.function.Predicate;

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
    public static final Set<String> entitiesWithChestplates = new HashSet<>();
    public static final Set<String> entitiesWithLeggings = new HashSet<>();
    public static final Set<String> entitiesWithBoots = new HashSet<>();
    public static final Set<String> entitiesWithFlight = new HashSet<>();


    public static boolean isPlayerWearing(LivingEntity entity, EquipmentSlot slot, Predicate<Item> predicate) {
        ItemStack stack = entity.getItemBySlot(slot);
        return !stack.isEmpty() && predicate.test(stack.getItem());
    }

    @SubscribeEvent
    public static void updateAbilities(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        LivingEntity entity = event.getEntity();
        String key = entity.getEncodeId() + "|" + entity.getCommandSenderWorld().isClientSide;

        boolean hasHelmet = isPlayerWearing(event.getEntity(), HEAD, item -> item instanceof ArmorInfinityItem);
        boolean hasChestplate = isPlayerWearing(event.getEntity(), CHEST, item -> item instanceof ArmorInfinityItem);
        boolean hasLeggings = isPlayerWearing(event.getEntity(), LEGS, item -> item instanceof ArmorInfinityItem);
        boolean hasBoots = isPlayerWearing(event.getEntity(), FEET, item -> item instanceof ArmorInfinityItem);

        //Helmet toggle.
        if (hasHelmet) {
            entitiesWithHelmets.add(key);
            handleHelmetStateChange(entity, true);
        }
        if (!hasHelmet) {
            entitiesWithHelmets.remove(key);
            handleHelmetStateChange(entity, false);
        }

        //Chestplate toggle.
        if (hasChestplate) {
            entitiesWithChestplates.add(key);
            handleChestplateStateChange(entity, true);
        }
        if (!hasChestplate) {
            entitiesWithChestplates.remove(key);
            handleChestplateStateChange(entity, false);
        }

        //Leggings toggle.
        if (hasLeggings) {
            entitiesWithLeggings.add(key);
            handleLeggingsStateChange(entity, true);
        }
        if (!hasLeggings) {
            entitiesWithLeggings.remove(key);
            handleLeggingsStateChange(entity, false);
        }

        //Boots toggle.
        if (hasBoots) {
            handleBootsStateChange(entity);
            entitiesWithBoots.add(key);
        }
        if (!hasBoots) {
            handleBootsStateChange(entity);
            entitiesWithBoots.remove(key);
        }

        //Active ability ticking.
        if (entitiesWithHelmets.contains(key)) {
            tickHelmetAbilities(entity);
        }
        if (entitiesWithChestplates.contains(key)) {
            tickChestplateAbilities(entity);
        }
        if (entitiesWithLeggings.contains(key)) {
            tickLeggingsAbilities(entity);
        }
        if (entitiesWithBoots.contains(key)) {
            tickBootsAbilities(entity);
        }
    }

    private static void stripAbilities(LivingEntity entity) {
        String key = entity.getEncodeId() + "|" + entity.getCommandSenderWorld().isClientSide;

        if (entitiesWithHelmets.remove(key)) {
            handleHelmetStateChange(entity, false);
        }

        if (entitiesWithChestplates.remove(key)) {
            handleChestplateStateChange(entity, false);
        }

        if (entitiesWithLeggings.remove(key)) {
            handleLeggingsStateChange(entity, false);
        }

        if (entitiesWithBoots.remove(key)) {
            handleBootsStateChange(entity);
        }
    }

    private static void handleHelmetStateChange(LivingEntity entity, boolean isNew) {
        String key = entity.getEncodeId() + "|" + entity.getCommandSenderWorld().isClientSide;
        if (entity instanceof Player player) {
            if (isNew) {
                player.setAirSupply(300);
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(20f);
                MobEffectInstance nv = player.getEffect(MobEffects.NIGHT_VISION);
                if (nv == null) {
                    nv = new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false);
                    player.addEffect(nv);
                }
                nv.duration = 300;
                entitiesWithHelmets.add(key);
            } else {
                entitiesWithHelmets.remove(key);
            }

        }
    }

    private static void handleChestplateStateChange(LivingEntity entity, boolean isNew) {
        String key = entity.getEncodeId() + "|" + entity.getCommandSenderWorld().isClientSide;
        if (entity instanceof Player player) {
            if (isNew) {
                player.getAbilities().mayfly = true;
                player.getAbilities().mayfly = true;
                List<MobEffectInstance> effects = Lists.newArrayList(player.getActiveEffects());
                for (MobEffectInstance potion : Collections2.filter(effects, potion -> !potion.getEffect().isBeneficial())) {
                    player.removeEffect(potion.getEffect());
                }
                entitiesWithFlight.add(key);
            } else {
                if (!player.isCreative() && entitiesWithFlight.contains(key)) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    entitiesWithFlight.remove(key);
                }
            }

        }
    }

    private static void handleLeggingsStateChange(LivingEntity entity, boolean isNew) {
        String key = entity.getEncodeId() + "|" + entity.getCommandSenderWorld().isClientSide;

        if (entity instanceof Player) {
            if (isNew) {
                entitiesWithLeggings.add(key);
            } else {
                entitiesWithLeggings.remove(key);
            }

        }
    }

    private static void handleBootsStateChange(LivingEntity entity) {
        String key = entity.getEncodeId() + "|" + entity.getCommandSenderWorld().isClientSide;
        boolean hasBoots = isPlayerWearing(entity, FEET, item -> item instanceof ArmorInfinityItem);
        if (hasBoots) {
            entity.setMaxUpStep(1.08F);//Step 17 pixels, Allows for stepping directly from a path to the top of a block next to the path.
            if (!entitiesWithBoots.contains(key)) {
                entitiesWithBoots.add(key);
            }
        } else {
            if (entitiesWithBoots.contains(key)) {
                entity.setMaxUpStep(0.5F);
                entitiesWithBoots.remove(key);
            }
        }
    }
    //endregion

    //region Ability Ticking
    private static void tickHelmetAbilities(LivingEntity entity) {

    }

    private static void tickChestplateAbilities(LivingEntity entity) {

    }

    private static void tickLeggingsAbilities(LivingEntity entity) {
        if (entity.isOnFire()) {
            entity.clearFire();
            entity.fireImmune();
        }
    }

    private static void tickBootsAbilities(LivingEntity entity) {
        boolean flying = entity instanceof Player && ((Player) entity).getAbilities().flying;
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
            if (InfinityHandler.isInfinite(player) && !(event.getSource() instanceof ModDamageTypes.DamageSourceRandomMessages)) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth());
            }
        }
    }


    //跳跃增强
    @SubscribeEvent
    public void jumpBoost(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entitiesWithBoots.contains(entity.getEncodeId() + "|" + entity.getCommandSenderWorld().isClientSide)) {
            entity.setDeltaMovement(0, 2.0f, 0);
        }
    }

    @SubscribeEvent
    public void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        stripAbilities(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        stripAbilities(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        stripAbilities(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        stripAbilities(event.getEntity());
    }

    @SubscribeEvent
    public void entityConstructEvent(EntityEvent.EntityConstructing event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            stripAbilities(entity);
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        stripAbilities(event.getEntity());
    }
}
