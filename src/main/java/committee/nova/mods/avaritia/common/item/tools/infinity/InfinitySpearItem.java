package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.IUndamageable;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.common.component.SpearMark;
import committee.nova.mods.avaritia.common.component.SpearTargetReference;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.common.item.tools.SpearMarkUtils;
import committee.nova.mods.avaritia.common.item.tools.SpearThrustUtils;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.*;
import committee.nova.mods.avaritia.util.InfinityDamageUtils;
import committee.nova.mods.avaritia.util.ToolUtils;
import lombok.NonNull;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static committee.nova.mods.avaritia.init.registry.ModToolTiers.INFINITY;

public class InfinitySpearItem extends Item implements InitEnchantItem, ISwitchable, IUndamageable {
    private static final String MODE_NORMAL = "infinity_spear_normal";
    private static final String MODE_LUNGE = "infinity_spear_lunge";
    private static final String MODE_LONG_RANGE = "infinity_spear_long_range";
    private static final List<String> MODES = List.of(MODE_NORMAL, MODE_LUNGE, MODE_LONG_RANGE);
    private static final double AUTO_TARGET_RANGE = 128.0D;
    private static final int AUTO_TARGET_POOL_SIZE = 5;
    private static final int TARGET_LOAD_COOLDOWN_TICKS = 10;
    private static final int LUNGE_LEVEL = 10;

