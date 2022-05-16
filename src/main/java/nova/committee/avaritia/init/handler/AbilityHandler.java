package nova.committee.avaritia.init.handler;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nova.committee.avaritia.common.item.ArmorInfinityItem;
import nova.committee.avaritia.common.item.tools.DamageSourceInfinitySword;
import nova.committee.avaritia.init.registry.ModItems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    public static void updateAbilities(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        LivingEntity entity = event.getEntityLiving();
        String key = entity.getEncodeId() + "|" + entity.level.isClientSide;

        boolean hasHelmet = isPlayerWearing(event.getEntityLiving(), HEAD, item -> item instanceof ArmorInfinityItem);
        boolean hasChestplate = isPlayerWearing(event.getEntityLiving(), CHEST, item -> item instanceof ArmorInfinityItem);
        boolean hasLeggings = isPlayerWearing(event.getEntityLiving(), LEGS, item -> item instanceof ArmorInfinityItem);
        boolean hasBoots = isPlayerWearing(event.getEntityLiving(), FEET, item -> item instanceof ArmorInfinityItem);

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
        String key = entity.getEncodeId() + "|" + entity.level.isClientSide;

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
        //TODO, Helmet abilities? Water breathing, NightVision, Auto Eat or No Hunger, No bad effects.
    }

    private static void handleChestplateStateChange(LivingEntity entity, boolean isNew) {
        String key = entity.getEncodeId() + "|" + entity.level.isClientSide;
        if (entity instanceof Player) {
            Player player = ((Player) entity);
            if (isNew) {
                player.getAbilities().mayfly = true;
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

    }

    private static void handleBootsStateChange(LivingEntity entity) {
        String temp_key = entity.getEncodeId() + "|" + entity.level.isClientSide;
        boolean hasBoots = isPlayerWearing(entity, FEET, item -> item instanceof ArmorInfinityItem);
        if (hasBoots) {
            entity.maxUpStep = 1.0625F;//Step 17 pixels, Allows for stepping directly from a path to the top of a block next to the path.
            if (!entitiesWithBoots.contains(temp_key)) {
                entitiesWithBoots.add(temp_key);
            }
        } else {
            if (entitiesWithBoots.contains(temp_key)) {
                entity.maxUpStep = 0.5F;
                entitiesWithBoots.remove(temp_key);
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

    }

    private static void tickBootsAbilities(LivingEntity entity) {
        boolean flying = entity instanceof Player && ((Player) entity).getAbilities().flying;
        boolean swimming = entity.isInWater();
        if (entity.isOnGround() || flying || swimming) {
            boolean sneaking = entity.isCrouching();

            float speed = 0.15f * (flying ? 1.1f : 1.0f)
                    //* (swimming ? 1.2f : 1.0f)
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

    @SubscribeEvent
    public static void opTool(PlayerEvent.ItemCraftedEvent event) {
        ItemStack stack = event.getCrafting();
        if (stack.getItem().equals(ModItems.infinity_sword)) {
            Map<Enchantment, Integer> map = new HashMap<>();
            map.put(Enchantments.MOB_LOOTING, 10);
            EnchantmentHelper.setEnchantments(map, stack);
        }
    }

    //取消身穿无尽套时的伤害
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof Player player) {
            if (InfinityHandler.isInfinite(player) && !(event.getSource() instanceof DamageSourceInfinitySword)) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    @SubscribeEvent
    public void jumpBoost(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entitiesWithBoots.contains(entity.getEncodeId() + "|" + entity.level.isClientSide)) {
            entity.setDeltaMovement(0, 2.0f, 0);
        }
    }

    @SubscribeEvent
    public void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        stripAbilities(event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        stripAbilities(event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        stripAbilities(event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        stripAbilities(event.getPlayer());
    }

    @SubscribeEvent
    public void entityContstructedEvent(EntityEvent.EntityConstructing event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            stripAbilities(entity);
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        stripAbilities(event.getEntityLiving());
    }
}
