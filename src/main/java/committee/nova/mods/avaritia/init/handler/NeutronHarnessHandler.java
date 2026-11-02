package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.HarnessOwner;
import committee.nova.mods.avaritia.common.component.HarnessShieldState;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDataAttachments;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 中子战魂铠的专属机制（恶魂装备）。
 *
 * <ul>
 *   <li><b>召唤</b>：玩家腾空时按键，在脚下生成一只穿铠的善良恶魂并把玩家驮起来；</li>
 *   <li><b>立体防御</b>：按键展开半径 4 格的护盾，锚点周围非敌对实体免疫一切伤害。</li>
 * </ul>
 * <p>护盾以锚点为中心：骑着恶魂时锚点是恶魂，否则是玩家自己。</p>
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public final class NeutronHarnessHandler {
    private static final int SHIELD_PARTICLE_POINTS = 16;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final Identifier FLYING_SPEED_MODIFIER_ID = Const.rl("neutron_harness_flying_speed");
    /** 活跃护盾：玩家 UUID → 锚点与结束刻。 */
    private static final Map<UUID, ActiveShield> SHIELDS = new HashMap<>();

    private NeutronHarnessHandler() {
    }

    private record ActiveShield(ServerPlayer owner, Entity anchor, long until) {
    }

    /**
     * 召唤：把最近一只穿中子战魂铠的善良恶魂瞬移到玩家脚下并驮起玩家。
     * <p>只搜索当前维度已加载的实体，跳过已被其他生物骑着的恶魂；脚下放不下时原样返回。</p>
     */
    public static boolean summonMount(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || player.isPassenger() || player.onGround()) {
            return false;
        }
        HappyGhast ghast = findNearestArmoredGhast(level, player);
        if (ghast == null) {
            failFeedback(level, player);
            return false;
        }
        double originX = ghast.getX();
        double originY = ghast.getY();
        double originZ = ghast.getZ();
        ghast.setPos(player.getX(), player.getY() - ghast.getBbHeight() - 0.05D, player.getZ());
        ghast.setYRot(player.getYRot());
        ghast.setDeltaMovement(Vec3.ZERO);
        if (!level.noCollision(ghast)) {
            ghast.setPos(originX, originY, originZ);
            failFeedback(level, player);
            return false;
        }
        if (ownerOf(ghast).isUnowned()) {
            ghast.setData(ModDataAttachments.HARNESS_OWNER, new HarnessOwner(player.getUUID()));
        }
        player.startRiding(ghast, true, true);
        level.playSound(null, ghast.getX(), ghast.getY(), ghast.getZ(),
                SoundEvents.HARNESS_EQUIP, SoundSource.NEUTRAL, 1.0F, 1.0F);
        return player.getVehicle() == ghast;
    }

    /**
     * 已加载区域里距离玩家最近、穿着中子战魂铠且没有乘客的善良恶魂。
     * <p>优先玩家名下的恶魂，其次无主的；别人的恶魂不会被抢。</p>
     */
    private static HappyGhast findNearestArmoredGhast(ServerLevel level, ServerPlayer player) {
        HappyGhast owned = null;
        HappyGhast unowned = null;
        double ownedDistance = Double.MAX_VALUE;
        double unownedDistance = Double.MAX_VALUE;
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof HappyGhast ghast) || ghast.isRemoved() || !ghast.getPassengers().isEmpty()) {
                continue;
            }
            if (!isNeutronHarness(ghast.getItemBySlot(EquipmentSlot.BODY))) {
                continue;
            }
            HarnessOwner owner = ownerOf(ghast);
            double distance = ghast.distanceToSqr(player);
            if (owner.isOwnedBy(player.getUUID())) {
                if (distance < ownedDistance) {
                    ownedDistance = distance;
                    owned = ghast;
                }
            } else if (owner.isUnowned() && distance < unownedDistance) {
                unownedDistance = distance;
                unowned = ghast;
            }
        }
        return owned != null ? owned : unowned;
    }

    private static HarnessOwner ownerOf(HappyGhast ghast) {
        HarnessOwner owner = ghast.getExistingDataOrNull(ModDataAttachments.HARNESS_OWNER);
        return owner == null ? HarnessOwner.EMPTY : owner;
    }

    /**
     * 展开立体防御：锚点在骑乘的恶魂（或玩家自身），范围内的友方与中立实体免疫一切伤害。
     */
    public static void openShield(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        Entity anchor = shieldAnchor(player);
        if (anchor == null) {
            failFeedback(level, player);
            return;
        }
        long gameTime = level.getGameTime();
        HarnessShieldState state = stateOf(player);
        if (state.isCoolingDown(gameTime)) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.NOTE_BLOCK_BASS, SoundSource.PLAYERS, 0.8F, 0.6F);
            return;
        }
        long duration = Math.max(1L, ModConfig.neutronHarnessShieldDurationSeconds.get()) * 20L;
        long cooldown = Math.max(1L, ModConfig.neutronHarnessShieldCooldownSeconds.get()) * 20L;
        SHIELDS.put(player.getUUID(), new ActiveShield(player, anchor, gameTime + duration));
        player.setData(ModDataAttachments.HARNESS_SHIELD, new HarnessShieldState(gameTime + duration, gameTime + cooldown));
        level.playSound(null, anchor.getX(), anchor.getY(), anchor.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    /** 护盾内的一切伤害被取消（含穿甲与无尽伤害）；穿铠的善良恶魂另有固定减伤。 */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity target) || target.level().isClientSide()) {
            return;
        }
        if (!SHIELDS.isEmpty() && isProtected(target)) {
            event.setCanceled(true);
            return;
        }
        if (target instanceof HappyGhast ghast && isNeutronHarness(ghast.getItemBySlot(EquipmentSlot.BODY))) {
            double remaining = 1.0D - ModConfig.neutronHarnessGhastDamageReduction.get();
            event.setAmount((float) (event.getAmount() * Math.max(0.0D, remaining)));
        }
    }

    /** 玩家第一次骑上穿铠的善良恶魂就算认主。 */
    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (!event.isMounting() || event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntityMounting() instanceof ServerPlayer player)
                || !(event.getEntityBeingMounted() instanceof HappyGhast ghast)) {
            return;
        }
        if (!isNeutronHarness(ghast.getItemBySlot(EquipmentSlot.BODY))) {
            return;
        }
        if (ownerOf(ghast).isUnowned()) {
            ghast.setData(ModDataAttachments.HARNESS_OWNER, new HarnessOwner(player.getUUID()));
        }
    }

    /** 每 tick 画护盾球面，并清理过期护盾。 */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (SHIELDS.isEmpty()) {
            return;
        }
        SHIELDS.values().removeIf(shield -> {
            boolean expired = shield.owner().isRemoved()
                    || shield.anchor().isRemoved()
                    || shield.owner().level() != shield.anchor().level()
                    || shield.until() <= shield.anchor().level().getGameTime();
            return expired;
        });
        for (ActiveShield shield : SHIELDS.values()) {
            drawShield(shield.anchor());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SHIELDS.clear();
    }

    /**
     * 飞行速度加成只在「玩家骑乘时」生效。
     * <p>没有玩家时撤回加成，避免无人乘坐的恶魂自己飘走。</p>
     */
    @SubscribeEvent
    public static void onGhastTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast) || ghast.level().isClientSide()) {
            return;
        }
        if (ghast.tickCount % 20 != 0) {
            return;
        }
        AttributeInstance flying = ghast.getAttribute(Attributes.FLYING_SPEED);
        if (flying == null) {
            return;
        }
        boolean ridden = ghast.getFirstPassenger() instanceof Player;
        if (ridden && isNeutronHarness(ghast.getItemBySlot(EquipmentSlot.BODY))) {
            flying.addOrUpdateTransientModifier(new AttributeModifier(FLYING_SPEED_MODIFIER_ID,
                    0.45D, AttributeModifier.Operation.ADD_VALUE));
        } else {
            flying.removeModifier(FLYING_SPEED_MODIFIER_ID);
        }
    }

    private static boolean isProtected(LivingEntity target) {
        long gameTime = target.level().getGameTime();
        double radius = ModConfig.neutronHarnessShieldRadius.get();
        for (ActiveShield shield : SHIELDS.values()) {
            Entity anchor = shield.anchor();
            if (gameTime >= shield.until() || anchor.isRemoved() || anchor.level() != target.level()) {
                continue;
            }
            if (target == anchor || target.getRootVehicle() == anchor) {
                return true;
            }
            if (!(target instanceof Enemy) && target.distanceToSqr(anchor) <= radius * radius) {
                return true;
            }
        }
        return false;
    }

    private static void drawShield(Entity anchor) {
        if (!(anchor.level() instanceof ServerLevel level)) {
            return;
        }
        double radius = ModConfig.neutronHarnessShieldRadius.get();
        double centerX = anchor.getX();
        double centerY = anchor.getY() + anchor.getBbHeight() * 0.5D;
        double centerZ = anchor.getZ();
        for (int index = 0; index < SHIELD_PARTICLE_POINTS; index++) {
            double y = 1.0D - 2.0D * index / (SHIELD_PARTICLE_POINTS - 1);
            double ring = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
            double theta = index * GOLDEN_ANGLE;
            level.sendParticles(ParticleTypes.END_ROD,
                    centerX + Math.cos(theta) * ring * radius,
                    centerY + y * radius,
                    centerZ + Math.sin(theta) * ring * radius,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    /** 护盾锚点：优先骑着的、穿着中子战魂铠的善良恶魂。 */
    private static Entity shieldAnchor(ServerPlayer player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof HappyGhast ghast && isNeutronHarness(ghast.getItemBySlot(EquipmentSlot.BODY))) {
            return ghast;
        }
        return null;
    }

    private static boolean isNeutronHarness(ItemStack stack) {
        return stack.is(ModItems.neutron_harness.get());
    }

    private static HarnessShieldState stateOf(ServerPlayer player) {
        HarnessShieldState state = player.getExistingDataOrNull(ModDataAttachments.HARNESS_SHIELD);
        return state == null ? HarnessShieldState.EMPTY : state;
    }

    private static void failFeedback(ServerLevel level, ServerPlayer player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    /** 供 GameTest 断言护盾覆盖判定。 */
    public static boolean isEntityProtected(LivingEntity target) {
        return isProtected(target);
    }

    /** 供 GameTest 直接登记护盾，跳过按键路径。 */
    public static void registerShieldForTest(ServerPlayer player, Entity anchor, long durationTicks) {
        long gameTime = player.level().getGameTime();
        SHIELDS.put(player.getUUID(), new ActiveShield(player, anchor, gameTime + durationTicks));
    }
}