    public InfinitySpearItem() {
        super(ModItems.properties()
                .rarity(ModRarities.COSMIC.getValue())
                .stacksTo(1)
                .fireResistant()
                .spear(INFINITY,0.25f,9999f,0.2f,10f,5.1f,10f,4f,10f,0.1f)
                .component(DataComponents.ATTACK_RANGE, new AttackRange(1.0F, 9.5F, 0.0F, 11.5F, 0.125F, 0.5F))
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ModToolTiers.INFINITY.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, ModToolTiers.INFINITY.speed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .build()
                        ));
    }
    private final InitEnchantment initEnchantment = new InitEnchantment(Enchantments.LOOTING, 10);

    public ToolMaterial getTier() {
        return ModToolTiers.INFINITY;
    }
    // ==================== 模式切换：shift+右键 ====================
    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            cycleMode(level, player, hand, MODES);
            return InteractionResult.SUCCESS;
        }

        if (!isActive(stack, MODE_LONG_RANGE)) {
            return super.use(level, player, hand);
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;
        SpearTargetReference markedTarget = stack.get(ModDataComponents.INFINITY_SPEAR_TARGET.get());
        if (markedTarget != null) {
            if (!markedTarget.isOwnedBy(player.getUUID(), level.getGameTime())) {
                stack.remove(ModDataComponents.INFINITY_SPEAR_TARGET.get());
                markedTarget = null;
            } else if (!markedTarget.dimension().equals(level.dimension().identifier())) {
                player.sendOverlayMessage(Component.translatable(
                        "message.avaritia.infinity_spear.target_unavailable"));
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        if (markedTarget != null) {
            LivingEntity target = markedTarget.resolve(level);
            if (target == null) {
                loadTargetChunkAndAttack(serverLevel, serverPlayer, hand, stack, markedTarget);
                return InteractionResult.SUCCESS_SERVER;
            }
            if (SpearThrustUtils.isEligibleTarget(player, target)
                    && SpearMarkUtils.isMarkedBy(target, player)) {
                tryAttackTarget(serverLevel, serverPlayer, hand, target);
                return InteractionResult.SUCCESS_SERVER;
            }
            stack.remove(ModDataComponents.INFINITY_SPEAR_TARGET.get());
        }

        LivingEntity target = SpearThrustUtils.selectRandomTarget(
                serverLevel, serverPlayer, AUTO_TARGET_RANGE, AUTO_TARGET_POOL_SIZE);
        if (target == null) {
            player.sendOverlayMessage(Component.translatable("message.avaritia.infinity_spear.no_target"));
            return InteractionResult.SUCCESS_SERVER;
        }
        tryAttackTarget(serverLevel, serverPlayer, hand, target);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void tryAttackTarget(ServerLevel level, ServerPlayer player, InteractionHand hand,
                                        LivingEntity target) {
        if (!SpearThrustUtils.isEligibleTarget(player, target) || target.level() != level) {
            player.sendOverlayMessage(Component.translatable(
                    "message.avaritia.infinity_spear.target_unavailable"));
            return;
        }
        if (!SpearThrustUtils.movePlayerToTarget(level, player, target)) {
            player.sendOverlayMessage(Component.translatable(
                    "message.avaritia.infinity_spear.target_unavailable"));
            return;
        }

        if (SpearThrustUtils.stabTarget(player, hand, target)) {
            player.onAttack();
        }
    }

    private static void loadTargetChunkAndAttack(ServerLevel level, ServerPlayer player, InteractionHand hand,
                                                 ItemStack stack, SpearTargetReference requestedTarget) {
        player.getCooldowns().addCooldown(stack, TARGET_LOAD_COOLDOWN_TICKS);
        level.getChunkSource()
                .addTicketAndLoadWithRadius(TicketType.PORTAL, requestedTarget.lastKnownChunk(), 0)
                .whenComplete((ignored, error) -> level.getServer().execute(() -> {
                    if (player.isRemoved() || player.level() != level || player.getItemInHand(hand) != stack
                            || !ISwitchable.isMode(stack, MODE_LONG_RANGE)) {
                        return;
                    }

                    SpearTargetReference currentTarget = stack.get(ModDataComponents.INFINITY_SPEAR_TARGET.get());
                    if (currentTarget == null || !currentTarget.equals(requestedTarget)
                            || !currentTarget.isOwnedBy(player.getUUID(), level.getGameTime())) {
                        return;
                    }
                    if (error != null) {
                        player.sendOverlayMessage(Component.translatable(
                                "message.avaritia.infinity_spear.target_unavailable"));
                        return;
                    }

                    LivingEntity target = currentTarget.resolve(level);
                    if (target == null
                            || !SpearThrustUtils.isEligibleTarget(player, target)
                            || !SpearMarkUtils.isMarkedBy(target, player)) {
                        stack.remove(ModDataComponents.INFINITY_SPEAR_TARGET.get());
                        player.sendOverlayMessage(Component.translatable(
                                "message.avaritia.infinity_spear.target_unavailable"));
                        return;
                    }
                    tryAttackTarget(level, player, hand, target);
                }));
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity owner,
                              @Nullable EquipmentSlot slot) {
        SpearTargetReference markedTarget = stack.get(ModDataComponents.INFINITY_SPEAR_TARGET.get());
        if (markedTarget != null) {
            if (!(owner instanceof Player player)
                    || !markedTarget.isOwnedBy(player.getUUID(), level.getGameTime())) {
                stack.remove(ModDataComponents.INFINITY_SPEAR_TARGET.get());
                super.inventoryTick(stack, level, owner, slot);
                return;
            }

            LivingEntity target = markedTarget.resolve(level);
            if (target != null) {
                SpearMark mark = SpearMarkUtils.getActiveMark(target);
                if (!SpearThrustUtils.isEligibleTarget(player, target)
                        || mark == null
                        || !mark.isOwnedBy(player.getUUID(), level.getGameTime())) {
                    stack.remove(ModDataComponents.INFINITY_SPEAR_TARGET.get());
                } else {
                    SpearTargetReference refreshedTarget = markedTarget.refreshPosition(target, mark.expiresAt());
                    if (refreshedTarget != markedTarget) {
                        stack.set(ModDataComponents.INFINITY_SPEAR_TARGET.get(), refreshedTarget);
                    }
                }
            }
        }
        super.inventoryTick(stack, level, owner, slot);
    }

    // ==================== Looting 10 常驻 + 突进模式才生效的原版 LUNGE ====================
    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.LOOTING)) return 10;
        if (enchantmentHolder.is(Enchantments.LUNGE) && isActive((ItemStack) stack, MODE_LUNGE)) return LUNGE_LEVEL;
        return 0;
    }

    // ==================== 一击必杀 ====================
    /**
     * 矛的两条攻击路径（左键穿刺 PiercingWeapon / 右键蓄力 KineticWeapon）
     * 都会在伤害结算后调用 hurtEnemy，这里统一挂秒杀逻辑。
     */
    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (attacker instanceof Player player && target.level() instanceof ServerLevel serverLevel) {
            if (target.isAlive()) {
                boolean newlyMarked = !SpearMarkUtils.isMarkedBy(target, player);
                SpearMark mark = SpearMarkUtils.apply(target, player);
                stack.set(ModDataComponents.INFINITY_SPEAR_TARGET.get(),
                        SpearTargetReference.of(target, player.getUUID(), mark.expiresAt()));
                if (newlyMarked) {
                    player.sendOverlayMessage(Component.translatable(
                            "message.avaritia.infinity_spear.marked", target.getDisplayName()));
                }
            }
            // 获取是否启用无限伤害的配置选项
            var endlessDamage = ModConfig.isSwordAttackEndless.get();
            // 创建伤害源
            var damageSource = ModDamageTypes.source(player.level(), target, player);
            // PvP 保护：穿全套无尽甲的玩家不秒杀，只产生爆炸（与无尽剑一致）
            if (target instanceof Player pvp && ToolUtils.isInfinite(pvp)) {
                serverLevel.explode(player, pvp.getBlockX(), pvp.getBlockY(), pvp.getBlockZ(), 25.0F, Level.ExplosionInteraction.MOB);
                return;
            }
            // 如果启用无限伤害
            if (endlessDamage) {
                // 直接强制杀死目标
                InfinityDamageUtils.forceKill(serverLevel, target, damageSource);
            } else if (target instanceof EnderDragon dragon) {
                dragon.hurt(serverLevel, dragon.head, damageSource,
                        ModToolTiers.INFINITY.attackDamageBonus()
                                * SpearThrustUtils.remoteDamageMultiplier(player, target));
            } else {
                target.invulnerableTime = 0;
                target.hurtServer(serverLevel, damageSource,
                        ModToolTiers.INFINITY.attackDamageBonus()
                                * SpearThrustUtils.remoteDamageMultiplier(player, target));
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }

    // ==================== 不朽物品实体 ====================
    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntityTypes.IMMORTAL.get(), level, location, stack);
    }

    // ==================== 外观/耐久 ====================
    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NonNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        // Looting 10 常驻显示
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        if (isActive(stack, MODE_LUNGE)) {
            // 切换后显示：“自带 突刺 X”（原版 LUNGE 附魔名 + 等级）
            var registries = context.registries();
            if (registries != null) {
                registries.lookupOrThrow(Registries.ENCHANTMENT)
                        .get(Enchantments.LUNGE)
                        .ifPresent(holder -> tooltipComponents.accept(
                                ModTooltips.INIT_ENCHANT.args(Enchantment.getFullname(holder, LUNGE_LEVEL)).build()));
            }
            tooltipComponents.accept(Component.translatable("tooltip.avaritia.infinity_spear_lunge.active")
                    .withStyle(ChatFormatting.RED));
        }
        if (isActive(stack, MODE_LONG_RANGE)) {
            tooltipComponents.accept(Component.translatable("tooltip.avaritia.tool.infinity_spear_long_range")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipComponents.accept(Component.translatable("tooltip.avaritia.infinity_spear_long_range.desc")
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}

