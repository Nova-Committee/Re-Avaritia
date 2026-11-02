package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.WolfArmorState;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDataAttachments;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

/**
 * 中子战狼铠的专属机制（1.21.1）。
 *
 * <ul>
 *   <li><b>护卫</b>：羁绊半径内伤害被完全抵消，半径外线性衰减到保底减伤；</li>
 *   <li><b>调停</b>：穿甲 / 无视无敌的机制伤害只吃 {@code wolf_hax_reduction}；</li>
 *   <li><b>羁绊</b>：主人被一击打死时，半径内的狼替主人吃下这一击；</li>
 *   <li><b>图腾</b>：狼自己被一击打死时免死一次，代价是铠甲耐久。</li>
 * </ul>
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public final class NeutronWolfArmorHandler {
    private static final double EPSILON = 1.0E-4D;
    private static final int TICK_INTERVAL = 20;
    private static final ResourceLocation HEALTH_MODIFIER_ID = Const.rl("neutron_wolf_armor_health");

    private NeutronWolfArmorHandler() {
    }

    /** 比例减伤、护卫免伤、羁绊与图腾统一在伤害结算后处理。 */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!ModConfig.neutronWolfArmorEnabled.get()) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)) {
            return;
        }

        if (victim instanceof ServerPlayer player) {
            absorbLethalHit(event, player, source);
            return;
        }
        if (!isArmoredWolf(victim)) {
            return;
        }
        double reduction = reductionAt(ownerDistance(victim));
        if (isHaxDamage(source)) {
            reduction = Math.min(reduction, ModConfig.neutronWolfHaxReduction.get());
        }
        double remaining = event.getNewDamage() * Math.max(0.0D, 1.0D - reduction);
        if (remaining >= victim.getHealth() - EPSILON && tryTotem(victim)) {
            event.setNewDamage(0.0F);
            return;
        }
        event.setNewDamage((float) Math.max(0.0D, remaining));
    }

    /**
     * 1.21.1 的 {@code Wolf#mobInteract} 用 {@code Items.WOLF_ARMOR} 硬编码判断穿甲，
     * 自定义狼铠没法直接右键穿上；这里用实体交互事件接管同一套校验条件。
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Wolf wolf)) {
            return;
        }
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.neutron_wolf_armor.get())) {
            return;
        }
        if (!wolf.isTame() || !wolf.isOwnedBy(player) || wolf.isBaby() || !wolf.getBodyArmorItem().isEmpty()) {
            return;
        }
        wolf.setBodyArmorItem(stack.copyWithCount(1));
        stack.consume(1, player);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    /** 每 20 tick 同步生命上限，避免每 tick 扫描。 */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity wolf) || wolf.level().isClientSide()) {
            return;
        }
        if (wolf.getType() != EntityType.WOLF || wolf.tickCount % TICK_INTERVAL != 0) {
            return;
        }
        syncHealthBonus(wolf);
    }

    /**
     * 主人受致命一击时，由半径内最近的穿铠狼代受。
     */
    private static void absorbLethalHit(LivingDamageEvent.Pre event, ServerPlayer player, DamageSource source) {
        float lethalDamage = event.getNewDamage();
        if (lethalDamage < player.getHealth() + player.getAbsorptionAmount()) {
            return;
        }
        LivingEntity wolf = findBondWolf(player);
        if (wolf == null) {
            return;
        }
        long gameTime = player.level().getGameTime();
        WolfArmorState state = stateOf(wolf);
        if (!state.isBondReady(gameTime)) {
            return;
        }
        setState(wolf, state.withBondCooldown(gameTime, Math.max(0L, ModConfig.neutronWolfBondCooldownSeconds.get()) * 20L));
        wolf.hurt(source, lethalDamage);
        event.setNewDamage(0.0F);
        playGuardFeedback(wolf, 2);
        playGuardFeedback(player, 2);
    }

    /**
     * 狼自己被一击打死时免死一次，代价是损耗铠甲耐久。
     */
    private static boolean tryTotem(LivingEntity wolf) {
        if (!(wolf.level() instanceof ServerLevel)) {
            return false;
        }
        ItemStack armor = wolf.getItemBySlot(EquipmentSlot.BODY);
        int cost = ModConfig.neutronWolfTotemDurabilityCost.get();
        if (cost > 0) {
            if (!armor.isDamageableItem()) {
                return false;
            }
            int remaining = armor.getMaxDamage() - armor.getDamageValue();
            if (remaining < cost) {
                return false;
            }
            armor.hurtAndBreak(cost, wolf, EquipmentSlot.BODY);
        }
        wolf.setHealth(wolf.getMaxHealth());
        playGuardFeedback(wolf, 1);
        return true;
    }

    /**
     * 距离 → 减伤比例：羁绊半径内 1.0，最远距离之外保底，中间线性。
     */
    public static double reductionAt(double distance) {
        double bond = ModConfig.neutronWolfBondRadius.get();
        double max = Math.max(bond + EPSILON, ModConfig.neutronWolfMaxRange.get());
        double floor = ModConfig.neutronWolfMinReduction.get();
        if (distance == Double.MAX_VALUE || distance >= max) {
            return 1.0D - floor;
        }
        if (distance <= bond) {
            return 1.0D;
        }
        double progress = (distance - bond) / (max - bond);
        return 1.0D - floor * progress;
    }

    private static LivingEntity findBondWolf(ServerPlayer player) {
        double radius = ModConfig.neutronWolfBondRadius.get();
        AABB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> candidates = player.level().getEntitiesOfClass(LivingEntity.class, area,
                wolf -> isArmoredWolf(wolf) && isOwnedBy(wolf, player));
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            double distance = candidate.distanceToSqr(player);
            if (distance <= radius * radius && distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    private static boolean isOwnedBy(LivingEntity wolf, ServerPlayer player) {
        return wolf instanceof OwnableEntity ownable && ownable.getOwner() == player;
    }

    private static void syncHealthBonus(LivingEntity wolf) {
        AttributeInstance maxHealth = wolf.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        if (isArmoredWolf(wolf)) {
            boolean firstApplication = maxHealth.getModifier(HEALTH_MODIFIER_ID) == null;
            maxHealth.addOrUpdateTransientModifier(new AttributeModifier(HEALTH_MODIFIER_ID,
                    ModConfig.neutronWolfHealthBonus.get(), AttributeModifier.Operation.ADD_VALUE));
            if (firstApplication) {
                wolf.setHealth(wolf.getMaxHealth());
            }
        } else if (maxHealth.removeModifier(HEALTH_MODIFIER_ID)) {
            wolf.setHealth(Math.min(wolf.getHealth(), wolf.getMaxHealth()));
        }
    }

    private static boolean isArmoredWolf(LivingEntity entity) {
        return entity.isAlive()
                && entity.getType() == EntityType.WOLF
                && entity.getItemBySlot(EquipmentSlot.BODY).is(ModItems.neutron_wolf_armor.get());
    }

    private static boolean isHaxDamage(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    private static double ownerDistance(LivingEntity wolf) {
        LivingEntity owner = ownerOf(wolf);
        return owner == null ? Double.MAX_VALUE : Math.sqrt(wolf.distanceToSqr(owner));
    }

    private static LivingEntity ownerOf(LivingEntity wolf) {
        if (!(wolf instanceof OwnableEntity ownable)) {
            return null;
        }
        LivingEntity owner = ownable.getOwner();
        if (owner == null || owner.isRemoved() || owner.isDeadOrDying() || owner.level() != wolf.level()) {
            return null;
        }
        return owner;
    }

    private static WolfArmorState stateOf(LivingEntity wolf) {
        WolfArmorState state = wolf.getExistingDataOrNull(ModDataAttachments.WOLF_ARMOR_STATE);
        return state == null ? WolfArmorState.EMPTY : state;
    }

    private static void setState(LivingEntity wolf, WolfArmorState state) {
        wolf.setData(ModDataAttachments.WOLF_ARMOR_STATE, state);
    }

    private static void playGuardFeedback(LivingEntity entity, int action) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.5D;
        double z = entity.getZ();
        if (action == 0) {
            level.sendParticles(ParticleTypes.ENCHANT, x, y, z, 8, 0.35D, 0.45D, 0.35D, 0.02D);
            level.playSound(null, x, y, z, SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 0.8F, 1.5F);
        } else if (action == 1) {
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 30, 0.4D, 0.6D, 0.4D, 0.3D);
            level.playSound(null, x, y, z, SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        } else {
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 20, 0.3D, 0.5D, 0.3D, 0.25D);
            level.sendParticles(ParticleTypes.HEART, x, y + 0.6D, z, 4, 0.3D, 0.3D, 0.3D, 0.0D);
            level.playSound(null, x, y, z, SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 0.8F, 1.4F);
        }
    }
}
